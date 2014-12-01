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
package org.spotter.core.config.interpretation;

import java.util.LinkedList;

import org.spotter.core.ProgressManager;
import org.spotter.core.result.ResultBlackboard;
import org.spotter.shared.result.model.SpotterResult;
import org.spotter.shared.status.DiagnosisStatus;

/**
 * The hierarchy model interpreter traverses the performance problem hierarchy
 * guiding the search process at the very top level.
 * 
 * @author Alexander Wert
 * 
 */
public class HierarchyModelInterpreter {

	/** performance problems backlog */
	private LinkedList<PerformanceProblem> problemsToBeExamined;

	/** performance problem currently under examination */
	private PerformanceProblem currentProblem;

	/**
	 * Constructor.
	 * 
	 * @param rootProblem
	 *            an instance of {@link PerformanceProblem} which is the root
	 *            node of the performance problem hierarchy to be interpreted.
	 */
	public HierarchyModelInterpreter(PerformanceProblem rootProblem) {
		problemsToBeExamined = new LinkedList<PerformanceProblem>();
		problemsToBeExamined.offerLast(rootProblem);
	}

	/**
	 * Traverses to the next {@link PerformanceProblem} to be examined and
	 * returns the corresponding performance problem.
	 * 
	 * @return Returns the next performance problem to be examined. If there are
	 *         no performance problems to be examined this method returns
	 *         {@code null}.
	 */
	public PerformanceProblem next() {
		addChildrenOfDetectedProblem();
		if (problemsToBeExamined.isEmpty()) {
			return null;
		}
		PerformanceProblem pp = problemsToBeExamined.pollFirst();
		if (!pp.isDetectable()) {
			currentProblem = null;
			for (PerformanceProblem child : pp.getChildren()) {
				problemsToBeExamined.offerLast(child);
			}
			return next();
		} else {
			currentProblem = pp;

			return currentProblem;
		}
	}

	/**
	 * 
	 * @return Returns the current performance problem under investigation.
	 */
	public PerformanceProblem getCurrentProblem() {
		return currentProblem;
	}

	/**
	 * If the current problem under examination has been detected, add its
	 * children to the list of problems to be examined. If it has not been
	 * detected, mark its children recursively as not detected as well.
	 */
	private void addChildrenOfDetectedProblem() {

		if (currentProblem != null) {
			if (ResultBlackboard.getInstance().hasBeenDetected(currentProblem)) {
				for (PerformanceProblem child : currentProblem.getChildren()) {
					problemsToBeExamined.offerLast(child);
				}
			} else {
				addNotDetectedChildrenRecursively(currentProblem);
			}
		}
	}

	/**
	 * Marks all children of parent recursively as not detected.
	 * 
	 * @param parent
	 *            the parent problem to start with
	 */
	private void addNotDetectedChildrenRecursively(PerformanceProblem parent) {
		for (PerformanceProblem child : parent.getChildren()) {

			ProgressManager.getInstance().updateProgressStatus(child.getUniqueId(), DiagnosisStatus.NOT_DETECTED);
			ProgressManager.getInstance().updateProgress(child.getUniqueId(), 1, 0);
			SpotterResult result = new SpotterResult();
			result.setDetected(false);
			ResultBlackboard.getInstance().putResult(child, result);

			addNotDetectedChildrenRecursively(child);

		}
	}

}
