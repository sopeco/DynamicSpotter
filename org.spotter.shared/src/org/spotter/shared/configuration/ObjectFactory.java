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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.spotter.shared.environment.model.XMeasurementEnvironment;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;

/**
 * JAXB factory.
 * 
 * @author Denis Knoepfle
 * 
 */
@XmlRegistry
public class ObjectFactory {

	/**
	 * Qname.
	 */
	public static final QName DESCRIPTION_QNAME = new QName("org.spotter.shared.configuration", "jobDescription");

	/**
	 * Constructor.
	 */
	public ObjectFactory() {
	}

	/**
	 * 
	 * @return job description
	 */
	public JobDescription createJobDescription() {
		return new JobDescription();
	}

	/**
	 * 
	 * @return DS config
	 */
	public Properties createDynamicSpotterConfig() {
		return new Properties();
	}

	/**
	 * 
	 * @return environment
	 */
	public XMeasurementEnvironment createMeasurementEnvironment() {
		return new XMeasurementEnvironment();
	}

	/**
	 * 
	 * @return hierarchy
	 */
	public XPerformanceProblem createHierarchy() {
		return new XPerformanceProblem();
	}

	/**
	 * 
	 * @param value
	 *            v
	 * @return JAXB
	 */
	@XmlElementDecl(namespace = "org.spotter.shared.configuration", name = "jobDescription")
	public JAXBElement<JobDescription> createDescription(JobDescription value) {
		return new JAXBElement<JobDescription>(DESCRIPTION_QNAME, JobDescription.class, null, value);
	}
}
