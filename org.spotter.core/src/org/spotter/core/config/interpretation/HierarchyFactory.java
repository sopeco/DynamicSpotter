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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.lpe.common.extension.ExtensionRegistry;
import org.spotter.core.detection.IDetectionController;
import org.spotter.core.detection.IDetectionExtension;
import org.spotter.shared.environment.model.XMConfiguration;
import org.spotter.shared.hierarchy.model.ObjectFactory;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;
import org.spotter.shared.result.model.ResultsContainer;


/**
 * Singleton Factory for creating a hierarchy object from an XML hierarchy file.
 * 
 * @author Alexander Wert
 * 
 */
public final class HierarchyFactory {

	private static HierarchyFactory instance;

	/**
	 * 
	 * @return the singleton instance of the factory.
	 */
	public static HierarchyFactory getInstance() {
		if (instance == null) {
			instance = new HierarchyFactory();
		}
		return instance;
	}

	/**
	 * Private constructor according to the singleton pattern.
	 */
	private HierarchyFactory() {
	}

	/**
	 * Crates a performance problem hierarchy object from the file specified by
	 * the passed path.
	 * 
	 * @param fileName
	 *            path to the XML file representing the hierarchy to be created
	 * @param resultsContainer
	 *            container in which to store the original root problem
	 * @return the root element (performance problem) of the hierarchy that has
	 *         been created
	 */
	public PerformanceProblem createPerformanceProblemHierarchy(String fileName, ResultsContainer resultsContainer) {
		PerformanceProblem rootProblem = parsePPHFile(fileName, resultsContainer);

		for (PerformanceProblem problem : rootProblem.getAllDEscendingProblems()) {
			if (problem.isDetectable()) {
				IDetectionController controller = ExtensionRegistry.getSingleton().getExtensionArtifact(
						IDetectionExtension.class, problem.getProblemName());
				if (controller == null) {
					throw new RuntimeException("Could not find a detection controller for performance problem "
							+ problem.getProblemName());
				}
				problem.setDetectionController(controller);
				controller.setProblemDetectionConfiguration(problem.getConfiguration());
				controller.loadProperties();
			}

		}
		return rootProblem;
	}

	/**
	 * Reads the file from disk specified by the given fileName and parses it
	 * for creation of an performance problem hierarchy.
	 * 
	 * @param fileName
	 *            specifies the name of the xml file containing the performance
	 *            problem hierarchy
	 * @param resultsContainer
	 *            container in which to store the original root problem
	 * @return Returns the root performance problem
	 */
	private PerformanceProblem parsePPHFile(String fileName, ResultsContainer resultsContainer) {
		try {
			FileReader fileReader = new FileReader(fileName);
			JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
			Unmarshaller u = jc.createUnmarshaller();

			@SuppressWarnings("unchecked")
			XPerformanceProblem xRoot = ((JAXBElement<XPerformanceProblem>) u.unmarshal(fileReader)).getValue();
			resultsContainer.setRootProblem(xRoot);

			return visitProblem(xRoot);
		} catch (Exception e) {
			throw new RuntimeException("Failed parsing performance problem hierarchy xml file!");
		}
	}

	/**
	 * Visits the XML element {@link XPerformanceProblem} and creates the
	 * corresponding Java object.
	 * 
	 * @param xProblem
	 *            element to be visited
	 * @return a {@link PerformanceProblem} instance
	 */
	private PerformanceProblem visitProblem(XPerformanceProblem xProblem) {
		PerformanceProblem problem = new PerformanceProblem(xProblem.getUniqueId());
		problem.setProblemName(xProblem.getExtensionName());
		if (xProblem.getProblem() != null) {
			for (XPerformanceProblem xChild : xProblem.getProblem()) {
				problem.getChildren().add(visitProblem(xChild));
			}
		}
		if (xProblem.getConfig() != null) {
			for (XMConfiguration config : xProblem.getConfig()) {
				problem.getConfiguration().setProperty(config.getKey(), config.getValue());
			}
		}
		return problem;
	}

}
