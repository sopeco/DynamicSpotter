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
package org.spotter.loadrunner.instrumentation;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.loadgenerator.LoadGeneratorClient;
import org.spotter.core.instrumentation.AbstractInstrumentationExtension;
import org.spotter.core.instrumentation.ISpotterInstrumentation;

/**
 * Extension for LoadRunner instrumentation.
 * @author Alexander Wert
 *
 */
public class LoadRunnerInstrumentationExtension extends AbstractInstrumentationExtension {

	private static final String EXTENSION_DESCRIPTION = "The instrumentation satellite adapter for Loadrunner can be used to "
														+ "connect to an Loadrunner instrumentation satellite. This satellite "
														+ "will only be applicable if you have a Loadrunner as a workload "
														+ "generator.";
	
	@Override
	public ISpotterInstrumentation createExtensionArtifact() {
		return new LoadRunnerInstrumentationClient(this);
	}

	@Override
	public String getName() {
		return "instrumentation.satellite.adapter.loadrunner";
	}
	
	@Override
	protected String getDefaultSatelleiteExtensionName() {
		return "LoadRunner Instrumentation Satellite Adapter";
	}

	@Override
	protected void initializeConfigurationParameters() {
		addConfigParameter(ConfigParameterDescription.createExtensionDescription(EXTENSION_DESCRIPTION));
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
