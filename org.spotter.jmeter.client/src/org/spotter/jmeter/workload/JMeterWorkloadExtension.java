/**
 * Copyright 2014 SAP AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.spotter.jmeter.workload;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.workload.AbstractWorkloadExtension;
import org.spotter.core.workload.IWorkloadAdapter;
import org.spotter.jmeter.JMeterConfigKeys;

/**
 * Loadrunner workload extension.
 * 
 * @author Alexander Wert
 * 
 */
public class JMeterWorkloadExtension extends AbstractWorkloadExtension {

	private static final String EXTENSION_DESCRIPTION = "The workload satellite adapter connecting to a JMeter "
														+ "workload satellite.";

	private static final String[] FILE_RESULTS_EXTENSIONS = { "*.csv" };

	private static final String FILE_RESULTS_DEFAULT = "result.csv";

	private static final String[] FILE_SCENARIO_EXTENSIONS = { "*.jmx" };

	public static final String THINK_TIME_MIN = "org.spotter.workload.jmeter.thinkTimeMin";

	public static final String THINK_TIME_MAX = "org.spotter.workload.jmeter.thinkTimeMax";

	@Override
	public String getName() {
		return "workload.satellite.adapter.jmeter";
	}

	@Override
	protected String getDefaultSatelleiteExtensionName() {
		return "JMeter Workload Satellite Adapter";
	}

	private ConfigParameterDescription createJMeterHomeParameter() {
		ConfigParameterDescription jMeterHomeParameter = new ConfigParameterDescription(JMeterConfigKeys.JMETER_HOME,
																						LpeSupportedTypes.String);
		jMeterHomeParameter.setDirectory(true);
		jMeterHomeParameter.setMandatory(true);
		jMeterHomeParameter.setDefaultValue("");
		jMeterHomeParameter.setDescription("The path to the JMeter root directory.");

		return jMeterHomeParameter;
	}

	private ConfigParameterDescription createJMeterScenarioFileParameter() {
		ConfigParameterDescription jMeterScenarioFileParameter = new ConfigParameterDescription(JMeterConfigKeys.SCENARIO_FILE,
																								LpeSupportedTypes.String);
		jMeterScenarioFileParameter.setFile(true);
		jMeterScenarioFileParameter.setMandatory(true);
		jMeterScenarioFileParameter.setDefaultValue("");
		jMeterScenarioFileParameter.setFileExtensions(FILE_SCENARIO_EXTENSIONS);
		jMeterScenarioFileParameter.setDescription("The path to the JMeter load script file.");

		return jMeterScenarioFileParameter;
	}

	private ConfigParameterDescription createJMeterSamplingFileParameter() {
		ConfigParameterDescription jMeterSamplingFileParameter = new ConfigParameterDescription(JMeterConfigKeys.SAMPLING_FILE,
																								LpeSupportedTypes.String);
		jMeterSamplingFileParameter.setFile(true);
		jMeterSamplingFileParameter.setMandatory(false);
		jMeterSamplingFileParameter.setDefaultValue("");
		jMeterSamplingFileParameter.setDefaultFileName(FILE_RESULTS_DEFAULT);
		jMeterSamplingFileParameter.setFileExtensions(FILE_RESULTS_EXTENSIONS);
		jMeterSamplingFileParameter.setDescription("The file where JMeter should store the sampling values. The sampling must be enabled "
													+ "explicity with the corresponding property key.");

		return jMeterSamplingFileParameter;
	}

	private ConfigParameterDescription createJMeterSamplingFileFlagParameter() {
		ConfigParameterDescription jMeterSamplingFileFlag = new ConfigParameterDescription(	JMeterConfigKeys.SAMPLING_FLAG,
																							LpeSupportedTypes.Boolean);
		jMeterSamplingFileFlag.setMandatory(false);
		jMeterSamplingFileFlag.setDefaultValue(String.valueOf(false));
		jMeterSamplingFileFlag.setDescription("Flag, if JMeter should create a result file. If this is true, you must define a sampling file!");

		return jMeterSamplingFileFlag;
	}

	private ConfigParameterDescription createJMeterThinkTimeMinParameter() {
		ConfigParameterDescription jMeterThinkTimeMinParameter = new ConfigParameterDescription(JMeterConfigKeys.THINK_TIME_MIN,
																								LpeSupportedTypes.Integer);
		jMeterThinkTimeMinParameter.setMandatory(true);
		jMeterThinkTimeMinParameter.setDefaultValue(String.valueOf(1000));
		jMeterThinkTimeMinParameter.setDescription("Minimal thinktime in milliseconds.");

		return jMeterThinkTimeMinParameter;
	}

	private ConfigParameterDescription createJMeterLogFilePrefixParameter() {
		ConfigParameterDescription jMeterLogFilePrefixParameter = new ConfigParameterDescription(	JMeterConfigKeys.LOG_FILE_PREFIX,
																									LpeSupportedTypes.String);
		jMeterLogFilePrefixParameter.setMandatory(false);
		jMeterLogFilePrefixParameter.setDefaultValue("JMETWRAPPERLOG_");
		jMeterLogFilePrefixParameter.setDescription("Prefix for log files. The log files are stored in the JMeter root directory. A "
													+ "unique ID is appeneded to the file name. Only respected when the log file flag is true.");

		return jMeterLogFilePrefixParameter;
	}

	private ConfigParameterDescription createJMeterLogFileFlagParameter() {
		ConfigParameterDescription jMeterLogFileFlag = new ConfigParameterDescription(	JMeterConfigKeys.LOG_FILE_FLAG,
																						LpeSupportedTypes.Boolean);
		jMeterLogFileFlag.setMandatory(false);
		jMeterLogFileFlag.setDefaultValue(String.valueOf(false));
		jMeterLogFileFlag.setDescription("Flag if JMeter should create a log file.");

		return jMeterLogFileFlag;
	}

	private ConfigParameterDescription createJMeterThinkTimeMaxParameter() {
		ConfigParameterDescription jMeterThinkTimeMaxParameter = new ConfigParameterDescription(JMeterConfigKeys.THINK_TIME_MAX,
																								LpeSupportedTypes.Integer);
		jMeterThinkTimeMaxParameter.setMandatory(true);
		jMeterThinkTimeMaxParameter.setDefaultValue(String.valueOf(2000));
		jMeterThinkTimeMaxParameter.setDescription("Maximum thinktime in milliseconds.");

		return jMeterThinkTimeMaxParameter;
	}

	@Override
	protected void initializeConfigurationParameters() {
		addConfigParameter(createJMeterHomeParameter());
		addConfigParameter(createJMeterScenarioFileParameter());
		addConfigParameter(createJMeterSamplingFileParameter());
		addConfigParameter(createJMeterThinkTimeMinParameter());
		addConfigParameter(createJMeterThinkTimeMaxParameter());
		addConfigParameter(createJMeterLogFileFlagParameter());
		addConfigParameter(createJMeterLogFilePrefixParameter());
		addConfigParameter(createJMeterSamplingFileFlagParameter());
		addConfigParameter(ConfigParameterDescription.createExtensionDescription(EXTENSION_DESCRIPTION));
	}

	@Override
	public IWorkloadAdapter createExtensionArtifact() {
		return new JMeterWorkloadClient(this);
	}

	@Override
	public boolean testConnection(String host, String port) {
		return true;
	}

	@Override
	public boolean isRemoteExtension() {
		return false;
	}

}
