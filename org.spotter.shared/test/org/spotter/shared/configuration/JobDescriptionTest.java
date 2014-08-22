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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.shared.environment.model.XMeasurementEnvironmentTest;
import org.spotter.shared.hierarchy.model.XPerformanceProblemTest;
import org.spotter.shared.util.JAXBUtil;

/**
 * 
 * @author Denis Knoepfle
 * 
 */
public class JobDescriptionTest {

	private static final String XML_TEST_NAME = "testdescription";
	private static final String XML_FILE_ENDING = ".xml";
	private static ObjectFactory objectFactory;
	private static final Logger LOGGER = LoggerFactory.getLogger(JobDescriptionTest.class);

	private static ObjectFactory getFactory() {
		if (objectFactory == null) {
			objectFactory = new ObjectFactory();
		}
		return objectFactory;
	}

	private static Properties createDynamicSpotterConfig() {
		Properties config = getFactory().createDynamicSpotterConfig();
		config.put("org.spotter.resultDir", "results");
		return config;
	}

	@Test
	public void testXmlParsing() {
		String fileName = XML_TEST_NAME + new Date().getTime() + XML_FILE_ENDING;
		File file = new File(fileName);
		JobDescription description = createJobDescription();
		try {
			JAXBUtil.writeElementToFile(file, description);
			LOGGER.debug("created environment test file " + file.getAbsolutePath());
		} catch (JAXBException e) {
			Assert.fail("Failed to marshall environment object");
			if (file.exists() && file.delete()) {
				LOGGER.debug("deleted environment test file " + file.getAbsolutePath());
			}
		}

		JobDescription parsedDescription = null;
		try {
			parsedDescription = parseXMLFile(fileName);
			LOGGER.debug("parsed job description");
		} finally {
			if (file.delete()) {
				LOGGER.debug("deleted job description test file " + file.getAbsolutePath());
			}
		}

		if (parsedDescription == null) {
			Assert.fail("Failed to parse job description");
		} else if (parsedDescription.getDynamicSpotterConfig() == null) {
			Assert.fail("Failed to parse DS configuration");
		} else if (parsedDescription.getMeasurementEnvironment() == null) {
			Assert.fail("Failed to parse measurement environment");
		} else if (parsedDescription.getHierarchy() == null) {
			Assert.fail("Failed to parse hierarchy");
		}

		Assert.assertEquals(description.getDynamicSpotterConfig(), parsedDescription.getDynamicSpotterConfig());
		XMeasurementEnvironmentTest.assertEqualEnvironments(description.getMeasurementEnvironment(),
				parsedDescription.getMeasurementEnvironment());
		XPerformanceProblemTest.assertEqualHierarchies(description.getHierarchy(), parsedDescription.getHierarchy());
	}

	private JobDescription createJobDescription() {
		JobDescription description = getFactory().createJobDescription();

		description.setDynamicSpotterConfig(createDynamicSpotterConfig());
		description.setMeasurementEnvironment(XMeasurementEnvironmentTest.createMeasurementEnvironment());
		description.setHierarchy(XPerformanceProblemTest.createHierarchy());

		return description;
	}

	private JobDescription parseXMLFile(String fileName) {
		try {
			JobDescription xRoot = JAXBUtil.parseXMLFile(fileName, ObjectFactory.class.getPackage().getName());
			return xRoot;
		} catch (FileNotFoundException e) {
			Assert.fail("Could not find file! (" + fileName + ")");
		} catch (JAXBException e) {
			Assert.fail("Failed parsing job description xml file! (" + fileName + ")");
		}
		return null;
	}
}
