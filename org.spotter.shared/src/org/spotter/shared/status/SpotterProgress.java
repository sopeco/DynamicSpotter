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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Progress of a Dynamic Spotter run/job.
 * 
 * @author Alexander Wert
 * 
 */
public class SpotterProgress {
	private Map<String, DiagnosisProgress> problemProgressMapping;
	private String currentProblem;

	/**
	 * Constructor.
	 */
	public SpotterProgress() {
		problemProgressMapping = new ConcurrentHashMap<>();
	}

	/**
	 * Returns the current diagnosis progress for the given problem.
	 * 
	 * @param problemId
	 *            problem id specifying the corresponding diagnosis step
	 * @return the current progress for the given problem
	 */
	public DiagnosisProgress getProgress(String problemId) {
		if (problemProgressMapping.containsKey(problemId)) {
			return problemProgressMapping.get(problemId);
		} else {
			return new DiagnosisProgress("", DiagnosisStatus.PENDING, 0.0, 0L, "");
		}
	}

	/**
	 * Sets the problem that is currently under investigation.
	 * 
	 * @param problemId
	 *            the current problem id
	 */
	public synchronized void setCurrentProblem(String problemId) {
		this.currentProblem = problemId;
	}

	/**
	 * Returns the id of the problem that is currently under investigation.
	 * 
	 * @return the current problem id
	 */
	public synchronized String getCurrentProblem() {
		return currentProblem;
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
