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
package org.spotter.shared.environment.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Measurement Environment Object Class for XML parsing.
 * 
 * @author Alexander Wert
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MeasurementEnvObject", propOrder = { "extensionName", "config" })
public class XMeasurementEnvObject {

	@XmlElement(name = "extensionName", required = true)
	private String extensionName;

	private List<XMConfiguration> config;

	/**
	 * @return the extensionName
	 */
	public String getExtensionName() {
		return extensionName;
	}

	/**
	 * @param extensionName
	 *            the extensionName to set
	 */
	public void setExtensionName(String extensionName) {
		this.extensionName = extensionName;
	}

	/**
	 * @return the config
	 */
	public List<XMConfiguration> getConfig() {
		return config;
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public void setConfig(List<XMConfiguration> config) {
		this.config = config;
	}

}
