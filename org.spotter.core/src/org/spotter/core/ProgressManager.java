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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.system.LpeSystemUtils;
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

	private static final int SECOND = 1000;

	private static ProgressManager instance;

	/**
	 * Get singleton instance.
	 * 
	 * @return singleton instance
	 */
	public static ProgressManager getInstance() {
		if (instance == null) {
			instance = new ProgressManager();
		}

		return instance;
	}

	private volatile boolean run = false;
	private IDetectionController controller;
	private long estimatedDuration = 0;
	private long additionalDuration = 0;
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
	 * Stops execution of the updater.
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
		int numExperiments = controller.getNumOfExperiments();
		long numUsers = GlobalConfiguration.getInstance().getPropertyAsLong(ConfigKeys.WORKLOAD_MAXUSERS, 1L);
		estimatedDuration = calculateExperimentDuration(numUsers) * numExperiments;
		initialEstimateConducted = true;
	}

	private long calculateExperimentDuration(long numUsers) {
		long rampUpUsersPerInterval = GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL, 0L);

		long coolDownUsersPerInterval = GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL, 0L);

		long rampUpInterval = GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH, 0L);

		long coolDownInterval = GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH, 0L);

		long stablePhase = GlobalConfiguration.getInstance().getPropertyAsLong(ConfigKeys.EXPERIMENT_DURATION, 0L);

		long rampUp = 0;
		if (rampUpUsersPerInterval != 0) {
			rampUp = (numUsers / rampUpUsersPerInterval) * rampUpInterval;
		}

		long coolDown = 0;
		if (coolDownUsersPerInterval != 0) {
			coolDown = (numUsers / coolDownUsersPerInterval) * coolDownInterval;
		}

		return rampUp + stablePhase + coolDown;

	}

	/**
	 * Updates the current progress of this controller.
	 */
	public void updateEstimatedProgress() {
		long elapsedTime = (System.currentTimeMillis() - GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.PPD_RUN_TIMESTAMP, 0L))
				/ SECOND;

		long currentEstimatedOverallDuration = getEstimatedOverallDuration();

		// as the estimated overall duration might not have been calculated yet
		// and return default

		// value 0, it must be checked to be greater 0
		if (currentEstimatedOverallDuration > 0) {
			updateProgress(controller.getProblemId(), (double) elapsedTime / (double) getEstimatedOverallDuration(),
					getEstimatedOverallDuration() - elapsedTime);
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
			DiagnosisProgress progress = new DiagnosisProgress(status, 0.0, 0L, "");
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
			DiagnosisProgress progress = new DiagnosisProgress(status, 0.0, 0L, currentProgressMessage);
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
