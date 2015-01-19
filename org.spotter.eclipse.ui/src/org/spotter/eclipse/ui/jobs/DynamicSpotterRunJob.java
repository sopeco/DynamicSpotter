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

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressConstants;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.handlers.RunHandler;
import org.spotter.eclipse.ui.navigator.SpotterProjectResults;
import org.spotter.eclipse.ui.navigator.SpotterProjectRunResult;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.shared.status.DiagnosisProgress;
import org.spotter.shared.status.SpotterProgress;

/**
 * A job to run a DynamicSpotter diagnosis which can be scheduled by the job
 * manager. This job updates the progress monitor according to the progress of
 * the DynamicSpotter run that is performed.
 * 
 * @author Denis Knoepfle
 * 
 */
public class DynamicSpotterRunJob extends Job {

	/**
	 * The unique key used to set the job id property of this job.
	 */
	public static final QualifiedName JOB_ID_KEY = new QualifiedName(Activator.PLUGIN_ID, "jobId");
	/**
	 * The identifier used to identify this job as a member of the job family of
	 * DS runs.
	 */
	public static final String DS_RUN_JOB_FAMILY = "org.spotter.eclipse.ui.jobs.DynamicSpotterRunJob";

	private static final String ICON_PATH = "icons/diagnosis.png"; //$NON-NLS-1$

	private static final int SLEEP_TIME_MILLIS = 1000;
	private static final int HUNDRED_PERCENT = 100;
	private static final int KEY_HASH_LENGTH = 7;

	private static final String MSG_RUN_FINISH = "Finished the DynamicSpotter diagnosis successfully!";
	private static final String MSG_RUN_ERROR = "Aborted the DynamicSpotter diagnosis due to an error!";
	private static final String MSG_LOST_CONNECTION = "Lost connection to DS Service!";
	private static final String MSG_CANCELLED = "The progress report has been cancelled and you will "
			+ "not be informed when the run is finished. DynamicSpotter will continue to run on the "
			+ "server though (cancellation of a running diagnosis is not implemented yet).";

	private final IProject project;
	private final long jobId;
	private String currentProblem;
	// determines whether a cancellation will be silent or reported to the user
	private boolean silentCancel;

	/**
	 * Create a new job for the given project and job id.
	 * 
	 * @param project
	 *            The project the job is for
	 * @param jobId
	 *            The job id of the new job
	 * @param timestamp
	 *            The timestamp when the job was initiated
	 */
	public DynamicSpotterRunJob(final IProject project, final long jobId, long timestamp) {
		super("DynamicSpotter Diagnosis '" + project.getName() + "'");

		this.project = project;
		this.jobId = jobId;
		this.silentCancel = false;

		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, ICON_PATH);
		setProperty(IProgressConstants.ICON_PROPERTY, imageDescriptor);

		IAction gotoAction = new Action("Results") {
			public void run() {
				revealResults();
			}
		};

		setProperty(IProgressConstants.ACTION_PROPERTY, gotoAction);
		setProperty(JOB_ID_KEY, String.valueOf(jobId));

		setPriority(LONG);
		setUser(true);

		if (!JobsContainer.registerJobId(project, jobId, timestamp)) {
			DialogUtils.openError(RunHandler.DIALOG_TITLE,
					"There was an error when saving the job id. You may not access the results of the diagnosis run.");
		}
	}

	/**
	 * @return <code>true</code> if cancellation is silent, otherwise
	 *         <code>false</code>
	 */
	public boolean isSilentCancel() {
		return silentCancel;
	}

	/**
	 * Sets whether cancellation is silent or reported to the user.
	 * 
	 * @param silentCancel
	 *            <code>true</code> for silent, otherwise <code>false</code>
	 */
	public void setSilentCancel(boolean silentCancel) {
		this.silentCancel = silentCancel;
	}

	/**
	 * Returns <code>true</code> only if the family is
	 * {@link #DS_RUN_JOB_FAMILY}, the family of DS run jobs.
	 * 
	 * @param family
	 *            the job family identifier
	 * @return <code>true</code> for DS run job family, <code>false</code>
	 *         otherwise
	 */
	@Override
	public boolean belongsTo(Object family) {
		return DS_RUN_JOB_FAMILY.equals(family);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		currentProblem = null;
		monitor.beginTask("DynamicSpotter Diagnosis '" + project.getName() + "'", IProgressMonitor.UNKNOWN);
		ServiceClientWrapper client = Activator.getDefault().getClient(project.getName());

		while (client.isRunning(true)) {
			if (monitor.isCanceled()) {
				return onUserCancelledJob(null);
			}
			Long currentJobId = client.getCurrentJobId();
			// check if the job is still being processed
			if (currentJobId != null && currentJobId == jobId) {
				updateCurrentRun(client, monitor);
				try {
					Thread.sleep(SLEEP_TIME_MILLIS);
				} catch (InterruptedException e) {
					return onUserCancelledJob(e);
				}
			} else {
				// assume our job is due
				break;
			}
		}

		monitor.done();
		boolean isConnectionIssue = client.isConnectionIssue();
		Exception runException = client.getLastRunException(true);
		isConnectionIssue |= client.isConnectionIssue();

		if (isConnectionIssue) {
			DialogUtils.openWarning(RunHandler.DIALOG_TITLE, MSG_LOST_CONNECTION);
			return new Status(Status.OK, Activator.PLUGIN_ID, Status.OK, MSG_LOST_CONNECTION, null);
		}

		onFinishedJob(runException);

		// keep the finished job in the progress view only if
		// it is not running in the progress dialog
		Boolean inDialog = (Boolean) getProperty(IProgressConstants.PROPERTY_IN_DIALOG);
		if (inDialog != null && !inDialog.booleanValue()) {
			setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Creates a progress string representing the progress of the given problem.
	 * 
	 * @param spotterProgress
	 *            the overall spotter progress to use for the lookup
	 * @param problemId
	 *            the id of the concrete problem to create the progress for
	 * @param omitProblemName
	 *            whether to omit the problem name in the resulting string
	 * @return a progress string
	 */
	public static String createProgressString(SpotterProgress spotterProgress, String problemId, boolean omitProblemName) {
		if (spotterProgress == null || problemId == null) {
			return null;
		}

		DiagnosisProgress diagProgress = spotterProgress.getProgress(problemId);
		String estimation = String.format("%.1f", HUNDRED_PERCENT * diagProgress.getEstimatedProgress());
		String duration = String.valueOf(diagProgress.getEstimatedRemainingDuration());

		String problemName = "";
		if (!omitProblemName) {
			String keyHashTag = createKeyHashTag(problemId);
			problemName = diagProgress.getName() + "-" + keyHashTag;
		}

		String estimates = ": ";

		switch (diagProgress.getStatus()) {
		case PENDING:
			break;
		case DETECTED:
		case NOT_DETECTED:
			estimates = " (100.0 %, 0s remaining): ";
			break;
		default:
			estimates = " (" + estimation + " %, " + duration + "s remaining): ";
			break;
		}

		String progressString = problemName + estimates + diagProgress.getStatus();
		return progressString;
	}

	private static String createKeyHashTag(String key) {
		if (key.length() < KEY_HASH_LENGTH) {
			return key;
		} else {
			return key.substring(0, KEY_HASH_LENGTH);
		}
	}

	private void updateCurrentRun(ServiceClientWrapper client, IProgressMonitor monitor) {
		SpotterProgress spotterProgress = client.getCurrentProgressReport();
		if (spotterProgress == null || spotterProgress.getProblemProgressMapping() == null) {
			return;
		}

		Map<String, DiagnosisProgress> progressAll = spotterProgress.getProblemProgressMapping();
		if (progressAll.isEmpty()) {
			return;
		}

		currentProblem = spotterProgress.getCurrentProblem();
		if (currentProblem != null && progressAll.containsKey(currentProblem)) {
			monitor.setTaskName(createProgressString(spotterProgress, currentProblem, false));
		}
	}

	private IStatus onUserCancelledJob(Exception exception) {
		if (silentCancel) {
			return Status.OK_STATUS;
		}

		DialogUtils.openInformation(RunHandler.DIALOG_TITLE, MSG_CANCELLED);
		return new Status(Status.CANCEL, Activator.PLUGIN_ID, MSG_CANCELLED, exception);
	}

	private void onFinishedJob(final Exception runException) {
		final Activator activator = Activator.getDefault();
		final Display display = PlatformUI.getWorkbench().getDisplay();

		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				Map<String, SpotterProjectResults> results = activator.getProjectHistoryElements();
				SpotterProjectResults projectResultsNode = results.get(project.getName());
				projectResultsNode.refreshChildren();
				CommonViewer viewer = activator.getNavigatorViewer();
				if (!viewer.isBusy()) {
					viewer.refresh(projectResultsNode);
					viewer.expandToLevel(projectResultsNode, 1);
				}

				if (runException == null) {
					DialogUtils.openAsyncInformation(RunHandler.DIALOG_TITLE, MSG_RUN_FINISH);
				} else {
					String exceptionMsg = runException.getLocalizedMessage();
					if (exceptionMsg == null || exceptionMsg.isEmpty()) {
						exceptionMsg = runException.getClass().getSimpleName();
					}
					String msg = DialogUtils.appendCause(MSG_RUN_ERROR, exceptionMsg, true);
					DialogUtils.openWarning(RunHandler.DIALOG_TITLE, msg);
				}

				revealResults();
			}
		});
	}

	private void revealResults() {
		Activator activator = Activator.getDefault();
		Map<String, SpotterProjectResults> results = activator.getProjectHistoryElements();
		SpotterProjectResults projectResultsNode = results.get(project.getName());
		if (projectResultsNode != null) {
			SpotterProjectRunResult runNode = projectResultsNode.getRunResultForJobId(jobId);
			if (runNode != null) {
				CommonViewer viewer = activator.getNavigatorViewer();
				viewer.getControl().setFocus();
				viewer.setSelection(new StructuredSelection(runNode), true);
			}
		}
	}

}
