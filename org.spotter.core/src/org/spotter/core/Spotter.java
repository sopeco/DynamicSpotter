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
package org.spotter.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.util.List;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.LpeNumericUtils;
import org.lpe.common.util.system.LpeSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.core.config.interpretation.HierarchyFactory;
import org.spotter.core.config.interpretation.HierarchyModelInterpreter;
import org.spotter.core.config.interpretation.MeasurementEnvironmentFactory;
import org.spotter.core.config.interpretation.PerformanceProblem;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;
import org.spotter.core.instrumentation.ISpotterInstrumentation;
import org.spotter.core.instrumentation.InstrumentationBroker;
import org.spotter.core.measurement.IMeasurementController;
import org.spotter.core.measurement.MeasurementBroker;
import org.spotter.core.result.ResultBlackboard;
import org.spotter.core.workload.IWorkloadAdapter;
import org.spotter.core.workload.WorkloadAdapterBroker;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.configuration.ConfigCheck;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.result.ResultsLocationConstants;
import org.spotter.shared.result.model.ResultsContainer;
import org.spotter.shared.result.model.SpotterResult;
import org.spotter.shared.status.DiagnosisStatus;
import org.spotter.shared.status.SpotterProgress;

/**
 * Main Controller for Performance Problem Diagnostics.
 * 
 * @author Alexander Wert
 * 
 */
public final class Spotter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Spotter.class);

	private static Spotter instance;

	/**
	 * 
	 * @return singleton instance
	 */
	public static synchronized Spotter getInstance() {
		if (instance == null) {
			instance = new Spotter();
		}
		return instance;
	}

	private SpotterProgress progress;
	private ProgressUpdater progressUpdater;

	/**
	 * Constructor.
	 */
	private Spotter() {

	}

	/**
	 * Executes diagnostics process.
	 * 
	 * @param configurationFile
	 *            path to the configuration file
	 */
	public void startDiagnosis(String configurationFile) {
		long startTime = System.currentTimeMillis();

		startDiagnosis(configurationFile, startTime);
	}

	/**
	 * Executes diagnostics process.
	 * 
	 * @param configurationFile
	 *            path to the configuration file
	 * @param timestamp
	 *            timestamp of that run
	 */
	public synchronized void startDiagnosis(String configurationFile, long timestamp) {

		GlobalConfiguration.reinitialize(configurationFile);
		setProgress(new SpotterProgress());
		progressUpdater = new ProgressUpdater();
		ResultsContainer resultsContainer = new ResultsContainer();
		try {

			GlobalConfiguration.getInstance().putProperty(ConfigKeys.PPD_RUN_TIMESTAMP, String.valueOf(timestamp));
			ConfigCheck.checkConfiguration();
			initializeMeasurementEnvironment();
			PerformanceProblem problem = retrieveRootPerformanceProblem(resultsContainer);
			HierarchyModelInterpreter hierarchyModelInterpreter = new HierarchyModelInterpreter(problem);
			problem = hierarchyModelInterpreter.next();

			LpeSystemUtils.submitTask(progressUpdater);

			while (problem != null) {
				IDetectionController detectionController = problem.getDetectionController();
				progressUpdater.setController((AbstractDetectionController) detectionController);
				SpotterResult result = detectionController.analyzeProblem();
				if (result.isDetected()) {
					progressUpdater.updateProgressStatus(problem.getProblemName(), DiagnosisStatus.DETECTED);
				} else {
					progressUpdater.updateProgressStatus(problem.getProblemName(), DiagnosisStatus.NOT_DETECTED);
				}

				ResultBlackboard.getInstance().putResult(problem, result);
				problem = hierarchyModelInterpreter.next();
			}

		} catch (Exception e) {
			LOGGER.error("Error during Performance Problem Diagnostics. Cause: {}", e);
			e.printStackTrace();
		} finally {
			progressUpdater.stop();
			long durationMillis = ((System.currentTimeMillis() - timestamp));

			resultsContainer.setResultsMap(ResultBlackboard.getInstance().getResults());
			printResults(durationMillis);
			serializeResults(resultsContainer);
			ResultBlackboard.getInstance().reset();
		}

	}

	/**
	 * Reads the performance problem hierarchy file and returns the root
	 * performance problem of that hierarchy.
	 * 
	 * @param resultsContainer
	 *            container in which to store the original root problem
	 * @return the root problem
	 */
	private PerformanceProblem retrieveRootPerformanceProblem(ResultsContainer resultsContainer) {
		String hierarchyFileName = GlobalConfiguration.getInstance()
				.getProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE);
		if (hierarchyFileName == null || !new File(hierarchyFileName).exists()) {
			throw new IllegalArgumentException(
					"Please provide a proper configuration for the performance problem hierarchy file!");
		}

		PerformanceProblem problem = HierarchyFactory.getInstance().createPerformanceProblemHierarchy(
				hierarchyFileName, resultsContainer);
		return problem;
	}

	/**
	 * Writes the Spotter report.
	 * 
	 * @param durationMillis
	 *            time in milli seconds the diagnostics took
	 */
	private void printResults(long durationMillis) {
		StringBuilder builder = new StringBuilder();
		builder.append("PPD analysis took ");
		builder.append(LpeNumericUtils.formatTimeMillis(durationMillis));
		builder.append(System.getProperty("line.separator"));
		builder.append(System.getProperty("line.separator"));
		builder.append(ResultBlackboard.getInstance().toString());
		String resultString = builder.toString();

		String outputFile = GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR)
				+ ResultsLocationConstants.TXT_REPORT_FILE_NAME;

		try {
			FileWriter fstream = new FileWriter(outputFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(resultString);
			out.close();
		} catch (Exception e) {
			LOGGER.error("Failed writing result report to file {}! ", outputFile);
		}

		LOGGER.info("Spotter analysis finished! Report is written to the following file: {}", outputFile);
	}

	/**
	 * Serializes the results to file.
	 * 
	 * @param resultsContainer
	 *            container with the collected results to serialize
	 */
	private void serializeResults(ResultsContainer resultsContainer) {
		String outputFile = GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR)
				+ ResultsLocationConstants.RESULTS_SERIALIZATION_FILE_NAME;
		try {
			FileOutputStream fileOut = new FileOutputStream(outputFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(resultsContainer);
			out.close();
			fileOut.close();
		} catch (Exception e) {
			LOGGER.error("Failed serializing results to file {}! ", outputFile);
		}

		LOGGER.info("Serialized results to the following file: {}", outputFile);
	}

	/**
	 * @return the progress
	 */
	public SpotterProgress getProgress() {
		return progress;
	}

	/**
	 * @param progress
	 *            the progress to set
	 */
	public void setProgress(SpotterProgress progress) {
		this.progress = progress;
	}

	/**
	 * Initializes the measurement environment. Reads the environment
	 * description from XML file and creates corresponding Java objects
	 */
	private void initializeMeasurementEnvironment() throws InstrumentationException, MeasurementException,
			WorkloadException {
		initInstrumentationController();

		initMeasurementController();

		initWorkloadAdapter();
	}

	private void initWorkloadAdapter() throws WorkloadException {
		String measurementEnvironmentFile = GlobalConfiguration.getInstance().getProperty(
				ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE);
		if (measurementEnvironmentFile == null) {
			throw new WorkloadException("Measurement Environment File has not been specified!");
		}
		List<IWorkloadAdapter> wlAdapters = MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(
				measurementEnvironmentFile);
		WorkloadAdapterBroker workloadAdapter = WorkloadAdapterBroker.getInstance();
		workloadAdapter.setControllers(wlAdapters);
		workloadAdapter.initialize();
	}

	private void initMeasurementController() throws InstrumentationException, MeasurementException {
		String measurementEnvironmentFile = GlobalConfiguration.getInstance().getProperty(
				ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE);
		if (measurementEnvironmentFile == null) {
			throw new InstrumentationException("Measurement Environment File has not been specified!");
		}
		List<IMeasurementController> controllers = MeasurementEnvironmentFactory.getInstance()
				.createMeasurementControllers(measurementEnvironmentFile);
		MeasurementBroker measurementController = MeasurementBroker.getInstance();
		measurementController.setControllers(controllers);
		measurementController.initialize();

	}

	private void initInstrumentationController() throws InstrumentationException {

		String measurementEnvironmentFile = GlobalConfiguration.getInstance().getProperty(
				ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE);
		if (measurementEnvironmentFile == null) {
			throw new InstrumentationException("Measurement Environment File has not been specified!");
		}
		List<ISpotterInstrumentation> instrumentations = MeasurementEnvironmentFactory.getInstance()
				.createInstrumentationControllers(measurementEnvironmentFile);
		InstrumentationBroker instrumentationController = InstrumentationBroker.getInstance();
		instrumentationController.setControllers(instrumentations);
		instrumentationController.initialize();
	}

	public ProgressUpdater getProgressUpdater() {
		return progressUpdater;
	}

	public void setProgressUpdater(ProgressUpdater progressUpdater) {
		this.progressUpdater = progressUpdater;
	}

}
