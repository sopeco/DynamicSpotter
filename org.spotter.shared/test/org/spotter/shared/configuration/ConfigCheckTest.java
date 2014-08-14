package org.spotter.shared.configuration;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.LpeFileUtils;

public class ConfigCheckTest {

	private static File tempDir;
	private static String newFileName;
	private static String hierarchyFileName;
	private static String mEnvFileName;
	private static String wrongFileName;
	private static int maxUsers = 15;
	private static int rampUpInterval = 16;
	private static int rampUpUsers = 17;
	private static int coolDownInterval = 18;
	private static int coolDownUsers = 19;
	private static int duration = 20;

	@BeforeClass
	public static void initialize() throws IOException {
		GlobalConfiguration.initialize(new Properties());
		createTempDir();
	}

	private static void createTempDir() throws IOException {
		tempDir = new File("tempJUnit");
		if (tempDir.exists()) {
			LpeFileUtils.removeDir(tempDir.getAbsolutePath());
		}
		LpeFileUtils.createDir(tempDir.getAbsolutePath());
		newFileName = tempDir.getAbsolutePath() + System.getProperty("file.separator") + "newFile.txt";
		File newFile = new File(newFileName);
		newFile.createNewFile();

		hierarchyFileName = tempDir.getAbsolutePath() + System.getProperty("file.separator") + "hierarchy.xml";
		newFile = new File(hierarchyFileName);
		newFile.createNewFile();

		mEnvFileName = tempDir.getAbsolutePath() + System.getProperty("file.separator") + "menv.xml";
		newFile = new File(mEnvFileName);
		newFile.createNewFile();

		wrongFileName = tempDir.getAbsolutePath() + System.getProperty("file.separator") + "wrong.xml";
	}

	/**
	 * Removes the temp dir.
	 * 
	 * @throws IOException
	 *             removal of temp dir fails
	 */
	@AfterClass
	public static void cleanUp() throws IOException {
		LpeFileUtils.removeDir(tempDir.getAbsolutePath());
	}

	@Before
	public void reinitializedGlobalConf() {
		GlobalConfiguration.reinitialize(new Properties());
	}

	@Test
	public void testCorrectConfig() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

		Assert.assertEquals(
				GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR),
				tempDir.getAbsolutePath() + System.getProperty("file.separator") + "testRun"
						+ System.getProperty("file.separator"));
		Assert.assertFalse(GlobalConfiguration.getInstance().getPropertyAsBoolean(ConfigKeys.OMIT_EXPERIMENTS));

	}

	@Test
	public void testCorrectConfig2() {
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		long timestamp = System.currentTimeMillis();
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.PPD_RUN_TIMESTAMP, String.valueOf(timestamp));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

		Assert.assertTrue(GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR)
				.contains(tempDir.getAbsolutePath()));
		Assert.assertFalse(GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR)
				.equals(tempDir.getAbsolutePath()));
		Assert.assertFalse(GlobalConfiguration.getInstance().getPropertyAsBoolean(ConfigKeys.OMIT_EXPERIMENTS));

	}

	@Test
	public void testCorrectConfigWithOmitExperimt() {
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		long timestamp = System.currentTimeMillis();
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.PPD_RUN_TIMESTAMP, String.valueOf(timestamp));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.OMIT_EXPERIMENTS, String.valueOf("true"));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.DUMMY_EXPERIMENT_DATA, tempDir.getAbsolutePath());

		ConfigCheck.checkConfiguration();

		Assert.assertTrue(GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR)
				.contains(tempDir.getAbsolutePath()));
		Assert.assertFalse(GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR)
				.equals(tempDir.getAbsolutePath()));
		Assert.assertTrue(GlobalConfiguration.getInstance().getPropertyAsBoolean(ConfigKeys.OMIT_EXPERIMENTS));

	}

	@Test(expected = IllegalStateException.class)
	public void testNoResultDir() {
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidResultDir() {
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, newFileName);

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();
	}

	@Test(expected = IllegalStateException.class)
	public void testNoHierarchyFile() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

	}

	@Test(expected = IllegalStateException.class)
	public void testWrongHierarchyFile() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, wrongFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();
	}

	@Test(expected = IllegalStateException.class)
	public void testNoMeasEnvFile() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

	}

	@Test(expected = IllegalStateException.class)
	public void testWrongMeasEnvFile() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, wrongFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();
	}

	@Test(expected = IllegalStateException.class)
	public void testNoMaxUsersConfig() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

	}

	@Test(expected = IllegalStateException.class)
	public void testWrongMaxUsersConfig() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, "A");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

	}

	@Test(expected = IllegalStateException.class)
	public void testNoRampUpIntervalConfig() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

	}

	@Test(expected = IllegalStateException.class)
	public void testWrongRampUpIntervalConfig() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH, "A");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

	}

	@Test(expected = IllegalStateException.class)
	public void testNoRampUpUsersConfig() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

	}

	@Test(expected = IllegalStateException.class)
	public void testWrongRampUpUsersConfig() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL, "A");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

	}

	@Test(expected = IllegalStateException.class)
	public void testNoCoolDownIntervalConfig() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

	}

	@Test(expected = IllegalStateException.class)
	public void testWrongCoolDownIntervalConfig() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH, "A");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

	}

	@Test(expected = IllegalStateException.class)
	public void testNoCoolDownUsersConfig() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

	}

	@Test(expected = IllegalStateException.class)
	public void testWrongCoolDownUsersConfig() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL, "A");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));

		ConfigCheck.checkConfiguration();

	}

	@Test(expected = IllegalStateException.class)
	public void testNoDurationConfig() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));

		ConfigCheck.checkConfiguration();

	}

	@Test(expected = IllegalStateException.class)
	public void testWrongDurationConfig() {

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.SPOTTER_RUN_NAME, "testRun");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, "A");

		ConfigCheck.checkConfiguration();

	}
	
	@Test(expected = IllegalStateException.class)
	public void testWrongOmitExperimt() {
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		long timestamp = System.currentTimeMillis();
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.PPD_RUN_TIMESTAMP, String.valueOf(timestamp));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.OMIT_EXPERIMENTS, String.valueOf("true"));
		
		ConfigCheck.checkConfiguration();

		Assert.assertTrue(GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR)
				.contains(tempDir.getAbsolutePath()));
		Assert.assertFalse(GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR)
				.equals(tempDir.getAbsolutePath()));
		Assert.assertTrue(GlobalConfiguration.getInstance().getPropertyAsBoolean(ConfigKeys.OMIT_EXPERIMENTS));

	}
	
	@Test(expected = IllegalStateException.class)
	public void testWrongOmitExperimt2() {
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, tempDir.getAbsolutePath());
		long timestamp = System.currentTimeMillis();
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.PPD_RUN_TIMESTAMP, String.valueOf(timestamp));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, hierarchyFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, mEnvFileName);
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.WORKLOAD_MAXUSERS, String.valueOf(maxUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				String.valueOf(rampUpInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL,
				String.valueOf(rampUpUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				String.valueOf(coolDownInterval));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL,
				String.valueOf(coolDownUsers));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(duration));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.OMIT_EXPERIMENTS, String.valueOf("true"));
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.DUMMY_EXPERIMENT_DATA, newFileName);

		ConfigCheck.checkConfiguration();

		Assert.assertTrue(GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR)
				.contains(tempDir.getAbsolutePath()));
		Assert.assertFalse(GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR)
				.equals(tempDir.getAbsolutePath()));
		Assert.assertTrue(GlobalConfiguration.getInstance().getPropertyAsBoolean(ConfigKeys.OMIT_EXPERIMENTS));

	}
}
