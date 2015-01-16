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
package org.spotter.shared.configuration;

import java.io.File;
import java.util.Date;

import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.LpeFileUtils;
import org.lpe.common.util.LpeStringUtils;

/**
 * Checker for Spotter configuration.
 * 
 * @author C5170547
 * 
 */
public final class ConfigCheck {
	/**
	 * private constructor.
	 */
	private ConfigCheck() {

	}

	/**
	 * Checks the validity of the Spotter configuration.
	 */
	public static void checkConfiguration() {

		checkResultDirConf();

		checkHierarchyConf();

		checkMeasurementEnvironmentConf();

		checkNumMaxUsers();

		checkExperimentTimes();

		checkOmitExperimentsConfig();
	}

	private static void checkOmitExperimentsConfig() {
		String omit = GlobalConfiguration.getInstance().getProperty(ConfigKeys.OMIT_EXPERIMENTS);
		if (omit == null) {
			GlobalConfiguration.getInstance().putProperty(ConfigKeys.OMIT_EXPERIMENTS, "false");
		} else if (Boolean.parseBoolean(omit)) {
			GlobalConfiguration.getInstance().putProperty(ConfigKeys.OMIT_EXPERIMENTS, "true");
			String dummyData = GlobalConfiguration.getInstance().getProperty(ConfigKeys.DUMMY_EXPERIMENT_DATA);
			if (dummyData == null) {
				throw new IllegalStateException("The DynamicSpotter configuration is specified to omit experiments. "
						+ "In this case a valid directory with dummy data has to be specified, "
						+ "which is not the case!");
			} else {
				File dir = new File(dummyData);
				if (!dir.exists() || !dir.isDirectory()) {
					throw new IllegalStateException(
							"The DynamicSpotter configuration is specified to omit experiments. "
									+ "In this case a valid directory with dummy data has to be specified, "
									+ "which is not the case!");
				}
				correctFileSeparator(ConfigKeys.DUMMY_EXPERIMENT_DATA, true);
			}
		}
	}

	private static void checkExperimentTimes() {
		String rampUpStr = GlobalConfiguration.getInstance().getProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH);
		if (rampUpStr == null) {
			throw new IllegalStateException("Configuration Error: " + "Experiment ramp up time has not been specified!");
		}
		try {
			Integer.parseInt(rampUpStr);
		} catch (Throwable e) {
			throw new IllegalStateException("Configuration Error: " + "Experiment ramp up time is not an integer!");
		}

		rampUpStr = GlobalConfiguration.getInstance().getProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL);
		if (rampUpStr == null) {
			throw new IllegalStateException("Configuration Error: " + "Experiment ramp up time has not been specified!");
		}
		try {
			Integer.parseInt(rampUpStr);
		} catch (Throwable e) {
			throw new IllegalStateException("Configuration Error: " + "Experiment ramp up time is not an integer!");
		}

		String coolDownStr = GlobalConfiguration.getInstance().getProperty(
				ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH);
		if (coolDownStr == null) {
			throw new IllegalStateException("Configuration Error: "
					+ "Experiment cool down time has not been specified!");
		}
		try {
			Integer.parseInt(coolDownStr);
		} catch (Throwable e) {
			throw new IllegalStateException("Configuration Error: " + "Experiment cool down time is not an integer!");
		}

		coolDownStr = GlobalConfiguration.getInstance().getProperty(
				ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL);
		if (coolDownStr == null) {
			throw new IllegalStateException("Configuration Error: "
					+ "Experiment cool down time has not been specified!");
		}
		try {
			Integer.parseInt(coolDownStr);
		} catch (Throwable e) {
			throw new IllegalStateException("Configuration Error: " + "Experiment cool down time is not an integer!");
		}

		String expDurationStr = GlobalConfiguration.getInstance().getProperty(ConfigKeys.EXPERIMENT_DURATION);
		if (expDurationStr == null) {
			throw new IllegalStateException("Configuration Error: " + "Experiment duration has not been specified!");
		}
		try {
			Integer.parseInt(expDurationStr);
		} catch (Throwable e) {
			throw new IllegalStateException("Configuration Error: " + "Experiment duration is not an integer!");
		}
	}

	private static void checkNumMaxUsers() {
		String maxUsersStr = GlobalConfiguration.getInstance().getProperty(ConfigKeys.WORKLOAD_MAXUSERS);
		if (maxUsersStr == null) {
			throw new IllegalStateException("Configuration Error: Maximal number of users for the "
					+ "system under test has not been specified!");
		}
		try {
			Integer.parseInt(maxUsersStr);
		} catch (Throwable e) {
			throw new IllegalStateException("Configuration Error: The value for maximal number of "
					+ "users for the system under test is not an integer!");
		}
	}

	private static void checkHierarchyConf() {
		String hierarchyFile = GlobalConfiguration.getInstance().getProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE);
		if (hierarchyFile == null) {
			throw new IllegalStateException("Configuration Error: "
					+ "Performance problem hierarchy file has not been specified!");
		}
		hierarchyFile = correctFileSeparator(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, false);

		File file = new File(hierarchyFile);
		if (!file.exists()) {
			throw new IllegalStateException("Configuration Error: " + "Performance problem hierarchy file "
					+ file.getAbsolutePath() + " does not exist!");
		}
	}

	private static void checkMeasurementEnvironmentConf() {
		String meFile = GlobalConfiguration.getInstance().getProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE);
		if (meFile == null) {
			throw new IllegalStateException("Configuration Error: "
					+ "Measurement environment file has not been specified!");
		}
		meFile = correctFileSeparator(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, false);
		File file = new File(meFile);
		if (!file.exists()) {
			throw new IllegalStateException("Configuration Error: " + "Measurement environment  file "
					+ file.getAbsolutePath() + " does not exist!");
		}
	}

	private static void checkResultDirConf() {
		String resultDir = GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR);

		if (resultDir == null) {
			throw new IllegalStateException("Configuration Error: "
					+ "Spotter result direcotry has not been specified!");
		}
		resultDir = correctFileSeparator(ConfigKeys.RESULT_DIR, true);

		File resultDirFile = new File(resultDir);
		if (resultDirFile.exists() && !resultDirFile.isDirectory()) {
			throw new IllegalStateException("Configuration Error: "
					+ "The specified Spotter result direcotry points not to a directory!");
		}

		String runName = GlobalConfiguration.getInstance().getProperty(ConfigKeys.SPOTTER_RUN_NAME);

		if (runName == null || runName.isEmpty()) {
			String subDir = LpeStringUtils.getDetailedTimeStamp(new Date(GlobalConfiguration.getInstance()
					.getPropertyAsLong(ConfigKeys.PPD_RUN_TIMESTAMP, System.currentTimeMillis())));
			subDir = subDir.replace(" - ", "_");
			subDir = subDir.replace(".", "-");
			subDir = subDir.replace(":", "-");
			resultDir += subDir + System.getProperty("file.separator");
		} else {
			resultDir += runName + System.getProperty("file.separator");
		}

		resultDirFile = new File(resultDir);
		if (!resultDirFile.exists()) {
			LpeFileUtils.createDir(resultDir);
		}

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR, resultDir);
	}

	private static String correctFileSeparator(String key, boolean isDirectory) {
		String path = GlobalConfiguration.getInstance().getProperty(key);
		path = correctFileName(path, isDirectory);
		GlobalConfiguration.getInstance().putProperty(key, path);
		return path;
	}

	/**
	 * Corrects the given file name. Sets proper file separators, etc.
	 * 
	 * @param fileName
	 *            filename to correct
	 * @param isDirectory
	 *            indicates whether the specified file is a directory
	 * @return corrected file name
	 */
	public static String correctFileName(String fileName, boolean isDirectory) {
		fileName = LpeStringUtils.correctFileSeparator(fileName);

		if (isDirectory) {
			if (!fileName.endsWith(System.getProperty("file.separator"))) {
				fileName += System.getProperty("file.separator");
			}
		}
		return fileName;
	}

}
