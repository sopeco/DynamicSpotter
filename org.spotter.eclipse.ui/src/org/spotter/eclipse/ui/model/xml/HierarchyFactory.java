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
package org.spotter.eclipse.ui.model.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.shared.environment.model.XMConfiguration;
import org.spotter.shared.hierarchy.model.ObjectFactory;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;

/**
 * A factory to create empty root instances of <code>XPerformanceProblem</code>
 * or instances that are parsed from a performance problem hierarchy XML file.
 * 
 * @author Denis Knoepfle
 * 
 */
public final class HierarchyFactory {

	/**
	 * The name of the default hierarchy configuration file.
	 */
	private static final String DEFAULT_HIERARCHY_FILENAME = "default-hierarchy.xml";

	private static final Logger LOGGER = LoggerFactory.getLogger(HierarchyFactory.class);

	private static HierarchyFactory instance;

	/**
	 * @return singleton instance
	 */
	public static HierarchyFactory getInstance() {
		if (instance == null) {
			instance = new HierarchyFactory();
		}
		return instance;
	}

	private HierarchyFactory() {
	}

	/**
	 * Reads the file from disk specified by the given <code>fileName</code> and
	 * parses it for creation of an {@link XPerformanceProblem}.
	 * 
	 * @param fileName
	 *            specifies the name of the XML file containing the performance
	 *            problem hierarchy
	 * @return the <code>XPerformanceProblem</code> root object
	 * @throws IllegalArgumentException
	 *             when either file could not be found or when there was an
	 *             error parsing the file
	 */
	public XPerformanceProblem parseHierarchyFile(String fileName) throws IllegalArgumentException {
		try {
			FileReader fileReader = new FileReader(fileName);
			JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
			Unmarshaller u = jc.createUnmarshaller();

			@SuppressWarnings("unchecked")
			JAXBElement<XPerformanceProblem> element = (JAXBElement<XPerformanceProblem>) u.unmarshal(fileReader);
			XPerformanceProblem xRoot = element.getValue();

			return xRoot;
		} catch (FileNotFoundException e) {
			String msg = "Could not find file '" + fileName + "'!";
			LOGGER.error(msg + ", " + e.getMessage());
			throw new IllegalArgumentException(msg, e);
		} catch (JAXBException e) {
			String msg = "Failed parsing performance problem hierarchy file '" + fileName + "'";
			LOGGER.error(msg + ", " + e.getMessage());
			throw new IllegalArgumentException(msg, e);
		}
	}

	/**
	 * Creates a new performance problem hierarchy using the default hierarchy
	 * configuration file located in the root directory of the execution path of
	 * the UI. The default configuration has to be named
	 * {@value #DEFAULT_HIERARCHY_FILENAME}. If the file does not exist an empty
	 * hierarchy is returned.
	 * 
	 * @return the default hierarchy given by the configuration file or an empty
	 *         root instance if file can not be parsed
	 */
	public XPerformanceProblem createProblemHierarchyRoot() {
		File file = new File(DEFAULT_HIERARCHY_FILENAME);
		XPerformanceProblem root;
		try {
			root = parseHierarchyFile(DEFAULT_HIERARCHY_FILENAME);
		} catch (IllegalArgumentException e) {
			String msg = "Could not load the default hierarchy file '" + file.getAbsolutePath()
					+ "', using empty hierarchy instead! Cause: " + e.getMessage();
			LOGGER.warn(msg);
			root = createEmptyHierarchy();
		}

		return root;
	}

	/**
	 * Creates an empty hierarchy.
	 * 
	 * @return The root of an empty hierarchy.
	 */
	private XPerformanceProblem createEmptyHierarchy() {
		XPerformanceProblem problem = new XPerformanceProblem();
		problem.setConfig(new ArrayList<XMConfiguration>());
		XMConfiguration xmConfig = new XMConfiguration();
		xmConfig.setKey("org.spotter.detection.detectable");
		xmConfig.setValue(Boolean.toString(false));
		problem.getConfig().add(xmConfig);
		return problem;
	}

}
