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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.spotter.shared.environment.model.XMConfiguration;

/**
 * Object Factory for Jaxb.
 * @author Alexander Wert
 *
 */
@XmlRegistry
public class ObjectFactory {
	public static final QName HIERARCHY_QNAME = new QName("http://www.sopeco.org/PerformanceProblemHierarchySchema", "root");

	/**
	 * Creates a Performance Problem JAXB representation.
	 * @return representation of a performance problem
	 */
	public XPerformanceProblem createPerformanceProblem() {
		return new XPerformanceProblem();
	}

	/**
	 * Creates a configuration JAXB representation.
	 * @return a configuration representation
	 */
	public XMConfiguration createConfiguration() {
		return new XMConfiguration();
	}

	/**
	 * Creates the JAXB root node of a hierarchy.
	 * @param value performance problem to parse
	 * @return JAXB element
	 */
	@XmlElementDecl(namespace = "http://www.sopeco.org/PerformanceProblemHierarchySchema", name = "root")
	public JAXBElement<XPerformanceProblem> createProblem(XPerformanceProblem value) {
		return new JAXBElement<XPerformanceProblem>(HIERARCHY_QNAME, XPerformanceProblem.class, null, value);
	}
}
