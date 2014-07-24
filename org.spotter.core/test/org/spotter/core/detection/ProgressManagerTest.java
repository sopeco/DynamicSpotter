package org.spotter.core.detection;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.lpe.common.config.GlobalConfiguration;
import org.spotter.core.ProgressManager;
import org.spotter.core.config.interpretation.PerformanceProblem;
import org.spotter.core.test.dummies.detection.MockDetection;
import org.spotter.core.test.dummies.detection.MockDetectionExtension;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.status.DiagnosisStatus;
import org.spotter.shared.status.SpotterProgress;

public class ProgressManagerTest {

	private static IDetectionController detectionController;
	private static PerformanceProblem problem;
	private static final String PROBLEM_ID = "abcdefgh";
	private static final String PROBLEM_NAME = "problemName";

	private static final int EXP_DURATION = 1;
	private static final int NUM_USRS = 10;

	private static void initGlobalConfigs() {
		String dir = System.getProperty("user.dir");
		Properties properties = new Properties();
		properties.setProperty("org.lpe.common.extension.appRootDir", dir);
		properties.setProperty("org.spotter.conf.pluginDirNames", "plugins");
		properties.setProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(EXP_DURATION));
		properties.setProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL, "1");
		properties.setProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(NUM_USRS));
		GlobalConfiguration.reinitialize(properties);
	}

	@BeforeClass
	public static void init() {
		GlobalConfiguration.initialize(new Properties());
		initGlobalConfigs();
		MockDetectionExtension mdExtension = new MockDetectionExtension();
		detectionController = mdExtension.createExtensionArtifact();
		problem = new PerformanceProblem(PROBLEM_ID);
		problem.setProblemName(PROBLEM_NAME);
		problem.setDetectionController(detectionController);
		ProgressManager.getInstance().reset();
	}

	@Test
	public void testProgress() throws InterruptedException {
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.PPD_RUN_TIMESTAMP,
				String.valueOf(System.currentTimeMillis()));

		Assert.assertEquals(0L, ProgressManager.getInstance().getEstimatedOverallDuration());
		SpotterProgress progress = ProgressManager.getInstance().getSpotterProgress();
		Assert.assertNotNull(progress);
		Assert.assertTrue(progress.getProblemProgressMapping().isEmpty());
		Assert.assertNotNull(progress.getProgress(PROBLEM_ID));
		Assert.assertEquals(DiagnosisStatus.PENDING, progress.getProgress(PROBLEM_ID).getStatus());
		Assert.assertEquals(0L, progress.getProgress(PROBLEM_ID).getEstimatedRemainingDuration());
		Assert.assertEquals("", progress.getProgress(PROBLEM_ID).getCurrentProgressMessage());
		Assert.assertEquals(0.0, progress.getProgress(PROBLEM_ID).getEstimatedProgress(), 0.001);

		ProgressManager.getInstance().setSamplingDelay(20);
		ProgressManager.getInstance().start();

		Assert.assertEquals(0L, ProgressManager.getInstance().getEstimatedOverallDuration());
		Assert.assertNotNull(progress);
		Assert.assertTrue(progress.getProblemProgressMapping().isEmpty());
		Assert.assertNotNull(progress.getProgress(PROBLEM_ID));
		Assert.assertEquals(DiagnosisStatus.PENDING, progress.getProgress(PROBLEM_ID).getStatus());
		Assert.assertEquals(0L, progress.getProgress(PROBLEM_ID).getEstimatedRemainingDuration());
		Assert.assertEquals("", progress.getProgress(PROBLEM_ID).getCurrentProgressMessage());
		Assert.assertEquals(0.0, progress.getProgress(PROBLEM_ID).getEstimatedProgress(), 0.001);

		ProgressManager.getInstance().setController(detectionController);
		ProgressManager.getInstance().updateProgressStatus(PROBLEM_ID, DiagnosisStatus.EXPERIMENTING_STABLE_PHASE);
		long sleepTime = 200;
		Thread.sleep(sleepTime);

		long overallDuration = ProgressManager.getInstance().getEstimatedOverallDuration();
		Assert.assertTrue(overallDuration >= EXP_DURATION * MockDetection.NUM_EXPERIMENTS);
		Assert.assertTrue(ProgressManager.getInstance().getEstimatedOverallDuration() <= EXP_DURATION
				* MockDetection.NUM_EXPERIMENTS + 2 * NUM_USRS * MockDetection.NUM_EXPERIMENTS);
		Assert.assertNotNull(progress);
		Assert.assertFalse(progress.getProblemProgressMapping().isEmpty());
		Assert.assertNotNull(progress.getProgress(PROBLEM_ID));
		Assert.assertEquals(DiagnosisStatus.EXPERIMENTING_STABLE_PHASE, progress.getProgress(PROBLEM_ID).getStatus());
		long remainingDuration = progress.getProgress(PROBLEM_ID).getEstimatedRemainingDuration();
		Assert.assertTrue(overallDuration >= remainingDuration);
		Assert.assertTrue(overallDuration - Math.ceil((double) sleepTime / 1000.0) <= remainingDuration);
		Assert.assertEquals("", progress.getProgress(PROBLEM_ID).getCurrentProgressMessage());
		Assert.assertTrue(((double) (overallDuration - remainingDuration) / (double) overallDuration) <= progress
				.getProgress(PROBLEM_ID).getEstimatedProgress());

		String newMessage = "a message text";
		ProgressManager.getInstance().updateProgressStatus(PROBLEM_ID, DiagnosisStatus.ANALYSING, newMessage);

		Assert.assertEquals(DiagnosisStatus.ANALYSING, progress.getProgress(PROBLEM_ID).getStatus());
		Assert.assertEquals(newMessage, progress.getProgress(PROBLEM_ID).getCurrentProgressMessage());

		String newMessage2 = "another text";
		ProgressManager.getInstance().updateProgressMessage(PROBLEM_ID, newMessage2);

		Assert.assertEquals(DiagnosisStatus.ANALYSING, progress.getProgress(PROBLEM_ID).getStatus());
		Assert.assertEquals(newMessage2, progress.getProgress(PROBLEM_ID).getCurrentProgressMessage());

		ProgressManager.getInstance().reset();
	}
}
