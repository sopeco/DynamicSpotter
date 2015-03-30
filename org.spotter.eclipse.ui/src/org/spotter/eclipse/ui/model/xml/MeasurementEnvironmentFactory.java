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
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.UICoreException;
import org.spotter.eclipse.ui.util.SpotterUtils;
import org.spotter.shared.environment.model.ObjectFactory;
import org.spotter.shared.environment.model.XMeasurementEnvObject;
import org.spotter.shared.environment.model.XMeasurementEnvironment;
import org.spotter.shared.util.JAXBUtil;

/**
 * A factory to create empty instances of <code>XMeasurementEnvironment</code>
 * or to create a <code>XMeasurementEnvironment</code> by parsing a measurement
 * environment XML file.
 * 
 * @author Denis Knoepfle
 * 
 */
public final class MeasurementEnvironmentFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementEnvironmentFactory.class);

	private static MeasurementEnvironmentFactory instance;

	/**
	 * @return singleton instance
	 */
	public static MeasurementEnvironmentFactory getInstance() {
		if (instance == null) {
			instance = new MeasurementEnvironmentFactory();
		}
		return instance;
	}

	private MeasurementEnvironmentFactory() {
	}

	/**
	 * Reads the file from disk specified by the given <code>fileName</code> and
	 * parses it for creation of an {@link XMeasurementEnvironment}.
	 * 
	 * @param fileName
	 *            specifies the name of the XML file containing the measurement
	 *            environment description
	 * @return the <code>XMeasurementEnvironment</code> object
	 * @throws UICoreException
	 *             when either file could not be found or when there was an
	 *             error parsing the file
	 */
	public XMeasurementEnvironment parseXMLFile(String fileName) throws UICoreException {
		try {
			return JAXBUtil.parseXMLFile(fileName, ObjectFactory.class.getPackage().getName());
		} catch (FileNotFoundException e) {
			String message = "Could not find file '" + fileName + "'!";
			LOGGER.error(message);
			throw new UICoreException(message, e);
		} catch (JAXBException e) {
			String message = "Failed to parse measurement environment description file '" + fileName + "'!";
			LOGGER.error(message + " Cause: {}", e.getMessage());
			throw new UICoreException(message, e);
		}
	}

	/**
	 * Creates an empty instance of a measurement environment. This factory
	 * method initializes the fields with empty lists.
	 * 
	 * @return an empty instance
	 */
	public XMeasurementEnvironment createMeasurementEnvironment() {
		XMeasurementEnvironment env = new XMeasurementEnvironment();

		List<XMeasurementEnvObject> instrumentationControllers = new LinkedList<>();
		List<XMeasurementEnvObject> measurementControllers = new LinkedList<>();
		List<XMeasurementEnvObject> workloadAdapters = new LinkedList<>();

		env.setInstrumentationController(instrumentationControllers);
		env.setMeasurementController(measurementControllers);
		env.setWorkloadAdapter(workloadAdapters);

		return env;
	}

	/**
	 * Creates a copy of the given measurement environment object.
	 * 
	 * @param envObj
	 *            the object to copy
	 * @return the copy of the given object
	 */
	public XMeasurementEnvObject copyMeasurementEnvObject(XMeasurementEnvObject envObj) {
		XMeasurementEnvObject envObjCopy = new XMeasurementEnvObject();
		envObjCopy.setExtensionName(envObj.getExtensionName());
		if (envObj.getConfig() != null) {
			envObjCopy.setConfig(SpotterUtils.copyConfigurationList(envObj.getConfig()));
		}
		return envObjCopy;
	}

}