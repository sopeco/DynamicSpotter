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
package org.spotter.measurement.jmsserver;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.measurement.AbstractMeasurmentExtension;
import org.spotter.core.measurement.IMeasurementController;

/**
 * Extension for JMS server sampler.
 * 
 * @author Alexander Wert
 * 
 */
public class JmsServerMeasurementExtension extends AbstractMeasurmentExtension {

	private static final String EXTENSION_DESCRIPTION = "The jmsserver sampling measurement satellite adapter is used "
														+ "to connect to the special sampling satellites for Java Messaging "
														+ "Service (JMS) server. They sample more than the default sampling "
														+ "satellites.";
	
	@Override
	public String getName() {
		return "measurement.satellite.adapter.sampling.jmsserver";
	}

	@Override
	public IMeasurementController createExtensionArtifact() {
		return new JmsServerMeasurement(this);
	}

	private ConfigParameterDescription createSamplingDelayParameter() {
		ConfigParameterDescription samplingDelayParameter = new ConfigParameterDescription(
				JmsServerMeasurement.SAMPLING_DELAY, LpeSupportedTypes.Long);
		samplingDelayParameter.setMandatory(false);
		samplingDelayParameter.setAset(false);
		samplingDelayParameter.setDefaultValue(String.valueOf(JmsServerMeasurement.DEFAULT_DELAY));
		samplingDelayParameter.setDescription("The sampling interval in milliseconds.");

		return samplingDelayParameter;
	}

	private ConfigParameterDescription createCollectorTypeParameter() {
		ConfigParameterDescription collectorTypeParameter = new ConfigParameterDescription(
				JmsServerMeasurement.COLLECTOR_TYPE_KEY, LpeSupportedTypes.String);
		collectorTypeParameter.setMandatory(true);
		collectorTypeParameter.setAset(false);
		collectorTypeParameter.setDescription("Type to use for data collector");

		return collectorTypeParameter;
	}

	private ConfigParameterDescription createServerConnectionStringParameter() {
		ConfigParameterDescription collectorTypeParameter = new ConfigParameterDescription(
				JmsServerMeasurement.ACTIVE_MQJMX_URL, LpeSupportedTypes.String);
		collectorTypeParameter.setMandatory(true);
		collectorTypeParameter.setAset(false);
		collectorTypeParameter.setDescription("Connection string to the JMX interface of the massaging service.");

		return collectorTypeParameter;
	}

	@Override
	protected void initializeConfigurationParameters() {
		addConfigParameter(createSamplingDelayParameter());
		addConfigParameter(createCollectorTypeParameter());
		addConfigParameter(createServerConnectionStringParameter());
		addConfigParameter(ConfigParameterDescription.createExtensionDescription(EXTENSION_DESCRIPTION));
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
