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
package org.spotter.core.config.interpretation;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.lpe.common.extension.ExtensionRegistry;
import org.spotter.core.instrumentation.AbstractInstrumentationExtension;
import org.spotter.core.instrumentation.IInstrumentationAdapter;
import org.spotter.core.measurement.AbstractMeasurmentExtension;
import org.spotter.core.measurement.IMeasurementAdapter;
import org.spotter.core.workload.AbstractWorkloadExtension;
import org.spotter.core.workload.IWorkloadAdapter;
import org.spotter.shared.environment.model.ObjectFactory;
import org.spotter.shared.environment.model.XMConfiguration;
import org.spotter.shared.environment.model.XMeasurementEnvObject;
import org.spotter.shared.environment.model.XMeasurementEnvironment;

/**
 * Factory for creation of a measurement environment.
 * 
 * @author Alexander Wert
 * 
 */
public final class MeasurementEnvironmentFactory {
	private static MeasurementEnvironmentFactory instance;

	/**
	 * 
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
	 * Creates a list of {@link IMeasurementAdapter} instances corresponding
	 * to the measurement environment description of the passed file.
	 * 
	 * @param fileName
	 *            xml file describing the measruement environment
	 * @return list of {@link IMeasurementAdapter} instances
	 */
	public List<IMeasurementAdapter> createMeasurementControllers(String fileName) {
		List<IMeasurementAdapter> result = new ArrayList<IMeasurementAdapter>();
		XMeasurementEnvironment mEnv = parseXMLFile(fileName);
		if (mEnv != null && mEnv.getMeasurementController() != null) {

			for (XMeasurementEnvObject xController : mEnv.getMeasurementController()) {
				result.add(createMeasurementController(xController));
			}
		}
		return result;
	}

	/**
	 * Creates a list of {@link IInstrumentationAdapter} instances corresponding
	 * to the measurement environment description of the passed file.
	 * 
	 * @param fileName
	 *            xml file describing the measruement environment
	 * @return list of {@link IInstrumentationAdapter} instances
	 */
	public List<IInstrumentationAdapter> createInstrumentationControllers(String fileName) {
		List<IInstrumentationAdapter> result = new ArrayList<IInstrumentationAdapter>();
		XMeasurementEnvironment mEnv = parseXMLFile(fileName);
		if (mEnv != null && mEnv.getInstrumentationController() != null) {
			for (XMeasurementEnvObject xController : mEnv.getInstrumentationController()) {
				result.add(createInstrumentationController(xController));
			}
		}
		return result;
	}

	/**
	 * Creates a list of {@link IWorkloadAdapter} instances corresponding to the
	 * measurement environment description of the passed file.
	 * 
	 * @param fileName
	 *            xml file describing the measruement environment
	 * @return list of {@link IWorkloadAdapter} instances
	 */
	public List<IWorkloadAdapter> createWorkloadAdapters(String fileName) {
		List<IWorkloadAdapter> result = new ArrayList<IWorkloadAdapter>();
		XMeasurementEnvironment mEnv = parseXMLFile(fileName);
		if (mEnv != null && mEnv.getWorkloadAdapter() != null) {
			for (XMeasurementEnvObject xwlAdapter : mEnv.getWorkloadAdapter()) {
				result.add(createWorkloadAdapter(xwlAdapter));
			}
		}
		return result;
	}

	/**
	 * Reads the file from disk specified by the given fileName and parses it
	 * for creation of an {@link XMeasurementEnvironment}.
	 * 
	 * @param fileName
	 *            specifies the name of the xml file containing the measurement
	 *            environment description
	 * @return Returns the {@link XMeasurementEnvironment} object
	 */
	public XMeasurementEnvironment parseXMLFile(String fileName) {
		try {
			FileReader fileReader = new FileReader(fileName);
			JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
			Unmarshaller u = jc.createUnmarshaller();

			@SuppressWarnings("unchecked")
			XMeasurementEnvironment xRoot = ((JAXBElement<XMeasurementEnvironment>) u.unmarshal(fileReader)).getValue();

			return xRoot;
		} catch (Exception e) {
			throw new RuntimeException("Failed parsing measurement environment description xml file!", e);
		}
	}

	/**
	 * Creates an instance for the interface {@link IMeasurementAdapter}
	 * based on the information provided in the passed
	 * {@link XMeasurementController} object.
	 * 
	 * @param xController
	 *            construction information
	 * @return an instance for the interface {@link IMeasurementAdapter}
	 */
	private IMeasurementAdapter createMeasurementController(XMeasurementEnvObject xController) {
		try {
			IMeasurementAdapter controller = ExtensionRegistry.getSingleton().getExtensionArtifact(
					AbstractMeasurmentExtension.class, xController.getExtensionName());
			if (xController.getConfig() != null) {
				for (XMConfiguration xConfig : xController.getConfig()) {
					controller.getProperties().setProperty(xConfig.getKey(), xConfig.getValue());
				}
			}

			return controller;
		} catch (Exception e) {
			throw new RuntimeException("Failed parsing measurement environment description xml file!", e);
		}

	}

	/**
	 * Creates an instance for the interface {@link IInstrumentationAdapter}
	 * based on the information provided in the passed
	 * {@link XInstrumentationController} object.
	 * 
	 * @param xController
	 *            construction information
	 * @return an instance for the interface {@link IInstrumentationAdapter}
	 */
	private IInstrumentationAdapter createInstrumentationController(XMeasurementEnvObject xController) {
		try {
			IInstrumentationAdapter controller = ExtensionRegistry.getSingleton().getExtensionArtifact(
					AbstractInstrumentationExtension.class, xController.getExtensionName());
			if (xController.getConfig() != null) {
				for (XMConfiguration xConfig : xController.getConfig()) {
					controller.getProperties().setProperty(xConfig.getKey(), xConfig.getValue());
				}
			}

			return controller;
		} catch (Exception e) {
			throw new RuntimeException("Failed parsing measurement environment description xml file!", e);
		}

	}

	/**
	 * Creates an instance for the interface {@link IWorkloadAdapter} based on
	 * the information provided in the passed {@link XWorkloadAdapter} object.
	 * 
	 * @param xwlAdapter
	 *            construction information
	 * @return an instance for the interface {@link IWorkloadAdapter}
	 */
	private IWorkloadAdapter createWorkloadAdapter(XMeasurementEnvObject xwlAdapter) {
		try {
			IWorkloadAdapter adapter = ExtensionRegistry.getSingleton().getExtensionArtifact(
					AbstractWorkloadExtension.class, xwlAdapter.getExtensionName());
			if (xwlAdapter.getConfig() != null) {
				for (XMConfiguration xConfig : xwlAdapter.getConfig()) {
					adapter.getProperties().setProperty(xConfig.getKey(), xConfig.getValue());
				}
			}
			return adapter;
		} catch (Exception e) {
			throw new RuntimeException("Failed parsing measurement environment description xml file!", e);
		}

	}

}
