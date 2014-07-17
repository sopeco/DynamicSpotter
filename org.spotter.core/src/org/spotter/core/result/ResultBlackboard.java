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
package org.spotter.core.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spotter.core.config.interpretation.PerformanceProblem;
import org.spotter.shared.result.model.SpotterResult;

/**
 * Singleton Blackboard for all PPD results.
 * 
 * @author Alexander Wert
 * 
 */
public class ResultBlackboard {

	private static ResultBlackboard instance;

	/**
	 * 
	 * @return singleton instance
	 */
	public static ResultBlackboard getInstance() {
		if (instance == null) {
			instance = new ResultBlackboard();
		}
		return instance;
	}

	// maps performance problems using their unique id to the corresponding
	// spotter result
	private final Map<String, SpotterResult> results = new HashMap<String, SpotterResult>();
	private final List<PerformanceProblem> knownProblems = new ArrayList<PerformanceProblem>();

	/**
	 * Returns the results as a mapping of performance problems to the
	 * corresponding spotter result.
	 * 
	 * @return returns the results currently on the result blackboard.
	 */
	public Map<String, SpotterResult> getResults() {
		return results;
	}

	/**
	 * Resets the blackboard.
	 */
	public void reset() {
		results.clear();
		knownProblems.clear();
	}

	/**
	 * updates a diagnosis result for the passed problem.
	 * 
	 * @param problem
	 *            performance problem for which the result should be updated
	 * @param result
	 *            the new result
	 */
	public void putResult(PerformanceProblem problem, SpotterResult result) {
		knownProblems.add(problem);
		results.put(problem.getUniqueId(), result);
	}

	/**
	 * Returns the diagnosis result for the given problem.
	 * 
	 * @param problemUniqueId
	 *            performance problem for which the diagnosis result should be
	 *            returned
	 * @return the diagnosis result for the given problem.
	 */
	public SpotterResult getResult(String problemUniqueId) {
		return results.get(problemUniqueId);
	}

	/**
	 * Indicates whether the given problem has been detected or not.
	 * 
	 * @param problem
	 *            performance problem of interest
	 * @return true if given problem has been detected, otherwise false
	 */
	public boolean hasBeenDetected(PerformanceProblem problem) {
		SpotterResult result = results.get(problem.getUniqueId());
		if (result == null) {
			throw new IllegalArgumentException("No result for problem " + problem.getProblemName()
					+ " has been recorded!");
		}

		return result.isDetected();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("#####################################");
		builder.append(System.getProperty("line.separator"));
		builder.append("########  PPD Results  ##############");
		builder.append(System.getProperty("line.separator"));
		builder.append("#####################################");

		builder.append(System.getProperty("line.separator"));
		builder.append(System.getProperty("line.separator"));
		for (PerformanceProblem problem : knownProblems) {
			builder.append("############################################################################################");
			builder.append(System.getProperty("line.separator"));
			builder.append("### performance problem under investigation: ");
			builder.append(problem.getProblemName());
			builder.append(System.getProperty("line.separator"));
			SpotterResult result = results.get(problem.getUniqueId());
			if (result.isDetected()) {
				builder.append("    # DETECTED ! ");
			} else {
				builder.append("    # Problem not detected.");
			}
			builder.append(System.getProperty("line.separator"));
			builder.append("--------------------------------------------------------------------------------------------");
			builder.append(System.getProperty("line.separator"));
			builder.append(result.getMessage());
			builder.append(System.getProperty("line.separator"));
			builder.append(System.getProperty("line.separator"));
			builder.append(System.getProperty("line.separator"));
		}
		return builder.toString();
	}

}
