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

import java.util.Properties;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.UICoreException;
import org.spotter.eclipse.ui.jobs.DynamicSpotterRunJob;
import org.spotter.eclipse.ui.jobs.JobsContainer;
import org.spotter.eclipse.ui.model.xml.HierarchyFactory;
import org.spotter.eclipse.ui.model.xml.MeasurementEnvironmentFactory;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;
import org.spotter.shared.configuration.FileManager;
import org.spotter.shared.configuration.JobDescription;
import org.spotter.shared.environment.model.XMeasurementEnvironment;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;

/**
 * A run handler for the DynamicSpotter run command which starts the
 * DynamicSpotter diagnosis if it is not already running.
 * 
 * @author Denis Knoepfle
 * 
 */
public class RunHandler extends AbstractHandler implements ISelectionChangedListener {

	/**
	 * The id of the run command.
	 */
	public static final String RUN_COMMAND_ID = "org.spotter.eclipse.ui.commands.run";

	public static final String DIALOG_TITLE = "DynamicSpotter Diagnosis";

	private static final String MSG_MULTI_SELECTION = "More than one project has been selected.";
	private static final String MSG_MISS_CONFIG = "DynamicSpotter Configuration '%s' is missing!";
	private static final String MSG_ALREADY_RUNNING = "DynamicSpotter is already running!";
	private static final String MSG_NO_CONNECTION = "No connection to DynamicSpotter Service!";
	private static final String MSG_RUNTIME_ERROR = "Error occured during diagnosis: %s";
	private static final String MSG_SPOTTER_STARTED = "Going to start DynamicSpotter diagnosis for project '%s' now. Continue?";

	private boolean isEnabled;

	/**
	 * Constructor.
	 */
	public RunHandler() {
		super();
		selectionChanged(null);
		Activator.getDefault().addProjectSelectionListener(this);
	}

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

		String spotterFileName = FileManager.SPOTTER_CONFIG_FILENAME;
		IFile spotterFile = project.getFile(spotterFileName);
		String spotterFilePath = spotterFile.getLocation().toString();

		if (!spotterFile.exists()) {
			DialogUtils.openWarning(DIALOG_TITLE, String.format(MSG_MISS_CONFIG, spotterFilePath));
			return null;
		}
		if (client.isRunning(true)) {
			DialogUtils.openWarning(DIALOG_TITLE, MSG_ALREADY_RUNNING);
			return null;
		}
		if (client.isConnectionIssue()) {
			DialogUtils.openWarning(DIALOG_TITLE, MSG_NO_CONNECTION);
			return null;
		}

		boolean startConfirm = DialogUtils.openConfirm(DIALOG_TITLE,
				String.format(MSG_SPOTTER_STARTED, project.getName()));
		if (startConfirm) {
			startSpotterRun(project, client);
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	private void startSpotterRun(IProject project, ServiceClientWrapper client) {
		JobDescription jobDescription;
		try {
			jobDescription = createJobDescription(project);
		} catch (UICoreException e) {
			String message = "Problem while parsing configuration files!";
			DialogUtils.handleError(message, e);
			return;
		}

		if (!checkJobDescription(jobDescription)) {
			DialogUtils.openWarning("The hierarchy must not be empty!");
			return;
		}

		Long jobId = client.startDiagnosis(jobDescription);
		if (jobId != null && jobId != 0) {
			DynamicSpotterRunJob job = new DynamicSpotterRunJob(project, jobId, System.currentTimeMillis());
			job.schedule();
			JobsContainer.addRunningJob(jobId, job);
		} else {
			String msg = String.format(MSG_RUNTIME_ERROR, "Could not retrieve a valid job id!");
			DialogUtils.openError(DIALOG_TITLE, msg);
		}
	}

	private boolean checkJobDescription(JobDescription jobDescription) {
		XPerformanceProblem hierarchy = jobDescription.getHierarchy();
		if (hierarchy == null || hierarchy.getProblem() == null || hierarchy.getProblem().isEmpty()) {
			return false;
		}
		return true;
	}

	private JobDescription createJobDescription(IProject project) throws UICoreException {
		JobDescription jobDescription = new JobDescription();

		IFile spotterFile = project.getFile(FileManager.SPOTTER_CONFIG_FILENAME);
		Properties dynamicSpotterConfig = SpotterProjectSupport.getSpotterConfig(spotterFile);
		jobDescription.setDynamicSpotterConfig(dynamicSpotterConfig);

		MeasurementEnvironmentFactory envFactory = MeasurementEnvironmentFactory.getInstance();
		String envFile = project.getFile(FileManager.ENVIRONMENT_FILENAME).getLocation().toString();
		XMeasurementEnvironment measurementEnvironment = envFactory.parseXMLFile(envFile);
		jobDescription.setMeasurementEnvironment(measurementEnvironment);

		HierarchyFactory hierFactory = HierarchyFactory.getInstance();
		String hierFile = project.getFile(FileManager.HIERARCHY_FILENAME).getLocation().toString();
		XPerformanceProblem hierarchy = hierFactory.parseHierarchyFile(hierFile);
		jobDescription.setHierarchy(hierarchy);

		return jobDescription;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		this.isEnabled = Activator.getDefault().getSelectedProjects().size() == 1;
	}

}
