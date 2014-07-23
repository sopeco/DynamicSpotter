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

import org.lpe.common.config.GlobalConfiguration;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.status.DiagnosisProgress;
import org.spotter.shared.status.DiagnosisStatus;

/**
 * The ProgressUpdater periodically updates the progress of the detection
 * controller in action.
 * 
 * @author Alexander Wert
 * 
 */
public class ProgressUpdater implements Runnable {

	private static final int SECOND = 1000;
	private volatile boolean run = false;
	private AbstractDetectionController controller;
	private long estimatedDuration = 0;
	private long additionalDuration = 0;

	@Override
	public void run() {
		run = true;
		calculateInitialEstimatedDuration();
		while (run) {
			synchronized (this) {
				if (controller != null) {
					updateEstimatedProgress();
				}
			}
			try {
				Thread.sleep(SECOND);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Stops execution of the updater.
	 */
	public synchronized void stop() {
		run = false;
	}

	/**
	 * Sets current detection controller.
	 * 
	 * @param controller
	 *            controller in action
	 */
	public synchronized void setController(AbstractDetectionController controller) {
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
			updateProgress(controller.getProvider().getName(), (double) (elapsedTime / getEstimatedOverallDuration()),
					getEstimatedOverallDuration() - elapsedTime);
		}

	}

	/**
	 * Updates progress data.
	 * 
	 * @param problemName
	 *            problem name specifying the corresponding diagnosis step
	 * @param status
	 *            progress status
	 * @param estimatedProgress
	 *            estimated progress in percent
	 * @param estimatedRemainingDuration
	 *            estimated remaining duration in seconds
	 * @param currentProgressMessage
	 *            progress message
	 */
	public void updateProgress(String problemName, DiagnosisStatus status, double estimatedProgress,
			long estimatedRemainingDuration, String currentProgressMessage) {
		if (Spotter.getInstance().getProgress().getProblemProgressMapping().containsKey(problemName)) {
			Spotter.getInstance().getProgress().getProblemProgressMapping().get(problemName)
					.setCurrentProgressMessage(currentProgressMessage);
			Spotter.getInstance().getProgress().getProblemProgressMapping().get(problemName)
					.setEstimatedProgress(estimatedProgress);
			Spotter.getInstance().getProgress().getProblemProgressMapping().get(problemName)
					.setEstimatedRemainingDuration(estimatedRemainingDuration);
			Spotter.getInstance().getProgress().getProblemProgressMapping().get(problemName).setStatus(status);
		} else {
			DiagnosisProgress progress = new DiagnosisProgress(status, estimatedProgress, estimatedRemainingDuration,
					currentProgressMessage);
			Spotter.getInstance().getProgress().getProblemProgressMapping().put(problemName, progress);
		}

	}

	/**
	 * Update progress message.
	 * 
	 * @param problemName
	 *            problem name specifying the corresponding diagnosis step
	 * @param currentProgressMessage
	 *            new progress message
	 */
	public void updateProgressMessage(String problemName, String currentProgressMessage) {
		if (Spotter.getInstance().getProgress().getProblemProgressMapping().containsKey(problemName)) {
			Spotter.getInstance().getProgress().getProblemProgressMapping().get(problemName)
					.setCurrentProgressMessage(currentProgressMessage);
		}
	}

	/**
	 * Updates the progress.
	 * 
	 * @param problemName
	 *            problem name specifying the corresponding diagnosis step
	 * @param estimatedProgress
	 *            estimated progress in percent
	 * @param estimatedRemainingDuration
	 *            estimated remaining duration in seconds
	 */
	public void updateProgress(String problemName, double estimatedProgress, long estimatedRemainingDuration) {
		if (Spotter.getInstance().getProgress().getProblemProgressMapping().containsKey(problemName)) {
			Spotter.getInstance().getProgress().getProblemProgressMapping().get(problemName)
					.setEstimatedProgress(estimatedProgress);
			Spotter.getInstance().getProgress().getProblemProgressMapping().get(problemName)
					.setEstimatedRemainingDuration(estimatedRemainingDuration);
		}
	}

	/**
	 * Updates the progress status.
	 * 
	 * @param problemName
	 *            problem name specifying the corresponding diagnosis step
	 * @param status
	 *            new status
	 */
	public void updateProgressStatus(String problemName, DiagnosisStatus status) {
		if (Spotter.getInstance().getProgress().getProblemProgressMapping().containsKey(problemName)) {
			Spotter.getInstance().getProgress().getProblemProgressMapping().get(problemName).setStatus(status);
		} else {
			DiagnosisProgress progress = new DiagnosisProgress(status, 0.0, 0L, "");
			Spotter.getInstance().getProgress().getProblemProgressMapping().put(problemName, progress);
		}
	}

}
