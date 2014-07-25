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
package org.spotter.eclipse.ui.jobs;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressConstants;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.handlers.RunHandler;
import org.spotter.eclipse.ui.navigator.SpotterProjectResults;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.shared.status.DiagnosisProgress;
import org.spotter.shared.status.SpotterProgress;

/**
 * A job to run a DynamicSpotter Diagnosis which can be scheduled by the job
 * manager. This job updates the progress monitor according to the progress of
 * the Spotter run that is performed.
 * 
 * @author Denis Knoepfle
 * 
 */
public class SpotterRunJob extends Job {

	private static final String ICON_PATH = "icons/diagnosis.png"; //$NON-NLS-1$

	private static final int SLEEP_TIME_MILLIS = 1000;

	private static final String MSG_SPOTTER_FINISHED = "Finished the Spotter diagnosis!";
	private static final String MSG_LOST_CONNECTION = "Lost connection to Spotter Service!";
	private static final String MSG_CANCELLED = "You cancelled the progress report and will not be informed when the run is finished. DynamicSpotter will continue to run on the server though (cancellation of a running diagnosis is not implemented yet).";

	private final IProject project;
	private final long jobId;
	private final Set<String> processedProblems;
	// TODO: replace this by problems unique id when implemented in Spotter core
	private Map.Entry<String, DiagnosisProgress> currentProblem;

	public SpotterRunJob(IProject project, long jobId) {
		super("DynamicSpotter Diagnosis '" + project.getName() + "'");

		this.project = project;
		this.jobId = jobId;
		this.processedProblems = new HashSet<>();

		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, ICON_PATH);
		setProperty(IProgressConstants.ICON_PROPERTY, imageDescriptor);
//		IAction gotoAction = new Action("Results") {
//			public void run() {
//				// TODO: show results in ResultsView
//			}
//		};
//		setProperty(IProgressConstants.ACTION_PROPERTY, gotoAction);
		setPriority(LONG);
		setUser(true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		processedProblems.clear();
		currentProblem = null;
		monitor.beginTask("DynamicSpotter Diagnosis", 100);
		ServiceClientWrapper client = Activator.getDefault().getClient(project.getName());

		while (client.isRunning()) {
			if (monitor.isCanceled()) {
				DialogUtils.openInformation(RunHandler.DIALOG_TITLE, MSG_CANCELLED);
				return new Status(Status.CANCEL, Activator.PLUGIN_ID, Status.OK, MSG_CANCELLED, null);
			}
			Long currentJobId = client.getCurrentJobId();
			// check if the job is still being processed
			if (currentJobId != null && currentJobId == jobId) {
				updateCurrentRun(client, monitor);
				try {
					Thread.sleep(SLEEP_TIME_MILLIS);
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
			} else {
				break;
			}
		}
		if (!client.testConnection(false)) {
			DialogUtils.openWarning(RunHandler.DIALOG_TITLE, MSG_LOST_CONNECTION);
			return new Status(Status.WARNING, Activator.PLUGIN_ID, Status.OK, MSG_LOST_CONNECTION, null);
		}

		monitor.worked(100);
		monitor.done();
		onFinishedJob();

		// keep the finished job in the progress view only if
		// it is not running in the progress dialog
		Boolean inDialog = (Boolean) getProperty(IProgressConstants.PROPERTY_IN_DIALOG);
		if (inDialog != null && !inDialog.booleanValue()) {
			setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		}
		return Status.OK_STATUS;
	}

	private void updateCurrentRun(ServiceClientWrapper client, IProgressMonitor monitor) {
		// TODO: implement correct progress view (needs more data from service)
		SpotterProgress spotterProgress = client.getCurrentProgressReport();
		if (spotterProgress == null || spotterProgress.getProblemProgressMapping() == null) {
			return;
		}

		Map<String, DiagnosisProgress> progressAll = spotterProgress.getProblemProgressMapping();
		if (progressAll.isEmpty()) {
			return;
		}

		for (Map.Entry<String, DiagnosisProgress> progressEntry : progressAll.entrySet()) {
			String key = progressEntry.getKey();
			if (processedProblems.contains(key)) {
				if (currentProblem.getKey().equals(key)) {
					currentProblem = progressEntry;
				}
			} else {
				currentProblem = progressEntry;
				processedProblems.add(currentProblem.getKey());
			}
		}
		if (currentProblem != null) {
			DiagnosisProgress progress = currentProblem.getValue();
			monitor.subTask("ProblemId: \"" + currentProblem.getKey() + "\" - " + progress.getCurrentProgressMessage()
					+ " - " + progress.getStatus());
			// currently this is just a fake representation of work completed
			monitor.worked(5);
		}
	}

	private void onFinishedJob() {
		final Activator activator = Activator.getDefault();
		final Display display = PlatformUI.getWorkbench().getDisplay();

		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				Map<String, SpotterProjectResults> results = activator.getProjectHistoryElements();
				results.get(project.getName()).refreshChildren();
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openInformation(shell, RunHandler.DIALOG_TITLE, MSG_SPOTTER_FINISHED);
			}
		});
	}

}
