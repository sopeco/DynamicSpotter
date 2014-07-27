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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * JAXB factory.
 * 
 * @author Alexander Wert
 * 
 */
@XmlRegistry
public class ObjectFactory {

	/**
	 * Qname.
	 */
	public static final QName ENVIRONMENT_QNAME = new QName("org.spotter.shared.environment.model",
			"measurementEnvironment");

	/**
	 * Constructor.
	 */
	public ObjectFactory() {
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
	 * @return controller
	 */
	public XMeasurementEnvObject createMeasurementController() {
		return new XMeasurementEnvObject();
	}

	/**
	 * 
	 * @return controller
	 */
	public XMeasurementEnvObject createInstrumentationController() {
		return new XMeasurementEnvObject();
	}

	/**
	 * 
	 * @return controller
	 */
	public XMeasurementEnvObject createWorkloadAdapter() {
		return new XMeasurementEnvObject();
	}

	/**
	 * 
	 * @return config
	 */
	public XMConfiguration createConfiguration() {
		return new XMConfiguration();
	}

	/**
	 * 
	 * @param value
	 *            v
	 * @return JAXB
	 */
	@XmlElementDecl(namespace = "org.spotter.shared.environment.model", name = "measurementEnvironment")
	public JAXBElement<XMeasurementEnvironment> createEnvironment(XMeasurementEnvironment value) {
		return new JAXBElement<XMeasurementEnvironment>(ENVIRONMENT_QNAME, XMeasurementEnvironment.class, null, value);
	}
}
