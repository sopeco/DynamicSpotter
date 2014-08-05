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
package org.spotter.eclipse.ui.model.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.shared.environment.model.XMConfiguration;

/**
 * A model wrapper which wraps only a configuration parameter list. This wrapper
 * is not associated with an extension.
 * 
 * @author Denis Knoepfle
 * 
 */
public class SpotterConfigModelWrapper implements IModelWrapper {

	private final String projectName;
	private List<XMConfiguration> xmConfigList;

	/**
	 * Creates an instance of this class using the given properties.
	 * 
	 * @param projectName
	 *            the project name
	 * @param properties
	 *            the properties of the model
	 */
	public SpotterConfigModelWrapper(String projectName, Properties properties) {
		this.projectName = projectName;
		this.xmConfigList = new ArrayList<XMConfiguration>();
		for (Entry<Object, Object> entry : properties.entrySet()) {
			XMConfiguration xmConfig = new XMConfiguration();
			xmConfig.setKey((String) entry.getKey());
			xmConfig.setValue((String) entry.getValue());
			xmConfigList.add(xmConfig);
		}
	}

	@Override
	public String getExtensionName() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public List<XMConfiguration> getConfig() {
		return xmConfigList;
	}

	@Override
	public void setConfig(List<XMConfiguration> config) {
		this.xmConfigList = config;
	}

	/**
	 * The spotter config has no underlying XML model and uses the
	 * <code>ConfigParameterDescription</code>s of {@link Spotter} directly.
	 * 
	 * @return <code>null</code>
	 */
	@Override
	public Object getXMLModel() {
		return null;
	}

	/**
	 * Returns the <code>ConfigParameterDescription</code>s from {@link Spotter}
	 * .
	 * 
	 * @return the config parameter descriptions from <code>Spotter</code>
	 */
	@Override
	public Set<ConfigParameterDescription> getExtensionConfigParams() {
		ServiceClientWrapper client = Activator.getDefault().getClient(projectName);
		return client.getConfigurationParameters();
	}

	@Override
	public Boolean testConnection() throws Exception {
		return false;
	}

	@Override
	public void removed() {
	}

}
