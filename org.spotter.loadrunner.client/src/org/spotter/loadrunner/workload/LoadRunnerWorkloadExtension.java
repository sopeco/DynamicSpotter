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
package org.spotter.loadrunner.workload;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.loadgenerator.LoadGeneratorClient;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.workload.AbstractWorkloadExtension;
import org.spotter.core.workload.IWorkloadAdapter;
import org.spotter.loadrunner.LRConfigKeys;

/**
 * Loadrunner workload extension.
 * 
 * @author Alexander Wert
 * 
 */
public class LoadRunnerWorkloadExtension extends AbstractWorkloadExtension {

	private static final String EXTENSION_DESCRIPTION = "The loadrunner workload satellite adapter connects to the workload "
														+ "satellite executed on the Loadrunner system. This satellite adapter "
														+ "provokes the workload satellite to start the workload on Loadrunner.";
	
	@Override
	public String getName() {
		return "workload.satellite.adapter.loadrunner";
	}

	@Override
	protected String getDefaultSatelleiteExtensionName() {
		return "LoadRunner Workload Satellite Adapter";
	}
	
	private ConfigParameterDescription createLoadRunnerPathParameter() {
		ConfigParameterDescription loadRunnerPathParameter = new ConfigParameterDescription(LRConfigKeys.CONTROLLER_EXE,
				LpeSupportedTypes.String);
		loadRunnerPathParameter.setDirectory(true);
		loadRunnerPathParameter.setMandatory(true);
		loadRunnerPathParameter.setAset(false);
		loadRunnerPathParameter.setDefaultValue("");
		loadRunnerPathParameter.setDescription("The path to the LoadRunner.exe file of the Loadrunner installation.");

		return loadRunnerPathParameter;
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

	private ConfigParameterDescription createScenarioPathParameter() {
		ConfigParameterDescription scenarioPathParameter = new ConfigParameterDescription(LRConfigKeys.SCENARIO_FILE,
				LpeSupportedTypes.String);
		scenarioPathParameter.setDirectory(true);
		scenarioPathParameter.setMandatory(true);
		scenarioPathParameter.setAset(false);
		scenarioPathParameter.setDefaultValue("");
		scenarioPathParameter
				.setDescription("The path to the Loadrunner scenario (.lrs) file to use for load generation.");

		return scenarioPathParameter;
	}



	@Override
	protected void initializeConfigurationParameters() {
		addConfigParameter(createLoadRunnerPathParameter());
		addConfigParameter(createScenarioPathParameter());
		addConfigParameter(createResultDirParameter());
		addConfigParameter(ConfigParameterDescription.createExtensionDescription(EXTENSION_DESCRIPTION));
	}

	@Override
	public IWorkloadAdapter createExtensionArtifact() {
		return new LoadRunnerWorkloadClient(this);
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
