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
package org.spotter.shared.hierarchy.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.spotter.shared.environment.model.XMConfiguration;

/**
 * Performance problem JAXB representation.
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PerformanceProblem", propOrder = { "extensionName", "uniqueId", "config", "problem" })
@XmlRootElement(name = "root")
public class XPerformanceProblem implements Serializable {

	private static final long serialVersionUID = -3934559836199225205L;

	@XmlElement(name = "extensionName", required = false)
	private String extensionName;
	
	/**
	 * The unique id is always required and is used to identify
	 * problem instances.
	 */
	@XmlElement(name = "uniqueId", required = true)
	private String uniqueId;

	/**
	 * The configuration is always required. At least, the property
	 * if this node is detectable must be set.
	 */
	@XmlElement(name = "config", required = true)
	private List<XMConfiguration> config;

	/**
	 * A sub node for a problem is not required, as
	 * a {@link XPerformanceProblem} can be a root-cause. A
	 * root-cause of a performance problem does not contain
	 * more problems.
	 */
	@XmlElement(name = "problem", required = false)
	private List<XPerformanceProblem> problem;
	
	/**
	 * @return the extensionName
	 */
	public String getExtensionName() {
		return extensionName;
	}
	
	/**
	 * @param extensionName the extensionName to set
	 */
	public void setExtensionName(String extensionName) {
		this.extensionName = extensionName;
	}

	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}
	
	/**
	 * @param uniqueId the uniqueId to set
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	/**
	 * @return the config
	 */
	public List<XMConfiguration> getConfig() {
		return config;
	}
	
	/**
	 * @param config the config to set
	 */
	public void setConfig(List<XMConfiguration> config) {
		this.config = config;
	}
	
	/**
	 * @return the problem
	 */
	public List<XPerformanceProblem> getProblem() {
		return problem;
	}
	
	/**
	 * @param problem the problem to set
	 */
	public void setProblem(List<XPerformanceProblem> problem) {
		this.problem = problem;
	}

}
