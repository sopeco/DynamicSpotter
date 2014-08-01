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

	/**
	 * Constructor sets extension and extension name.
	 * 
	 * @param extension
	 *            the wrapped extension
	 */
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

		if (SpotterUtils.hasConfigParameter(getConfig(), HOST_KEY)
				&& SpotterUtils.hasConfigParameter(getConfig(), PORT_KEY)) {

			return testRemoteConnection(SpotterUtils.extractConfigValue(getConfig(), HOST_KEY),
					SpotterUtils.extractConfigValue(getConfig(), PORT_KEY));

		}

		boolean mandatoryFileDirectory = testMandatoryFileDirectory();

		return mandatoryFileDirectory;
	}

	/**
	 * Checks all the mandatory {@link ConfigParameterDescription}s in the
	 * configuration with the types of files or directories. The values for each
	 * of theses entries must be non-<code>null</code>.<br />
	 * This does not check the correctness of the values.
	 * 
	 * @return true, if all files and directories are filled with values
	 */
	private boolean testMandatoryFileDirectory() {
		for (ConfigParameterDescription param : getExtensionConfigParams()) {
			if (param.isMandatory()) {
				if (param.isADirectory() || param.isAFile()) {

					// the value of mandatories files and directories
					// must not be empty
					String paramValue = SpotterUtils.extractConfigValue(getConfig(), param.getName());

					if (paramValue == null || paramValue.isEmpty()) {
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Checks if a remote connection to the given host and port can be
	 * established.
	 * 
	 * @return <code>true</code>, if a connection was successful established
	 */
	private boolean testRemoteConnection(String host, String port) {
		ServiceClientWrapper client = Activator.getDefault().getClient(extension.getProjectName());
		boolean connection = client.testConnectionToSattelite(extensionName, host, port);

		return connection;
	}
}
