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
package org.spotter.core.workload;

import java.util.Properties;

import org.lpe.common.extension.AbstractExtensionArtifact;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.LpeStringUtils;
import org.spotter.core.AbstractSpotterSatelliteExtension;

/**
 * Abstract superclass for all workload adapters.
 * 
 * @author Alexander Wert
 * 
 */
public abstract class AbstractWorkloadAdapter extends AbstractExtensionArtifact implements IWorkloadAdapter {

	private Properties properties;

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *            extension provider
	 */
	public AbstractWorkloadAdapter(IExtension<?> provider) {
		super(provider);
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return LpeStringUtils.getPropertyOrFail(getProperties(), AbstractSpotterSatelliteExtension.NAME_KEY, null);
	}

	/**
	 * @return the host
	 */
	@Override
	public String getHost() {
		return LpeStringUtils.getPropertyOrFail(getProperties(), AbstractSpotterSatelliteExtension.HOST_KEY, null);
	}

	/**
	 * @return the port
	 */
	@Override
	public String getPort() {
		return LpeStringUtils.getPropertyOrFail(getProperties(), AbstractSpotterSatelliteExtension.PORT_KEY, null);
	}

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
		}
		return properties;
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

}
