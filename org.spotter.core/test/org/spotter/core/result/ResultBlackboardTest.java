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

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.spotter.core.config.interpretation.PerformanceProblem;
import org.spotter.shared.result.model.SpotterResult;

public class ResultBlackboardTest {

	private static PerformanceProblem problemA;
	private static PerformanceProblem problemB;
	private static PerformanceProblem problemC;

	private static SpotterResult resultA;
	private static SpotterResult resultB;
	private static SpotterResult resultC;

	@BeforeClass
	public static void initProblems() {
		problemA = new PerformanceProblem("problemA");
		problemA.setProblemName("problemA");
		resultA = new SpotterResult();
		resultA.setDetected(true);

		problemB = new PerformanceProblem("problemB");
		problemB.setProblemName("problemB");
		resultB = new SpotterResult();
		resultB.setDetected(true);

		problemC = new PerformanceProblem("problemC");
		problemC.setProblemName("problemC");
		resultC = new SpotterResult();
		resultC.setDetected(false);

	}

	@Test
	public void testResultBlackBoard() {
		ResultBlackboard.getInstance().reset();
		ResultBlackboard rbb = ResultBlackboard.getInstance();

		Assert.assertEquals(0, rbb.getResults().size());

		rbb.putResult(problemA, resultA);
		rbb.putResult(problemB, resultB);
		rbb.putResult(problemC, resultC);

		Assert.assertEquals(3, rbb.getResults().size());
		Assert.assertSame(resultB, rbb.getResult("problemB"));
		Assert.assertTrue(rbb.hasBeenDetected(problemA));
		Assert.assertFalse(rbb.hasBeenDetected(problemC));

		String resultString = rbb.toString();
		Assert.assertTrue(resultString.contains("problemA"));
		Assert.assertTrue(resultString.contains("problemB"));
		Assert.assertTrue(resultString.contains("problemC"));
		Assert.assertTrue(resultString.contains("# DETECTED"));
		Assert.assertTrue(resultString.contains("# Problem not detected"));
		
		rbb.reset();
		Assert.assertEquals(0, rbb.getResults().size());
		


	}
}
