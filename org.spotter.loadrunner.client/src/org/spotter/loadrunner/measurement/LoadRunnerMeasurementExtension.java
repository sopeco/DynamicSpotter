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
package org.spotter.loadrunner.measurement;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.loadgenerator.LoadGeneratorClient;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.measurement.AbstractMeasurmentExtension;
import org.spotter.core.measurement.IMeasurementController;
import org.spotter.loadrunner.LRConfigKeys;

/**
 * Loadrunner measurement extension.
 * 
 * @author Alexander Wert
 * 
 */
public class LoadRunnerMeasurementExtension extends AbstractMeasurmentExtension {

	@Override
	public String getName() {
		return "measurement.loadrunner";
	}

	private ConfigParameterDescription createAnalysisPathParameter() {
		ConfigParameterDescription analysisPathParameter = new ConfigParameterDescription(LRConfigKeys.ANALYSIS_EXE,
				LpeSupportedTypes.String);
		analysisPathParameter.setDirectory(true);
		analysisPathParameter.setMandatory(true);
		analysisPathParameter.setAset(false);
		analysisPathParameter.setDefaultValue("");
		analysisPathParameter.setDescription("The path to the Analysis.exe file of the Loadrunner installation.");

		return analysisPathParameter;
	}

	private ConfigParameterDescription createAnalysisTemplateParameter() {
		ConfigParameterDescription analysisTemplateParameter = new ConfigParameterDescription(
				LRConfigKeys.ANALYSIS_TEMPLATE_NAME, LpeSupportedTypes.String);
		analysisTemplateParameter.setMandatory(true);
		analysisTemplateParameter.setAset(false);
		analysisTemplateParameter.setDefaultValue("");
		analysisTemplateParameter
				.setDescription("The name of the analysis template as configured in the Analysis program of Loadrunner.");

		return analysisTemplateParameter;
	}

	private ConfigParameterDescription createResultDirParameter() {
		ConfigParameterDescription resultDirParameter = new ConfigParameterDescription(LRConfigKeys.RESULT_DIR,
				LpeSupportedTypes.String);
		resultDirParameter.setDirectory(true);
		resultDirParameter.setMandatory(true);
		resultDirParameter.setAset(false);
		resultDirParameter.setDefaultValue("");
		resultDirParameter.setDescription("The path to the result directory.");

		return resultDirParameter;
	}

	private ConfigParameterDescription createAnalysisSessionParameter() {
		ConfigParameterDescription analysisSessionParameter = new ConfigParameterDescription(
				LRConfigKeys.ANALYSIS_SESSION_NAME, LpeSupportedTypes.String);
		analysisSessionParameter.setMandatory(true);
		analysisSessionParameter.setAset(false);
		analysisSessionParameter.setDefaultValue("");
		analysisSessionParameter
				.setDescription("The name of the analysis session as configured in the Analysis program of Loadrunner.");

		return analysisSessionParameter;
	}

	@Override
	protected void initializeConfigurationParameters() {
		addConfigParameter(createAnalysisPathParameter());
		addConfigParameter(createAnalysisTemplateParameter());
		addConfigParameter(createResultDirParameter());
		addConfigParameter(createAnalysisSessionParameter());

	}

	@Override
	public IMeasurementController createExtensionArtifact() {
		return new LoadRunnerMeasurementClient(this);
	}

	@Override
	public boolean testConnection(String host, String port) {
		return LoadGeneratorClient.testConnection(host, port);
	}

	@Override
	public boolean isRemoteExtension() {
		return true;
	}

}
