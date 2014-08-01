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
package org.spotter.core.test.dummies.satellites;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.instrumentation.AbstractInstrumentationExtension;
import org.spotter.core.instrumentation.ISpotterInstrumentation;

public class DummyInstrumentationExtension extends AbstractInstrumentationExtension{

	@Override
	public String getName() {
		return "DummyInstrumentation";
	}

	@Override
	public ISpotterInstrumentation createExtensionArtifact() {
		return new DummyInstrumentation(this);
	}

	@Override
	protected void initializeConfigurationParameters() {
		ConfigParameterDescription parDescription = new ConfigParameterDescription("test.instrumentation.parameter", LpeSupportedTypes.Integer);
		addConfigParameter(parDescription);
		
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
