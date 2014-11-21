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

import java.util.HashSet;
import java.util.Set;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;

/**
 * Wraps all configuration key constants.
 * 
 * @author Alexander Wert
 */
public final class ConfigKeys {

	private static final int _10 = 10;
	public static final int DEFAULT_SUT_WARMUP_DURATION = 180;

	
	public static final String DETECTABLE_KEY = "org.spotter.detection.detectable";
	
	/**
	 * Config-Key for the path to the performance problem hierarchy file.<br />
	 * <b>Required configuration key.</b>
	 */
	public static final String CONF_PROBLEM_HIERARCHY_FILE = "org.spotter.conf.problemHierarchyFile";

	/**
	 * Specifies the directoy for the results. Required configuration key.
	 */
	public static final String RESULT_DIR = "org.spotter.resultDir";

	public static final String REPORT_FOR_NEGATIVE_RESULTS = "org.spotter.reportForNotDetected";

	/**
	 * <b>Required configuration key.</b>
	 */
	public static final String MEASUREMENT_ENVIRONMENT_FILE = "org.spotter.measurement.environmentDescriptionFile";

	/**
	 * The maximum users for a workload.<br />
	 * <b>Required configuration key (default 10).</b>
	 */
	public static final String WORKLOAD_MAXUSERS = "org.spotter.workload.maxusers";

	public static final String PPD_RUN_TIMESTAMP = "org.spotter.run.timestamp";

	/**
	 * Defines how many users per interval (
	 * {@link #EXPERIMENT_RAMP_UP_INTERVAL_LENGTH}) are put into the system. The
	 * ramp up phase is finished, when the {@link #WORKLOAD_MAXUSERS} value is
	 * reached.<br />
	 * <b>Required configuration key.</b>
	 */
	public static final String EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL = "org.spotter.workload.experiment.rampup.numUsersPerInterval";

	/**
	 * Defines the interval length in seconds for the ramp up phase.<br />
	 * <b>Required configuration key.</b>
	 */
	public static final String EXPERIMENT_RAMP_UP_INTERVAL_LENGTH = "org.spotter.workload.experiment.rampup.intervalLength"; // [seconds]

	/**
	 * Defines how many users per interval (
	 * {@link #EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH}) are put out of the system.
	 * The cool down phase is finished, when the minimum user count of the
	 * system is reached. The minimum user count is defined in the corresponding
	 * DetectionController, which is most times 1.<br />
	 * <b>Required configuration key.</b>
	 */
	public static final String EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL = "org.spotter.workload.experiment.cooldown.numUsersPerInterval";

	/**
	 * Defines the interval length in seconds for the cool down phase.<br />
	 * <b>Required configuration key.</b>
	 */
	public static final String EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH = "org.spotter.workload.experiment.cooldown.intervalLength"; // [seconds]

	/**
	 * The experiment duration time in seconds. <b>Required configuration key
	 * (default 10).</b>
	 */
	public static final String EXPERIMENT_DURATION = "org.spotter.workload.experiment.duration"; // [seconds]

	/**
	 * True, if the experiment running is omitted and already available results
	 * are used instead. If the experiment should be omitted, the path to the
	 * dummy data ({@link #DUMMY_EXPERIMENT_DATA}) must be set.<br />
	 * <b>Required configuration key.</b>
	 */
	public static final String OMIT_EXPERIMENTS = "org.spotter.omitExperiments";

	public static final String OMIT_WARMUP = "org.spotter.omitWarmup";

	/**
	 * Duration in seconds the pre-warmup phase should be running. The
	 * pre-warmup phase is the phase the SUT is visited to load the required
	 * classes for the experiment into the JVM. The JVM does only load a class
	 * when it is required. We need to ensure that all the classes for the
	 * experiment are already loaded when the the real experiment is started.
	 * Thus, we need to run a pre-warmup phase where the workload is already
	 * executed and necessary classes are loaded.<br />
	 * <b>Optional configuration key.</b>
	 */
	public static final String PREWARUMUP_DURATION = "org.spotter.prewarmup.duration"; // [seconds]

	/**
	 * Required configuration key, when the experiments are omitted.
	 */
	public static final String DUMMY_EXPERIMENT_DATA = "org.spotter.dummyData";

	public static final String SPOTTER_RUN_NAME = "org.spotter.runName";

	/**
	 * The host the satellite adapter is connecting to.
	 */
	public static final String SATELLITE_HOST_KEY = "org.spotter.satellite.host";

	/**
	 * The port the satellite adapter is connecting to.
	 */
	public static final String SATELLITE_PORT_KEY = "org.spotter.satellite.port";

	/**
	 * The name for a satellite adapter.
	 */
	public static final String SATELLITE_ADAPTER_NAME_KEY = "org.spotter.satellite.adapter.name";

	public static final String SPOTTER_REST_BASE = "spotter";
	public static final String SPOTTER_REST_START_DIAG = "startDiagnosis";
	public static final String SPOTTER_REST_REQU_RESULTS = "requestResults";
	public static final String SPOTTER_REST_IS_RUNNING = "isRunning";
	public static final String SPOTTER_REST_LAST_EXCEPTION = "lastException";
	public static final String SPOTTER_REST_CONFIG_PARAMS = "configParameters";
	public static final String SPOTTER_REST_EXTENSIONS = "extensions";
	public static final String SPOTTER_REST_EXTENSION_PARAMETERS = "extensionParameters";
	public static final String SPOTTER_REST_DEFAULT_HIERARCHY = "defaultHierarchy";
	public static final String SPOTTER_REST_CURRENT_JOB = "currentJob";
	public static final String SPOTTER_REST_CURRENT_ROOT_PROBLEM = "currentRootProblem";
	public static final String SPOTTER_REST_CURRENT_PROGRESS = "currentProgress";
	public static final String SPOTTER_REST_TEST_SATELLITE_CONNECTION = "testSatelliteConnection";
	public static final String SPOTTER_REST_TEST_CONNECTION = "testConnection";

	/**
	 * Private constructor due to utility class.
	 */
	private ConfigKeys() {
	}

	private static ConfigParameterDescription getMaxUsersParameter() {
		ConfigParameterDescription parameter = new ConfigParameterDescription(WORKLOAD_MAXUSERS,
				LpeSupportedTypes.Integer);
		parameter.setMandatory(true);
		parameter.setDefaultValue(String.valueOf(_10));
		parameter.setDescription("The maximal number of users the system under test should be able to handle.");
		return parameter;
	}

	private static ConfigParameterDescription getRampUpUsersPerIntervalParameter() {
		ConfigParameterDescription parameter = new ConfigParameterDescription(
				EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL, LpeSupportedTypes.Integer);
		parameter.setMandatory(true);
		parameter.setDefaultValue(String.valueOf(1));
		parameter
				.setDescription("Defines the ramp up phase of load generation. Specifies the number of user which should enter the system per time interval.");
		return parameter;
	}

	private static ConfigParameterDescription getRampUpIntervalParameter() {
		ConfigParameterDescription parameter = new ConfigParameterDescription(EXPERIMENT_RAMP_UP_INTERVAL_LENGTH,
				LpeSupportedTypes.Integer);
		parameter.setMandatory(true);
		parameter.setDefaultValue(String.valueOf(1));
		parameter
				.setDescription("Defines the ramp up phase of load generation. Specifies the length of a single interval in [seconds].");
		return parameter;
	}

	private static ConfigParameterDescription getCoolDownUsersPerIntervalParameter() {
		ConfigParameterDescription parameter = new ConfigParameterDescription(
				EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL, LpeSupportedTypes.Integer);
		parameter.setMandatory(true);
		parameter.setDefaultValue(String.valueOf(1));
		parameter.setDescription("Defines the cool down phase of load generation. "
				+ "Specifies the number of user which should enter the system per time interval.");
		return parameter;
	}

	private static ConfigParameterDescription getCoolDownIntervalParameter() {
		ConfigParameterDescription parameter = new ConfigParameterDescription(EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH,
				LpeSupportedTypes.Integer);
		parameter.setMandatory(true);
		parameter.setDefaultValue(String.valueOf(1));
		parameter
				.setDescription("Defines the cool down phase of load generation. Specifies the length of a single interval in [seconds].");
		return parameter;
	}

	private static ConfigParameterDescription getExperimentDurationParameter() {
		ConfigParameterDescription parameter = new ConfigParameterDescription(EXPERIMENT_DURATION,
				LpeSupportedTypes.Integer);
		parameter.setMandatory(true);
		parameter.setDefaultValue(String.valueOf(_10));
		parameter.setDescription("Specifies the duration of the steady state experiment phase in [seconds].");
		return parameter;
	}

	private static ConfigParameterDescription getOmitExperimentParameter() {
		ConfigParameterDescription parameter = new ConfigParameterDescription(OMIT_EXPERIMENTS,
				LpeSupportedTypes.Boolean);
		parameter.setMandatory(false);
		parameter.setDefaultValue(String.valueOf(false));
		parameter.setDescription("Specifies whether the experiment phase of Spotter should be omitted or not. "
				+ "If this parameter is true, the [org.spotter.dummyData] parameter needs to be specified.");
		return parameter;
	}

	private static ConfigParameterDescription getDummyDataParameter() {
		ConfigParameterDescription parameter = new ConfigParameterDescription(DUMMY_EXPERIMENT_DATA,
				LpeSupportedTypes.String);
		parameter.setMandatory(false);
		parameter.setADirectory(true);
		parameter.setDefaultValue("");
		parameter.setDescription("If experiments should be omitted, this parameter specified "
				+ "the path to the experiment data which should be used for analysis instead. "
				+ "The path must point to the root folder which contains the SpotterReport.txt file.");
		return parameter;
	}

	private static ConfigParameterDescription getPreWarumupDuration() {
		ConfigParameterDescription parameter = new ConfigParameterDescription(PREWARUMUP_DURATION,
				LpeSupportedTypes.Integer);
		parameter.setMandatory(false);
		parameter.setDefaultValue(String.valueOf(DEFAULT_SUT_WARMUP_DURATION));
		parameter
				.setDescription("Specifies the duration the pre-warmup phase should be running. In the pre-warmup phase workload is started "
						+ "to force the JVM to load classes which are used in the real experiment. "
						+ "Then, in the real experiment theses classes can be instrumented "
						+ "directly. (Instrumentation of an unloaded class would fail.)");
		return parameter;
	}

	/**
	 * 
	 * @return returns a set of configuration parameters of Dynamic Spotter.
	 */
	public static Set<ConfigParameterDescription> getSpotterConfigParamters() {
		Set<ConfigParameterDescription> configParameters = new HashSet<>();
		configParameters.add(getMaxUsersParameter());
		configParameters.add(getRampUpUsersPerIntervalParameter());
		configParameters.add(getRampUpIntervalParameter());
		configParameters.add(getCoolDownUsersPerIntervalParameter());
		configParameters.add(getCoolDownIntervalParameter());
		configParameters.add(getExperimentDurationParameter());
		configParameters.add(getOmitExperimentParameter());
		configParameters.add(getDummyDataParameter());
		configParameters.add(getPreWarumupDuration());
		return configParameters;
	}
}
