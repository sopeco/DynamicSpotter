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
package org.spotter.core;

import java.util.HashSet;
import java.util.Set;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.shared.configuration.ConfigKeys;

/**
 * Abstract class for all Dynamic Spotter satellites.
 * 
 * @author Alexander Wert
 * 
 */
public abstract class AbstractSpotterSatelliteExtension {
	
	/**
	 * property key for host.
	 */
	public static final String HOST_KEY = ConfigKeys.SATELLITE_ADAPTER_HOST_KEY;
	
	/**
	 * property key for port.
	 */
	public static final String PORT_KEY = ConfigKeys.SATELLITE_ADAPTER_PORT_KEY;
	
	/**
	 * property key for name.
	 */
	public static final String NAME_KEY = ConfigKeys.SATELLITE_ADAPTER_NAME_KEY;
	
	/**
	 * The set contains all the configuration for this extension.
	 */
	protected final Set<ConfigParameterDescription> configParameters;
	
	/**
	 * Constructor.
	 */
	public AbstractSpotterSatelliteExtension() {
		configParameters = new HashSet<ConfigParameterDescription>();
		addConfigParameter(createNameParameter());
		if (isRemoteExtension()) {
			addConfigParameter(createHostParameter());
			addConfigParameter(createPortParameter());
		}
		initializeConfigurationParameters();
	}

	protected abstract void initializeConfigurationParameters();
	
	/**
	 * Returns the default name for the satellite adapter.
	 * 
	 * @return the default name for the satellite adapter
	 */
	protected String getDefaultSatelleiteExtensionName() {
		return "Spotter Satellite Adapter";
	}

	/**
	 * @param configuration
	 *            parameter to add to this extension
	 */
	protected void addConfigParameter(ConfigParameterDescription parameter) {
		configParameters.add(parameter);
	}

	private ConfigParameterDescription createNameParameter() {
		ConfigParameterDescription nameParameter = new ConfigParameterDescription(NAME_KEY, LpeSupportedTypes.String);
		nameParameter.setMandatory(true);
		nameParameter.setAset(false);
		nameParameter.setDefaultValue(getDefaultSatelleiteExtensionName());
		nameParameter.setDescription("The name of this satellite adapter.");

		return nameParameter;
	}

	private ConfigParameterDescription createHostParameter() {
		ConfigParameterDescription hostParameter = new ConfigParameterDescription(HOST_KEY, LpeSupportedTypes.String);
		hostParameter.setMandatory(true);
		hostParameter.setAset(false);
		hostParameter.setDefaultValue("localhost");
		hostParameter.setDescription("The host/ip where this satellite adapter should connect to.");

		return hostParameter;
	}

	private ConfigParameterDescription createPortParameter() {
		ConfigParameterDescription portParameter = new ConfigParameterDescription(PORT_KEY, LpeSupportedTypes.Integer);
		portParameter.setMandatory(true);
		portParameter.setAset(false);
		portParameter.setRange("0", "65535");
		portParameter.setDefaultValue("8080");
		portParameter.setDescription("The port the satellite adapter should connect to.");

		return portParameter;
	}

	/**
	 * Tests connectivity to the satellite.
	 * 
	 * @param host
	 *            host or IP of the machine running the satellite
	 * @param port
	 *            port of the satellite
	 * @return true, if connection can be established
	 */
	public abstract boolean testConnection(String host, String port);

	/**
	 * 
	 * @return true, if the satellite is a remote satellite
	 */
	public abstract boolean isRemoteExtension();
}
