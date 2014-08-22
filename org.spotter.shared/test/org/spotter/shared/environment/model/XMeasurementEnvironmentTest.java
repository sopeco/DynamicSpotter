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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.shared.util.JAXBUtil;

/**
 * 
 * @author Denis Knoepfle
 *
 */
public class XMeasurementEnvironmentTest {

	private static final String XML_ENV_TEST_NAME = "testenvironment";
	private static final String XML_FILE_ENDING = ".xml";
	private static ObjectFactory objectFactory;
	private static final Logger LOGGER = LoggerFactory.getLogger(XMeasurementEnvironmentTest.class);

	private static ObjectFactory getFactory() {
		if (objectFactory == null) {
			objectFactory = new ObjectFactory();
		}
		return objectFactory;
	}

	@Test
	public void testXmlParsing() {
		String fileName = XML_ENV_TEST_NAME + new Date().getTime() + XML_FILE_ENDING;
		File file = new File(fileName);
		XMeasurementEnvironment env = createMeasurementEnvironment();
		try {
			JAXBUtil.writeElementToFile(file, env);
			LOGGER.debug("created environment test file " + file.getAbsolutePath());
		} catch (JAXBException e) {
			Assert.fail("Failed to marshall environment object");
			if (file.exists() && file.delete()) {
				LOGGER.debug("deleted environment test file " + file.getAbsolutePath());
			}
		}

		XMeasurementEnvironment parsedEnv = null;
		try {
			parsedEnv = parseXMLFile(fileName);
			LOGGER.debug("parsed measurement env");
		} finally {
			if (file.delete()) {
				LOGGER.debug("deleted environment test file " + file.getAbsolutePath());
			}
		}

		if (parsedEnv == null) {
			Assert.fail("Failed to parse measurement environment");
		} else if (parsedEnv.getInstrumentationController() == null) {
			Assert.fail("Failed to parse instrumentation satellite adapter");
		} else if (parsedEnv.getMeasurementController() == null) {
			Assert.fail("Failed to parse measurement satellite adapter");
		} else if (parsedEnv.getWorkloadAdapter() == null) {
			Assert.fail("Failed to parse workload satellite adapter");
		}

		XMeasurementEnvironmentTest.assertEqualEnvironments(env, parsedEnv);
	}

	/**
	 * Asserts that both environments are equal. Assumes that each list contains
	 * exactly one entry.
	 * 
	 * @param o1
	 *            first measurement environment
	 * @param o2
	 *            second measurement environment
	 */
	public static void assertEqualEnvironments(XMeasurementEnvironment o1, XMeasurementEnvironment o2) {
		XMeasurementEnvObject instrController1 = o1.getInstrumentationController().get(0);
		XMeasurementEnvObject instrController2 = o2.getInstrumentationController().get(0);
		Assert.assertTrue(compareMeasurementObjects(instrController1, instrController2));

		XMeasurementEnvObject measurementController1 = o1.getMeasurementController().get(0);
		XMeasurementEnvObject measurementController2 = o2.getMeasurementController().get(0);
		Assert.assertTrue(compareMeasurementObjects(measurementController1, measurementController2));

		XMeasurementEnvObject workloadAdapter1 = o1.getWorkloadAdapter().get(0);
		XMeasurementEnvObject workloadAdapter2 = o2.getWorkloadAdapter().get(0);
		Assert.assertTrue(compareMeasurementObjects(workloadAdapter1, workloadAdapter2));
	}

	/**
	 * Creates a measurement environment with each list having exactly one
	 * entry.
	 * 
	 * @return the newly created measurement environment
	 */
	public static XMeasurementEnvironment createMeasurementEnvironment() {
		XMeasurementEnvironment env = getFactory().createMeasurementEnvironment();

		List<XMeasurementEnvObject> instrumentationControllers = new LinkedList<>();
		instrumentationControllers.add(createInstrumentationController());

		List<XMeasurementEnvObject> measurementControllers = new LinkedList<>();
		measurementControllers.add(createMeasurementController());

		List<XMeasurementEnvObject> workloadAdapters = new LinkedList<>();
		workloadAdapters.add(createWorkloadAdapter());

		env.setInstrumentationController(instrumentationControllers);
		env.setMeasurementController(measurementControllers);
		env.setWorkloadAdapter(workloadAdapters);

		return env;
	}

	private static boolean compareMeasurementObjects(XMeasurementEnvObject o1, XMeasurementEnvObject o2) {
		if (o1 == null && o2 == null) {
			return true;
		} else if (o1 == null || o2 == null || !o1.getExtensionName().equals(o2.getExtensionName())) {
			return false;
		}
		if (o1.getConfig() == null && o2.getConfig() == null) {
			return true;
		} else if (o1.getConfig() == null || o2.getConfig() == null) {
			return false;
		}
		List<XMConfiguration> o2ConfigCopy = new ArrayList<>(o2.getConfig());
		for (XMConfiguration xmConfig : o1.getConfig()) {
			XMConfiguration foundConfig = null;
			for (XMConfiguration o2xmConfig : o2ConfigCopy) {
				if (xmConfig.getKey().equals(o2xmConfig.getKey())) {
					foundConfig = o2xmConfig;
					break;
				}
			}
			if (foundConfig != null) {
				if (xmConfig.getValue() != null && !xmConfig.getValue().equals(foundConfig.getValue())
						|| xmConfig.getValue() == null && foundConfig.getValue() != null) {
					return false;
				}
				o2ConfigCopy.remove(foundConfig);
			}
		}
		return true;
	}

	private static XMeasurementEnvObject createInstrumentationController() {
		XMeasurementEnvObject controller = getFactory().createInstrumentationController();
		controller.setExtensionName("instrumentation.dynamic.client");
		List<XMConfiguration> configList = new ArrayList<>();
		XMConfiguration name = getFactory().createConfiguration();
		name.setKey("org.spotter.satellite.name");
		name.setValue("Spotter Satellite Extension");

		XMConfiguration host = getFactory().createConfiguration();
		host.setKey("org.spotter.satellite.host");
		host.setValue("localhost");

		XMConfiguration port = getFactory().createConfiguration();
		port.setKey("org.spotter.satellite.port");
		port.setValue("8080");

		configList.add(name);
		configList.add(host);
		configList.add(port);
		controller.setConfig(configList);
		return controller;
	}

	private static XMeasurementEnvObject createMeasurementController() {
		XMeasurementEnvObject controller = getFactory().createMeasurementController();
		controller.setExtensionName("measurement.sampler.jmsserver");
		List<XMConfiguration> configList = new ArrayList<>();
		XMConfiguration name = getFactory().createConfiguration();
		name.setKey("org.spotter.satellite.name");
		name.setValue("Spotter Satellite Extension");

		XMConfiguration active = getFactory().createConfiguration();
		active.setKey("org.spotter.measurement.jmsserver.ActiveMQJMXUrl");

		XMConfiguration collectorType = getFactory().createConfiguration();
		collectorType.setKey("org.spotter.sampling.jmsserver.collectorType");

		configList.add(name);
		configList.add(active);
		configList.add(collectorType);
		controller.setConfig(configList);
		return controller;
	}

	private static XMeasurementEnvObject createWorkloadAdapter() {
		XMeasurementEnvObject controller = getFactory().createWorkloadAdapter();
		controller.setExtensionName("workload.adapter.simple");
		List<XMConfiguration> configList = new ArrayList<>();
		XMConfiguration name = getFactory().createConfiguration();
		name.setKey("org.spotter.satellite.name");
		name.setValue("Spotter Satellite Extension");

		XMConfiguration userScript = getFactory().createConfiguration();
		userScript.setKey("org.spotter.workload.simple.userScriptClassName");
		userScript.setValue("");

		XMConfiguration scriptPath = getFactory().createConfiguration();
		scriptPath.setKey("org.spotter.workload.simple.userScriptPath");
		scriptPath.setValue("");

		configList.add(name);
		configList.add(userScript);
		configList.add(scriptPath);
		controller.setConfig(configList);
		return controller;
	}

	private XMeasurementEnvironment parseXMLFile(String fileName) {
		try {
			XMeasurementEnvironment xRoot = JAXBUtil.parseXMLFile(fileName, ObjectFactory.class.getPackage().getName());
			return xRoot;
		} catch (FileNotFoundException e) {
			Assert.fail("Could not find file! (" + fileName + ")");
		} catch (JAXBException e) {
			Assert.fail("Failed parsing measurement environment description xml file! (" + fileName + ")");
		}
		return null;
	}
}
