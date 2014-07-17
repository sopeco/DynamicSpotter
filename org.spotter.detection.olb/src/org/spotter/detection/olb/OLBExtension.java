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
package org.spotter.detection.olb;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.detection.IDetectionController;

/**
 * One Lane Bridge detection extension.
 * 
 * @author Alexander Wert
 * 
 */
public class OLBExtension extends AbstractDetectionExtension {

	private static final double _100_PERCENT = 100.0;
	
	private static final String EXTENSION_DESCRIPTION = "One-Lane Bridge - Occurs at a point in execution where only one, or a few, processes may continue to execute concurrently (e.g., when accessing a database). Other processes are delayed while they wait for their turn.";
	
	protected static final String OLB_SCOPE_KEY = "scopes";
	protected static final String REQUIRED_CONFIDENCE_LEVEL_KEY = "confidenceLevel";
	protected static final String REQUIRED_SIGNIFICANT_STEPS_KEY = "numSignificantSteps";
	protected static final String CPU_UTILIZATION_THRESHOLD_KEY = "cpuThreshold";
	protected static final String EXPERIMENT_STEPS_KEY = "numExperiments";


	protected static final double REQUIRED_CONFIDENCE_LEVEL_DEFAULT = 0.95;
	protected static final int REQUIRED_SIGNIFICANT_STEPS_DEFAULT = 2;
	protected static final double CPU_UTILIZATION_THRESHOLD_DEFAULT = 90.0;
	protected static final int EXPERIMENT_STEPS_DEFAULT = 4;

	@Override
	public String getName() {
		return "OLB";
	}

	private ConfigParameterDescription createNumExperimentsParameter() {
		ConfigParameterDescription numExperimentsParameter = new ConfigParameterDescription(EXPERIMENT_STEPS_KEY,
				LpeSupportedTypes.Integer);
		numExperimentsParameter.setDefaultValue(String.valueOf(EXPERIMENT_STEPS_DEFAULT));
		numExperimentsParameter.setRange(String.valueOf(2), String.valueOf(Integer.MAX_VALUE));
		numExperimentsParameter.setDescription("Number of experiments to execute with "
				+ "different number of users between 1 and max number of users.");
		return numExperimentsParameter;
	}

	private ConfigParameterDescription createCpuThresholdParameter() {
		ConfigParameterDescription cpuThresholdParameter = new ConfigParameterDescription(
				CPU_UTILIZATION_THRESHOLD_KEY, LpeSupportedTypes.Double);
		cpuThresholdParameter.setDefaultValue(String.valueOf(CPU_UTILIZATION_THRESHOLD_DEFAULT));
		cpuThresholdParameter.setRange(String.valueOf(0.0), String.valueOf(_100_PERCENT));
		cpuThresholdParameter.setDescription("Defines the CPU utilization threshold, "
				+ "when a system is considered as overutilized.");
		return cpuThresholdParameter;
	}

	private ConfigParameterDescription createNumSignificantStepsParameter() {
		ConfigParameterDescription numSignificantStepsParameter = new ConfigParameterDescription(
				REQUIRED_SIGNIFICANT_STEPS_KEY, LpeSupportedTypes.Integer);
		numSignificantStepsParameter.setDefaultValue(String.valueOf(REQUIRED_SIGNIFICANT_STEPS_DEFAULT));
		numSignificantStepsParameter.setRange(String.valueOf(1), String.valueOf(Integer.MAX_VALUE));
		numSignificantStepsParameter.setDescription("This parameter specifies the number of steps between experiments "
				+ "required to show a significant increase in order to detect a One Lane Bridge.");
		return numSignificantStepsParameter;
	}

	private ConfigParameterDescription createConfidenceLevelParameter() {
		ConfigParameterDescription requiredConfidenceLevel = new ConfigParameterDescription(
				REQUIRED_CONFIDENCE_LEVEL_KEY, LpeSupportedTypes.Double);
		requiredConfidenceLevel.setDefaultValue(String.valueOf(REQUIRED_CONFIDENCE_LEVEL_DEFAULT));
		requiredConfidenceLevel.setRange("0.0", "1.0");
		requiredConfidenceLevel.setDescription("This parameter defines the confidence level to be reached "
				+ "in order to recognize a significant difference when comparing "
				+ "two response time samples with the t-test.");
		return requiredConfidenceLevel;
	}

	@Override
	public IDetectionController createExtensionArtifact() {
		return new OLBDetectionController(this);
	}



	@Override
	protected void initializeConfigurationParameters() {
		addConfigParameter(ConfigParameterDescription.createExtensionDescription(EXTENSION_DESCRIPTION));
		addConfigParameter(createConfidenceLevelParameter());
		addConfigParameter(createNumSignificantStepsParameter());
		addConfigParameter(createCpuThresholdParameter());
		addConfigParameter(createNumExperimentsParameter());
	}

}
