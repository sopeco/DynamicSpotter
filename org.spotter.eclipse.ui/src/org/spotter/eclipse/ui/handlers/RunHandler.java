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

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.jobs.SpotterRunJob;
import org.spotter.eclipse.ui.util.DialogUtils;
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

	public static final String DIALOG_TITLE = "DynamicSpotter Diagnosis";

	private static final String MSG_MULTI_SELECTION = "More than one project has been selected.";
	private static final String MSG_MISS_CONFIG = "DynamicSpotter Configuration '%s' is missing!";
	private static final String MSG_ALREADY_RUNNING = "DynamicSpotter is already running";
	private static final String MSG_RUNTIME_ERROR = "Error occured during diagnosis: %s";
	private static final String MSG_SPOTTER_STARTED = "Going to start Spotter diagnosis for project '%s' now. Continue?";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator activator = Activator.getDefault();
		Set<IProject> selectedProjects = activator.getSelectedProjects();

		if (selectedProjects.size() != 1) {
			DialogUtils.openWarning(DIALOG_TITLE, MSG_MULTI_SELECTION);
			return null;
		}

		IProject project = selectedProjects.iterator().next();
		ServiceClientWrapper client = activator.getClient(project.getName());
		String spotterFileName = SpotterProjectSupport.SPOTTER_CONFIG_FILENAME;
		IFile spotterFile = project.getFile(spotterFileName);
		String spotterFilePath = spotterFile.getLocation().toString();

		if (!spotterFile.exists()) {
			DialogUtils.openError(DIALOG_TITLE, String.format(MSG_MISS_CONFIG, spotterFilePath));
			return null;
		}
		if (client.isRunning()) {
			DialogUtils.openWarning(DIALOG_TITLE, MSG_ALREADY_RUNNING);
			return null;
		}

		boolean startConfirm = MessageDialog.openConfirm(null, DIALOG_TITLE,
				String.format(MSG_SPOTTER_STARTED, project.getName()));
		if (startConfirm) {
			startSpotterRun(project, client, spotterFilePath);
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return Activator.getDefault().getSelectedProjects().size() == 1;
	}

	private void startSpotterRun(IProject project, ServiceClientWrapper client, String spotterConfigPath) {
		Long jobId = client.startDiagnosis(spotterConfigPath);
		if (jobId != null) {
			SpotterRunJob job = new SpotterRunJob(project, jobId);
			job.schedule();
		} else {
			String msg = String.format(MSG_RUNTIME_ERROR, "Could not retrieve a valid job id!");
			DialogUtils.openError(DIALOG_TITLE, msg);
		}
	}

}
