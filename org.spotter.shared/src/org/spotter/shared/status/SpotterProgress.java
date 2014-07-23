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
