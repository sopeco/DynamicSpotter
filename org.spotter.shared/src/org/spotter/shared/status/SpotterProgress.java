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
package org.spotter.shared.status;

import java.util.HashMap;
import java.util.Map;

/**
 * Progress of a Dynamic Spotter run/job.
 * 
 * @author Alexander Wert
 *
 */
public class SpotterProgress {
	private Map<String, DiagnosisProgress> problemProgressMapping;

	/**
	 * Constructor.
	 */
	public SpotterProgress() {
		problemProgressMapping = new HashMap<>();
	}

	/**
	 * Updates progress data.
	 * @param problemName problem name specifying the corresponding diagnosis step
	 * @param status progress status
	 * @param estimatedProgress estimated progress in percent
	 * @param estimatedRemainingDuration estimated remaining duration in seconds
	 * @param currentProgressMessage progress message
	 */
	public void updateProgress(String problemName, DiagnosisStatus status, double estimatedProgress,
			long estimatedRemainingDuration, String currentProgressMessage) {
		if (problemProgressMapping.containsKey(problemName)) {
			problemProgressMapping.get(problemName).setCurrentProgressMessage(currentProgressMessage);
			problemProgressMapping.get(problemName).setEstimatedProgress(estimatedProgress);
			problemProgressMapping.get(problemName).setEstimatedRemainingDuration(estimatedRemainingDuration);
			problemProgressMapping.get(problemName).setStatus(status);
		} else {
			DiagnosisProgress progress = new DiagnosisProgress(status, estimatedProgress, estimatedRemainingDuration,
					currentProgressMessage);
			problemProgressMapping.put(problemName, progress);
		}

	}

	/**
	 * Update progress message.
	 * @param problemName problem name specifying the corresponding diagnosis step
	 * @param currentProgressMessage new progress message
	 */
	public void updateProgressMessage(String problemName, String currentProgressMessage) {
		if (problemProgressMapping.containsKey(problemName)) {
			problemProgressMapping.get(problemName).setCurrentProgressMessage(currentProgressMessage);
		}
	}

	/**
	 * Updates the progress.
	 * @param problemName problem name specifying the corresponding diagnosis step
	 * @param estimatedProgress estimated progress in percent
	 * @param estimatedRemainingDuration estimated remaining duration in seconds
	 */
	public void updateProgress(String problemName, double estimatedProgress, long estimatedRemainingDuration) {
		if (problemProgressMapping.containsKey(problemName)) {
			problemProgressMapping.get(problemName).setEstimatedProgress(estimatedProgress);
			problemProgressMapping.get(problemName).setEstimatedRemainingDuration(estimatedRemainingDuration);
		}
	}

	/**
	 * Updates the progress status.
	 * @param problemName problem name specifying the corresponding diagnosis step
	 * @param status new status
	 */
	public void updateProgressStatus(String problemName, DiagnosisStatus status) {
		if (problemProgressMapping.containsKey(problemName)) {
			problemProgressMapping.get(problemName).setStatus(status);
		} else {
			DiagnosisProgress progress = new DiagnosisProgress(status, 0.0, 0L, "");
			problemProgressMapping.put(problemName, progress);
		}
	}

	/**
	 * Returns the current diagnosis progress.
	 * @param problemName problem name specifying the corresponding diagnosis step
	 * @return the current progress
	 */
	public DiagnosisProgress getProgress(String problemName) {
		if (problemProgressMapping.containsKey(problemName)) {
			return problemProgressMapping.get(problemName);
		} else {
			return new DiagnosisProgress(DiagnosisStatus.PENDING, 0.0, 0L, "");
		}
	}

	/**
	 * @return the problemProgressMapping
	 */
	public Map<String, DiagnosisProgress> getProblemProgressMapping() {
		return problemProgressMapping;
	}

	/**
	 * @param problemProgressMapping
	 *            the problemProgressMapping to set
	 */
	public void setProblemProgressMapping(Map<String, DiagnosisProgress> problemProgressMapping) {
		this.problemProgressMapping = problemProgressMapping;
	}

}
