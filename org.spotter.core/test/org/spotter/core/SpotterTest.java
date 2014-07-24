package org.spotter.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import junit.framework.Assert;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.LpeFileUtils;
import org.lpe.common.util.LpeStringUtils;
import org.spotter.core.config.interpretation.HierarchyTest;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.configuration.ConfigKeys;

public class SpotterTest {
	private static File tempDir;
	private static String configFile;

	@BeforeClass
	public static void initialize() throws URISyntaxException, IOException, InstrumentationException,
			MeasurementException, WorkloadException {
		GlobalConfiguration.initialize(new Properties());
		String baseDir = creeateTempDir();
		configFile = baseDir + System.getProperty("file.separator") + "test-spotter.conf";
		URL hierarchyUrl = HierarchyTest.class.getResource("/small-hierarchy.xml");
		String hierarchyFile = hierarchyUrl.toURI().getPath();
		URL envUrl = HierarchyTest.class.getResource("/test-env.xml");
		String envFile = envUrl.toURI().getPath();

		createConfigFile(baseDir, hierarchyFile, envFile);
	}

	@AfterClass
	public static void cleanUp() throws IOException {
		// LpeFileUtils.removeDir(tempDir.getAbsolutePath());
	}

	private static String creeateTempDir() throws IOException {
		tempDir = new File("tempJUnit");
		if (tempDir.exists()) {
			LpeFileUtils.removeDir(tempDir.getAbsolutePath());
		}
		LpeFileUtils.createDir(tempDir.getAbsolutePath());
		return tempDir.getAbsolutePath();
	}

	private static void createConfigFile(String baseDir, String hierarchyFile, String envFile) throws IOException {
		String dir = System.getProperty("user.dir");
		Properties properties = new Properties();
		properties.setProperty("org.lpe.common.extension.appRootDir", cleanPath(dir));
		properties.setProperty("org.spotter.conf.pluginDirNames", "plugins");

		properties.setProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, cleanPath(hierarchyFile));
		properties.setProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, cleanPath(envFile));
		properties.setProperty(ConfigKeys.RESULT_DIR, cleanPath(baseDir));

		properties.setProperty(ConfigKeys.EXPERIMENT_DURATION, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL, "1");
		properties.setProperty(ConfigKeys.WORKLOAD_MAXUSERS, "10");
		FileWriter fWriter = new FileWriter(configFile);
		properties.store(fWriter, null);
		fWriter.close();
	}

	private static String cleanPath(String toClean) {
		toClean = toClean.replace(System.getProperty("file.separator"), "/");
		toClean = toClean.replace("\\:", ":");
		if (toClean.startsWith("/")) {
			toClean = toClean.substring(1);
		}
		return toClean;
	}

	@Test
	public void testSpotter() throws IOException, InstrumentationException, MeasurementException, WorkloadException {
		Spotter.getInstance().startDiagnosis(configFile);
		String timestampString = LpeStringUtils.getDetailedTimeStamp(new Date(GlobalConfiguration.getInstance()
				.getPropertyAsLong(ConfigKeys.PPD_RUN_TIMESTAMP, System.currentTimeMillis())));
		timestampString = timestampString.replace(" - ", "_");
		timestampString = timestampString.replace(".", "-");
		timestampString = timestampString.replace(":", "-");
		String dataPath = cleanPath(tempDir.getAbsolutePath() + System.getProperty("file.separator") + timestampString);
		File dir = new File(dataPath);
		Assert.assertTrue(dir.isDirectory());
		Assert.assertTrue(dir.exists());

		File resultFile = new File(cleanPath(dataPath + System.getProperty("file.separator") + "SpotterReport.txt"));
		Assert.assertTrue(resultFile.isFile());
		Assert.assertTrue(resultFile.exists());
		
		
		FileReader fReader = new FileReader(resultFile);
		BufferedReader reader = new BufferedReader(fReader);
		StringBuilder sBuilder = new StringBuilder();
		String line = reader.readLine();
		while(line != null){
			sBuilder.append(line);
			line = reader.readLine();
		}
		reader.close();
		
		Assert.assertTrue(sBuilder.toString().contains("DETECTED"));

	}
}
