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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.shared.environment.model.XMConfiguration;
import org.spotter.shared.util.JAXBUtil;

/**
 * A factory to create empty root instances of <code>XPerformanceProblem</code>
 * or instances that are parsed from a performance problem hierarchy XML file.
 * 
 * @author Denis Knoepfle
 * 
 */
public final class RawHierarchyFactory {

	/**
	 * The name of the default hierarchy configuration file.
	 */
	private static final String DEFAULT_HIERARCHY_FILENAME = "default-hierarchy.xml";

	private static final Logger LOGGER = LoggerFactory.getLogger(RawHierarchyFactory.class);

	private static RawHierarchyFactory instance;

	/**
	 * @return singleton instance
	 */
	public static RawHierarchyFactory getInstance() {
		if (instance == null) {
			instance = new RawHierarchyFactory();
		}
		return instance;
	}

	private RawHierarchyFactory() {
	}

	/**
	 * Reads the file from disk specified by the given <code>fileName</code> and
	 * parses it for creation of an {@link XPerformanceProblem}.
	 * 
	 * @param fileName
	 *            specifies the name of the XML file containing the performance
	 *            problem hierarchy
	 * @return the <code>XPerformanceProblem</code> root object
	 * @throws FileNotFoundException
	 *             when file cannot be found
	 * @throws JAXBException
	 *             when there is an error parsing the file
	 */
	public XPerformanceProblem parseHierarchyFile(String fileName) throws FileNotFoundException, JAXBException {
		XPerformanceProblem xRoot = JAXBUtil.parseXMLFile(fileName, ObjectFactory.class.getPackage().getName());

		return xRoot;
	}

	/**
	 * Creates a new performance problem hierarchy using the default hierarchy
	 * configuration file located in the root directory of the execution path.
	 * The default configuration has to be named
	 * {@value #DEFAULT_HIERARCHY_FILENAME}. If the file does not exist an empty
	 * hierarchy is returned.
	 * <p>
	 * This method is not intended to be called directly by clients, they should
	 * rather invoke the DS service using REST.
	 * </p>
	 * 
	 * @return the default hierarchy given by the configuration file or an empty
	 *         root instance if file can not be found or parsed
	 */
	public XPerformanceProblem createProblemHierarchyRoot() {
		File file = new File(DEFAULT_HIERARCHY_FILENAME);
		XPerformanceProblem root = null;
		try {
			root = parseHierarchyFile(DEFAULT_HIERARCHY_FILENAME);
		} catch (FileNotFoundException e) {
			LOGGER.info("Could not find the default hierarchy file '" + file.getAbsolutePath()
					+ "', using empty hierarchy instead!");
		} catch (JAXBException e) {
			LOGGER.warn("The default hierarchy file '" + file.getAbsolutePath()
					+ "' cannot be parsed, using empty hierarchy instead! Cause: {}", e.getMessage());
		}

		return root != null ? root : createEmptyHierarchy();
	}

	/**
	 * Creates an empty hierarchy. The root node is set to be non-detectable.
	 * 
	 * @return the root of an empty hierarchy
	 */
	public XPerformanceProblem createEmptyHierarchy() {
		XPerformanceProblem problem = new XPerformanceProblem();
		problem.setUniqueId(RawHierarchyFactory.generateUniqueId());
		problem.setConfig(new ArrayList<XMConfiguration>());

		XMConfiguration xmConfig = new XMConfiguration();
		xmConfig.setKey("org.spotter.detection.detectable");
		xmConfig.setValue(Boolean.toString(false));
		problem.getConfig().add(xmConfig);

		return problem;
	}

	/**
	 * Generates a unique 128-bit id using UUID.
	 * 
	 * @return the generated id
	 */
	public static String generateUniqueId() {
		return UUID.randomUUID().toString();
	}

}
