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

import java.util.Set;

import org.lpe.common.config.ConfigParameterDescription;

/**
 * This class contains relevant meta information about an extension such as its name
 * and its configuration parameter descriptions.
 */
public class ExtensionMetaobject {

	private final String projectName;
	private final String extensionName;
	private final Set<ConfigParameterDescription> configParams;
	
	public ExtensionMetaobject(String projectName, String extensionName, Set<ConfigParameterDescription> configParams) {
		this.projectName = projectName;
		this.extensionName = extensionName;
		this.configParams = configParams;
	}

	public String getProjectName() {
		return projectName;
	}
	
	public String getExtensionName() {
		return extensionName;
	}

	public Set<ConfigParameterDescription> getConfigParams() {
		return configParams;
	}

}
