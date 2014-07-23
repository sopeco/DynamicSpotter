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
