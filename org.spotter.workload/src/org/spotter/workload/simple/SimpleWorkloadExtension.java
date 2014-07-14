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
package org.spotter.workload.simple;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.workload.AbstractWorkloadExtension;
import org.spotter.core.workload.IWorkloadAdapter;

/**
 * Extension class for simple workload generator.
 * 
 * @author Alexander Wert
 * 
 */
public class SimpleWorkloadExtension extends AbstractWorkloadExtension {

	@Override
	public String getName() {
		return "workload.adapter.simple";
	}

	private ConfigParameterDescription createScriptPathParameter() {
		ConfigParameterDescription scriptParameter = new ConfigParameterDescription(
				SimpleWorkloadDriver.USER_SCRIPT_PATH, LpeSupportedTypes.String);
		scriptParameter.setMandatory(true);
		scriptParameter.setAset(false);
		scriptParameter.setDefaultValue("");
		scriptParameter
				.setDescription("Path to the directory which contains the package containing the load script class file.");

		return scriptParameter;
	}

	private ConfigParameterDescription createScriptClassParameter() {
		ConfigParameterDescription classParameter = new ConfigParameterDescription(
				SimpleWorkloadDriver.USER_SCRIPT_CLASS_NAME, LpeSupportedTypes.String);
		classParameter.setMandatory(true);
		classParameter.setAset(false);
		classParameter.setDefaultValue("");
		classParameter.setDescription("Full qualified name of the Class describing the load.");

		return classParameter;
	}

	@Override
	protected void initializeConfigurationParameters() {
		addConfigParameter(createScriptPathParameter());
		addConfigParameter(createScriptClassParameter());
	}

	@Override
	public IWorkloadAdapter createExtensionArtifact() {
		return new SimpleWorkloadDriver(this);
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
