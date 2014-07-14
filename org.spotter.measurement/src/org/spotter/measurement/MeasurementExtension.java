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
package org.spotter.measurement;

import org.aim.artifacts.instrumentation.InstrumentationClient;
import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.core.measurement.AbstractMeasurmentExtension;
import org.spotter.core.measurement.IMeasurementController;

/**
 * Extension for generic measurement REST client.
 * 
 * @author Alexander Wert
 * 
 */
public class MeasurementExtension extends AbstractMeasurmentExtension {

	@Override
	public String getName() {
		return "measurement.client";
	}

	@Override
	protected void initializeConfigurationParameters() {

	}

	@Override
	public IMeasurementController createExtensionArtifact() {
		IMeasurementController mController = new MeasurementClient(this);
		for (ConfigParameterDescription cpd : this.getConfigParameters()) {
			if (cpd.getDefaultValue() != null) {
				mController.getProperties().setProperty(cpd.getName(), cpd.getDefaultValue());
			}
		}
		return mController;

	}

	@Override
	public boolean testConnection(String host, String port) {
		return InstrumentationClient.testConnection(host, port);
	}

	@Override
	public boolean isRemoteExtension() {
		return true;
	}

}
