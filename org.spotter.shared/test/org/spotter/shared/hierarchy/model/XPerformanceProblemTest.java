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
import java.util.Date;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.shared.environment.model.XMConfiguration;
import org.spotter.shared.util.JAXBUtil;

/**
 * 
 * @author Denis Knoepfle
 * 
 */
public class XPerformanceProblemTest {

	private static final String XML_TEST_NAME = "testhierarchy";
	private static final String XML_FILE_ENDING = ".xml";
	private static final Logger LOGGER = LoggerFactory.getLogger(XPerformanceProblemTest.class);

	@Test
	public void testXmlParsing() {
		String fileName = XML_TEST_NAME + new Date().getTime() + XML_FILE_ENDING;
		File file = new File(fileName);
		XPerformanceProblem hierarchy = createHierarchy();
		try {
			JAXBUtil.writeElementToFile(file, hierarchy);
			LOGGER.debug("created hierarchy test file " + file.getAbsolutePath());
		} catch (JAXBException e) {
			Assert.fail("Failed to marshal hierarchy object");
			if (file.exists() && file.delete()) {
				LOGGER.debug("deleted hierarchy test file " + file.getAbsolutePath());
			}
		}

		XPerformanceProblem parsedHierarchy = null;
		try {
			parsedHierarchy = parseXMLFile(fileName);
			LOGGER.debug("parsed hierarchy");
		} finally {
			if (file.delete()) {
				LOGGER.debug("deleted hierarchy test file " + file.getAbsolutePath());
			}
		}

		if (parsedHierarchy == null) {
			Assert.fail("Failed to parse hierarchy");
		} else if (parsedHierarchy.getUniqueId() == null) {
			Assert.fail("Failed to parse problem unique id");
		} else if (parsedHierarchy.getExtensionName() != null) {
			Assert.fail("Failed to parse extension name, expected null");
		} else if (parsedHierarchy.getConfig() == null) {
			Assert.fail("Failed to parse hierarchy root node config");
		} else if (parsedHierarchy.getProblem() != null) {
			Assert.fail("Failed to parse hierarchy problem list, expected null");
		}

		XPerformanceProblemTest.assertEqualHierarchies(hierarchy, parsedHierarchy);
	}

	/**
	 * Asserts that both hierarchies are equal.
	 * 
	 * @param o1
	 *            first hierarchy
	 * @param o2
	 *            second hierarchy
	 */
	public static void assertEqualHierarchies(XPerformanceProblem o1, XPerformanceProblem o2) {
		Assert.assertEquals(o1.getUniqueId(), o2.getUniqueId());
		Assert.assertEquals(o1.getExtensionName(), o2.getExtensionName());
		Assert.assertEquals(o1.getConfig().size(), o2.getConfig().size());
		
		XMConfiguration config1 = o1.getConfig().get(0);
		XMConfiguration config2 = o2.getConfig().get(0);
		Assert.assertEquals(config1.getKey(), config2.getKey());
		Assert.assertEquals(config1.getValue(), config2.getValue());
		
		Assert.assertEquals(o1.getProblem(), o2.getProblem());
	}

	/**
	 * Creates a hierarchy with no children.
	 * 
	 * @return the newly created measurement environment
	 */
	public static XPerformanceProblem createHierarchy() {
		XPerformanceProblem hierarchy = RawHierarchyFactory.getInstance().createEmptyHierarchy();

		return hierarchy;
	}

	private XPerformanceProblem parseXMLFile(String fileName) {
		try {
			XPerformanceProblem xRoot = JAXBUtil.parseXMLFile(fileName, ObjectFactory.class.getPackage().getName());
			return xRoot;
		} catch (FileNotFoundException e) {
			Assert.fail("Could not find file! (" + fileName + ")");
		} catch (JAXBException e) {
			Assert.fail("Failed parsing hierarchy description xml file! (" + fileName + ")");
		}
		return null;
	}
}
