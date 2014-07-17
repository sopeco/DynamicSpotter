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

import java.util.HashSet;
import java.util.Set;

import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.model.ExtensionMetaobject;
import org.spotter.eclipse.ui.util.SpotterUtils;

/**
 * An abstract base class for model wrappers.
 * 
 * @author Denis Knoepfle
 * 
 */
public abstract class AbstractModelWrapper implements IModelWrapper {
	protected final ExtensionMetaobject extension;
	protected final String extensionName;

	public AbstractModelWrapper(ExtensionMetaobject extension) {
		this.extension = extension;
		this.extensionName = extension == null ? null : extension.getExtensionName();
	}

	@Override
	public String getExtensionName() {
		return extensionName;
	}

	@Override
	public String getName() {
		return SpotterUtils.extractConfigValue(getConfig(), NAME_KEY);
	}

	@Override
	public Set<ConfigParameterDescription> getExtensionConfigParams() {
		if (extension == null) {
			return new HashSet<ConfigParameterDescription>();
		}
		return extension.getConfigParams();
	}

	@Override
	public Boolean testConnection() throws Exception {
		if (extension == null) {
			return false;
		}
		ServiceClientWrapper client = Activator.getDefault().getClient(extension.getProjectName());
		if (SpotterUtils.hasConfigParameter(getConfig(), HOST_KEY)
				&& SpotterUtils.hasConfigParameter(getConfig(), PORT_KEY)) {
			String host = SpotterUtils.extractConfigValue(getConfig(), HOST_KEY);
			String port = SpotterUtils.extractConfigValue(getConfig(), PORT_KEY);
			Boolean connection = client.testConnectionToSattelite(extensionName, host, port);
			return connection;
		} else {
			return true;
		}

	}
}
