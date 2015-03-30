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

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.UICoreException;
import org.spotter.eclipse.ui.util.SpotterUtils;
import org.spotter.shared.hierarchy.model.RawHierarchyFactory;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;

/**
 * A factory that wraps the RawHierarchyFactory for the UI, used to parse an
 * instance of <code>XPerformanceProblem</code> from a performance problem
 * hierarchy XML file.
 * 
 * @author Denis Knoepfle
 * 
 */
public final class HierarchyFactory {

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
	 * @throws UICoreException
	 *             when either file could not be found or when there was an
	 *             error parsing the file
	 */
	public XPerformanceProblem parseHierarchyFile(String fileName) throws UICoreException {
		try {
			XPerformanceProblem xRoot = RawHierarchyFactory.getInstance().parseHierarchyFile(fileName);
			return xRoot;
		} catch (FileNotFoundException e) {
			String message = "Could not find file '" + fileName + "'!";
			LOGGER.error(message);
			throw new UICoreException(message, e);
		} catch (JAXBException e) {
			String message = "Failed to parse performance problem hierarchy file '" + fileName + "'!";
			LOGGER.error(message + " Cause: {}", e.getMessage());
			throw new UICoreException(message, e);
		}
	}

	/**
	 * Copies the given performance problem. All unique keys are replaced by new
	 * ones. Children are not copied.
	 * 
	 * @param problem
	 *            the problem to copy
	 * @return the copied problem
	 */
	public XPerformanceProblem copyPerformanceProblem(XPerformanceProblem problem) {
		XPerformanceProblem copy = new XPerformanceProblem();
		copy.setExtensionName(problem.getExtensionName());
		copy.setUniqueId(RawHierarchyFactory.generateUniqueId());

		if (problem.getConfig() != null) {
			copy.setConfig(SpotterUtils.copyConfigurationList(problem.getConfig()));
		}

		return copy;
	}

}
