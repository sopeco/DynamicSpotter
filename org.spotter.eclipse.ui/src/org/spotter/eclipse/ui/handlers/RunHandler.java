/**
 * Copyright 2014 SAP AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spotter.eclipse.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.lpe.common.util.system.LpeSystemUtils;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;

/**
 * A run handler for the Spotter run command which starts the Spotter diagnosis
 * if it is not already running.
 * 
 * @author Denis Knoepfle
 * 
 */
public class RunHandler extends AbstractHandler {

	/**
	 * The id of the run command.
	 */
	public static final String RUN_COMMAND_ID = "org.spotter.eclipse.ui.commands.run";

	private static final int SLEEP_TIME_MILLIS = 1000;

	private static final String DIALOG_TITLE = "DynamicSpotter Diagnosis";
	private static final String MSG_MULTI_SELECTION = "More than one project has been selected.";
	private static final String MSG_MISS_CONFIG = "DynamicSpotter Configuration '%s' is missing!";
	private static final String MSG_ALREADY_RUNNING = "DynamicSpotter is already running";
	private static final String MSG_RUNTIME_ERROR = "Error occured during diagnosis: %s";
	private static final String MSG_SPOTTER_STARTED = "Going to start DynamicSpotter diagnosis for project '%s' now. Continue?";
	private static final String MSG_SPOTTER_FINISHED = "Finished the DynamicSpotter diagnosis!";

	private boolean startConfirm;
	private String runningProject;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (Activator.getDefault().getSelectedProjects().size() != 1) {
			Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
			MessageDialog.openWarning(shell, DIALOG_TITLE, MSG_MULTI_SELECTION);
			return null;
		}

		IProject project = Activator.getDefault().getSelectedProjects().iterator().next();
		ServiceClientWrapper client = Activator.getDefault().getClient(project.getName());
		String spotterFileName = SpotterProjectSupport.SPOTTER_CONFIG_FILENAME;
		IFile spotterFile = project.getFile(spotterFileName);
		String spotterFilePath = spotterFile.getLocation().toString();

		if (!spotterFile.exists()) {
			Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
			MessageDialog.openError(shell, DIALOG_TITLE, String.format(MSG_MISS_CONFIG, spotterFilePath));
			return null;
		}
		if (client.isRunning()) {
			Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
			MessageDialog.openWarning(shell, DIALOG_TITLE, MSG_ALREADY_RUNNING);
			return null;
		}

		startConfirm = MessageDialog.openConfirm(null, DIALOG_TITLE,
				String.format(MSG_SPOTTER_STARTED, project.getName()));
		if (startConfirm) {
			runningProject = project.getName();
			if (!startSpotterRun(client, spotterFilePath)) {
				runningProject = null;
			}
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return Activator.getDefault().getSelectedProjects().size() == 1;
	}

	private boolean startSpotterRun(ServiceClientWrapper client, String spotterConfigPath) {
		Long jobId = client.startDiagnosis(spotterConfigPath);
		if (jobId != null) {
			startPolling(client, jobId);
			return true;
		} else {
			String msg = String.format(MSG_RUNTIME_ERROR, "Could not retrieve a valid job id");
			MessageDialog.openError(null, DIALOG_TITLE, msg);
			return false;
		}
	}

	private void startPolling(final ServiceClientWrapper client, final long jobId) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (client.isRunning()) {
					Long currentJobId = client.getCurrentJobId();
					// check if the job is still being processed
					if (currentJobId != null && currentJobId == jobId) {
						updateCurrentRun(client);
						try {
							Thread.sleep(SLEEP_TIME_MILLIS);
						} catch (InterruptedException e) {
							return;
						}
					} else {
						break;
					}
				}
				// job finished
				Activator.getDefault().getProjectHistoryElements().get(runningProject).refreshChildren();
				showFinishMsgOnUIThread();
			}
		};
		LpeSystemUtils.submitTask(runnable);
	}

	private void updateCurrentRun(ServiceClientWrapper client) {
		// TODO: implement correct progress view (needs more accurate data from the server)
//		SpotterProgress spotterProgress = client.getCurrentProgressReport();
//		if (spotterProgress == null || spotterProgress.getProblemProgressMapping() == null) {
//			return;
//		}
//		Collection<DiagnosisProgress> progressAll = spotterProgress.getProblemProgressMapping().values();
//		double estimatedProgress = 0;
//		long estimatedRemainingDuration = 0;
//		for (DiagnosisProgress progress : progressAll) {
//			estimatedProgress += progress.getEstimatedProgress();
//			estimatedRemainingDuration += progress.getEstimatedRemainingDuration();
//		}
//		estimatedProgress /= progressAll.size();
//		String status = "estimated progress: " + estimatedProgress * 100 + " %, remaining duration: "
//				+ estimatedRemainingDuration;
	}

	private void showFinishMsgOnUIThread() {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = display.getActiveShell();
				MessageDialog.openInformation(shell, DIALOG_TITLE, MSG_SPOTTER_FINISHED);
				TreeViewer viewer = Activator.getDefault().getNavigatorViewer();
				viewer.refresh();
			}
		});
	}

}
