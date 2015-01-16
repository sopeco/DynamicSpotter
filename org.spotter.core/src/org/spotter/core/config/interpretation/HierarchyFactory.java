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

import org.lpe.common.extension.ExtensionRegistry;
import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.detection.IDetectionController;
import org.spotter.core.detection.IDetectionExtension;
import org.spotter.core.detection.IExperimentReuser;
import org.spotter.shared.environment.model.XMConfiguration;
import org.spotter.shared.hierarchy.model.RawHierarchyFactory;
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

		initializeExperimentReuser(rootProblem);

		return rootProblem;
	}

	/**
	 * Adapts data paths for detection controllers which reuse the experiments
	 * of its parent
	 */
	private void initializeExperimentReuser(PerformanceProblem rootProblem) {
		for (PerformanceProblem problem : rootProblem.getAllDEscendingProblems()) {
			IDetectionController controller = problem.getDetectionController();
			for (PerformanceProblem child : problem.getChildren()) {
				if (child.isDetectable()) {
					IDetectionController childController = child.getDetectionController();
					boolean reuser = Boolean.parseBoolean(childController.getProblemDetectionConfiguration()
							.getProperty(AbstractDetectionExtension.REUSE_EXPERIMENTS_FROM_PARENT, "false"));
					if (reuser) {
						controller.addExperimentReuser((IExperimentReuser) childController);
						childController.getResultManager().setParentIdentifier(
								controller.getResultManager().getControllerIdentifier());
					}
				}
			}
		}
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
			XPerformanceProblem xRoot = RawHierarchyFactory.getInstance().parseHierarchyFile(fileName);
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
		if (xProblem.getUniqueId() == null || xProblem.getUniqueId().isEmpty()) {
			throw new IllegalArgumentException(
					"Invalid Performance Problem Hierarchy XML: uniqueId is missing for a performance problem");
		}
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
