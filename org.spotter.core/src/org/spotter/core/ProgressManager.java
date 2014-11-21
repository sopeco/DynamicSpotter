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
package org.spotter.core;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.LpeStringUtils;
import org.lpe.common.util.system.LpeSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.core.detection.IDetectionController;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.status.DiagnosisProgress;
import org.spotter.shared.status.DiagnosisStatus;
import org.spotter.shared.status.SpotterProgress;

/**
 * The ProgressUpdater periodically updates the progress of the detection
 * controller in action.
 * 
 * @author Alexander Wert
 * 
 */
public final class ProgressManager implements Runnable {
	private static final double _100_PERCENT = 100.0;
	private static final Logger LOGGER = LoggerFactory.getLogger(ProgressManager.class);
	private static final int SECOND = 1000;
	private static final int MIN_NUM_USERS = 1;
	private static final double EPSILON = 0.5;

	private static ProgressManager instance;

	/**
	 * Get singleton instance.
	 * 
	 * @return singleton instance
	 */
	public static synchronized ProgressManager getInstance() {
		if (instance == null) {
			instance = new ProgressManager();
		}

		return instance;
	}

	private volatile boolean run = false;
	private IDetectionController controller;
	private long estimatedDuration = 0;
	private long additionalDuration = 0;
	private long problemInvestigationStartedTimestamp;
	private int samplingDelay = SECOND; // in [ms]
	private Future<?> managingTask;
	private SpotterProgress spotterProgress;
	private boolean initialEstimateConducted = false;

	private ProgressManager() {
		spotterProgress = new SpotterProgress();
	}

	@Override
	public void run() {
		run = true;

		while (run) {
			synchronized (this) {
				if (controller != null) {
					if (!initialEstimateConducted) {
						calculateInitialEstimatedDuration();
					}
					updateEstimatedProgress();
				}
			}
			try {
				Thread.sleep(samplingDelay);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Starts execution of the updater.
	 */
	public synchronized void start() {
		managingTask = LpeSystemUtils.submitTask(this);
	}

	/**
	 * Stops execution of the updater.
	 */
	public synchronized void stop() {
		run = false;
		if (managingTask != null) {
			try {
				managingTask.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Sets current detection controller.
	 * 
	 * @param controller
	 *            controller in action
	 */
	public synchronized void setController(IDetectionController controller) {
		this.controller = controller;
		estimatedDuration = 0;
		additionalDuration = 0;
		initialEstimateConducted = false;
		String currentProblem = controller == null ? null : controller.getProblemId();
		getSpotterProgress().setCurrentProblem(currentProblem);
	}

	/**
	 * Adds additional duration to the estimated duration.
	 * 
	 * @param additionalDuration
	 *            time in [s] to add
	 */
	public void addAdditionalDuration(long additionalDuration) {
		this.additionalDuration += additionalDuration;
	}

	/**
	 * @return the estimatedOverallDuration
	 */
	public long getEstimatedOverallDuration() {
		return estimatedDuration + additionalDuration;
	}

	private void calculateInitialEstimatedDuration() {
		estimatedDuration = controller.getExperimentSeriesDuration();
		problemInvestigationStartedTimestamp = System.currentTimeMillis();
		initialEstimateConducted = true;
	}

	/**
	 * Calculates the duration of a single experiment (including warm-up and
	 * cool-down phases).
	 * 
	 * @param numUsers
	 *            number of users to ramp up
	 * @param stablePhaseDuration
	 *            duration of the stable experimentation phase
	 * @return calculated duration
	 */
	public long calculateExperimentDuration(long numUsers, long stablePhaseDuration) {
		long rampUpUsersPerInterval = GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL, 0L);

		long coolDownUsersPerInterval = GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL, 0L);

		long rampUpInterval = GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH, 0L);

		long coolDownInterval = GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH, 0L);

		long rampUp = 0;
		if (rampUpUsersPerInterval != 0) {
			rampUp = (numUsers / rampUpUsersPerInterval) * rampUpInterval;
		}

		long coolDown = 0;
		if (coolDownUsersPerInterval != 0) {
			coolDown = (numUsers / coolDownUsersPerInterval) * coolDownInterval;
		}

		return rampUp + stablePhaseDuration + coolDown;

	}

	/**
	 * Calculates the experiment series duration for a default experiment series
	 * with the given amount of experimentation steps.
	 * 
	 * @param experimentSteps
	 *            number of experimentation steps
	 * @return duration of a default experiment series
	 */
	public long calculateDefaultExperimentSeriesDuration(int experimentSteps) {
		int maxUsers = Integer.parseInt(LpeStringUtils.getPropertyOrFail(GlobalConfiguration.getInstance()
				.getProperties(), ConfigKeys.WORKLOAD_MAXUSERS, null));

		if (experimentSteps <= 1) {
			return calculateExperimentDuration(maxUsers,
					GlobalConfiguration.getInstance().getPropertyAsLong(ConfigKeys.EXPERIMENT_DURATION, 0L));
		} else {
			double dMinUsers = MIN_NUM_USERS;
			double dMaxUsers = maxUsers;
			double dStep = (dMaxUsers - dMinUsers) / (double) (experimentSteps - 1);

			// if we have the same number of maximum and minimum users, then we
			// have only one experiment run
			if (dStep <= 0.0 + EPSILON) {
				return calculateExperimentDuration(MIN_NUM_USERS,
						GlobalConfiguration.getInstance().getPropertyAsLong(ConfigKeys.EXPERIMENT_DURATION, 0L));
			} else {
				long duration = 0L;
				for (double dUsers = dMinUsers; dUsers <= (dMaxUsers + EPSILON); dUsers += dStep) {
					int numUsers = new Double(dUsers).intValue();

					duration += calculateExperimentDuration(numUsers, GlobalConfiguration.getInstance()
							.getPropertyAsLong(ConfigKeys.EXPERIMENT_DURATION, 0L));
				}
				return duration;

			}
		}

	}

	/**
	 * Updates the current progress of this controller.
	 */
	public void updateEstimatedProgress() {
		long elapsedTime = (System.currentTimeMillis() - problemInvestigationStartedTimestamp) / SECOND;

		long currentEstimatedOverallDuration = getEstimatedOverallDuration();

		// as the estimated overall duration might not have been calculated yet
		// and return default

		// value 0, it must be checked to be greater 0
		if (currentEstimatedOverallDuration > 0) {
			updateProgress(controller.getProblemId(), (double) elapsedTime / (double) currentEstimatedOverallDuration,
					currentEstimatedOverallDuration - elapsedTime);
		}

		if (LOGGER.isInfoEnabled()) {
			DecimalFormat dFormat = new DecimalFormat("#00.0");
			LOGGER.info("Progress - " + controller.getProvider().getName() + " - {}% - remaining: {}s",
					dFormat.format(((double) elapsedTime / (double) currentEstimatedOverallDuration) * _100_PERCENT),
					currentEstimatedOverallDuration - elapsedTime);
		}

	}

	/**
	 * Updates the progress.
	 * 
	 * @param problemId
	 *            problem unique id specifying the corresponding diagnosis step
	 * @param estimatedProgress
	 *            estimated progress in percent
	 * @param estimatedRemainingDuration
	 *            estimated remaining duration in seconds
	 */
	private void updateProgress(String problemId, double estimatedProgress, long estimatedRemainingDuration) {
		if (getSpotterProgress().getProblemProgressMapping().containsKey(problemId)) {
			getSpotterProgress().getProblemProgressMapping().get(problemId).setEstimatedProgress(estimatedProgress);
			getSpotterProgress().getProblemProgressMapping().get(problemId)
					.setEstimatedRemainingDuration(estimatedRemainingDuration);
		}
	}

	/**
	 * Sets the name for the problem with the given id.
	 * 
	 * @param problemId
	 *            problem unique id specifying the corresponding diagnosis step
	 * @param problemName
	 *            name to set
	 */
	public void setProblemName(String problemId, String problemName) {
		if (getSpotterProgress().getProblemProgressMapping().containsKey(problemId)) {
			getSpotterProgress().getProblemProgressMapping().get(problemId).setName(problemName);
		} else {
			DiagnosisProgress progress = new DiagnosisProgress(problemName, null, 0.0, 0L, "");
			getSpotterProgress().getProblemProgressMapping().put(problemId, progress);
		}
	}

	/**
	 * Updates the progress status.
	 * 
	 * @param problemId
	 *            problem unique id specifying the corresponding diagnosis step
	 * @param status
	 *            new status
	 */
	public void updateProgressStatus(String problemId, DiagnosisStatus status) {
		if (getSpotterProgress().getProblemProgressMapping().containsKey(problemId)) {
			getSpotterProgress().getProblemProgressMapping().get(problemId).setStatus(status);
		} else {
			DiagnosisProgress progress = new DiagnosisProgress("", status, 0.0, 0L, "");
			getSpotterProgress().getProblemProgressMapping().put(problemId, progress);
		}
	}

	/**
	 * Update progress message.
	 * 
	 * @param problemId
	 *            problem unique id specifying the corresponding diagnosis step
	 * @param currentProgressMessage
	 *            new progress message
	 */
	public void updateProgressMessage(String problemId, String currentProgressMessage) {
		if (getSpotterProgress().getProblemProgressMapping().containsKey(problemId)) {
			getSpotterProgress().getProblemProgressMapping().get(problemId)
					.setCurrentProgressMessage(currentProgressMessage);
		}
	}

	/**
	 * Updates the progress status.
	 * 
	 * @param problemId
	 *            problem unique id specifying the corresponding diagnosis step
	 * @param currentProgressMessage
	 *            new progress message
	 * @param status
	 *            new status
	 */
	public void updateProgressStatus(String problemId, DiagnosisStatus status, String currentProgressMessage) {
		if (getSpotterProgress().getProblemProgressMapping().containsKey(problemId)) {
			DiagnosisProgress progress = getSpotterProgress().getProblemProgressMapping().get(problemId);
			progress.setStatus(status);
			progress.setCurrentProgressMessage(currentProgressMessage);
		} else {
			DiagnosisProgress progress = new DiagnosisProgress("", status, 0.0, 0L, currentProgressMessage);
			getSpotterProgress().getProblemProgressMapping().put(problemId, progress);
		}
	}

	/**
	 * Returns the spotter job progress.
	 * 
	 * @return SpotterProgress
	 */
	public SpotterProgress getSpotterProgress() {
		return spotterProgress;
	}

	/**
	 * @param samplingDelay
	 *            the samplingDelay to set in [ms]
	 */
	public void setSamplingDelay(int samplingDelay) {
		this.samplingDelay = samplingDelay;
	}

	/**
	 * Resets all properties of the progress manager. If the progress manager is
	 * running, this methods stops the manager.
	 */
	public void reset() {
		if (run) {
			stop();
		}
		run = false;
		setController(null);
		estimatedDuration = 0;
		additionalDuration = 0;
		initialEstimateConducted = false;
		spotterProgress = new SpotterProgress();
	}

}
