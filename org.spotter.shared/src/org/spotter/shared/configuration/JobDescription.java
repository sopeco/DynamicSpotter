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
package org.spotter.shared.configuration;

import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.spotter.shared.environment.model.XMeasurementEnvironment;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;

/**
 * A job description wraps all necessary configuration elements to perform a DS
 * diagnosis run.
 * 
 * @author Denis Knoepfle
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JobDescription", propOrder = { "dynamicSpotterConfig", "measurementEnvironment", "hierarchy" })
@XmlRootElement(name = "jobDescription")
public class JobDescription {

	private Properties dynamicSpotterConfig;
	private XMeasurementEnvironment measurementEnvironment;
	private XPerformanceProblem hierarchy;

	/**
	 * Returns the DynamicSpotter config.
	 * 
	 * @return the DynamicSpotter config.
	 */
	public Properties getDynamicSpotterConfig() {
		return dynamicSpotterConfig;
	}

	/**
	 * Sets the DynamicSpotter config.
	 * 
	 * @param dynamicSpotterConfig
	 *            the config to set
	 */
	public void setDynamicSpotterConfig(Properties dynamicSpotterConfig) {
		this.dynamicSpotterConfig = dynamicSpotterConfig;
	}

	/**
	 * Returns the measurement environment.
	 * 
	 * @return the measurement environment
	 */
	public XMeasurementEnvironment getMeasurementEnvironment() {
		return measurementEnvironment;
	}

	/**
	 * Sets the measurement environment.
	 * 
	 * @param measurementEnvironment
	 *            the measurement environment to set
	 */
	public void setMeasurementEnvironment(XMeasurementEnvironment measurementEnvironment) {
		this.measurementEnvironment = measurementEnvironment;
	}

	/**
	 * Returns the hierarchy.
	 * 
	 * @return the hierarchy
	 */
	public XPerformanceProblem getHierarchy() {
		return hierarchy;
	}

	/**
	 * Sets the hierarchy.
	 * 
	 * @param hierarchy
	 *            the hierarchy to set
	 */
	public void setHierarchy(XPerformanceProblem hierarchy) {
		this.hierarchy = hierarchy;
	}

}
