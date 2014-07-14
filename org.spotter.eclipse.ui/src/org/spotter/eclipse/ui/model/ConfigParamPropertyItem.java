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

import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.model.xml.IModelWrapper;
import org.spotter.shared.environment.model.XMConfiguration;

/**
 * This property item represents a configuration parameter.
 */
public class ConfigParamPropertyItem extends AbstractPropertyItem {

	private final XMConfiguration xmConfig;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param modelWrapper
	 *            the model wrapper of the property item
	 * @param confParamDesc
	 *            the parameter description of this property item
	 * @param xmConfig
	 *            the concrete <code>XMConfiguration</code> of this property item
	 */
	public ConfigParamPropertyItem(IModelWrapper modelWrapper, ConfigParameterDescription confParamDesc,
			XMConfiguration xmConfig) {
		super(modelWrapper, confParamDesc);
		this.xmConfig = xmConfig;
	}

	@Override
	public String getValue() {
		String value = null;
		if (xmConfig != null) {
			value = xmConfig.getValue();
		}
		return value == null ? "" : value;
	}

	@Override
	public void updateValue(String value) {
		if (xmConfig != null) {
			xmConfig.setValue(value);
		}
	}

	/**
	 * @return the encapsulated <code>XMConfiguration</code>
	 */
	public XMConfiguration getXMConfig() {
		return xmConfig;
	}

}
