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
package org.spotter.eclipse.ui.model;

import java.util.HashSet;
import java.util.Set;

import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;

/**
 * This class contains relevant meta information about an extension such as its
 * name and its configuration parameter descriptions.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ExtensionMetaobject {

	private final String projectName;
	private final ServiceClientWrapper client;
	private final String extensionName;

	/**
	 * Create a new instance for the given project.
	 * 
	 * @param projectName
	 *            The project the extension is associated with
	 * @param extensionName
	 *            The name of the extension
	 */
	public ExtensionMetaobject(String projectName, String extensionName) {
		this.projectName = projectName;
		if (projectName == null) {
			this.client = null;
		} else {
			this.client = Activator.getDefault().getClient(projectName);
		}

		this.extensionName = extensionName;
	}

	/**
	 * @return the project name
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * @return the extension name
	 */
	public String getExtensionName() {
		return extensionName;
	}

	/**
	 * @return the configuration parameters
	 */
	public Set<ConfigParameterDescription> getConfigParams() {
		Set<ConfigParameterDescription> params = null;
		if (client != null) {
			params = client.getExtensionConfigParamters(extensionName);
		}

		return params != null ? params : new HashSet<ConfigParameterDescription>();
	}

}
