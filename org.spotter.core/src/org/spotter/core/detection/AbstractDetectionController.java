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
package org.spotter.core.detection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.instrumentation.description.InstrumentationDescription;
import org.aim.api.instrumentation.description.InstrumentationDescriptionBuilder;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.api.measurement.dataset.Parameter;
import org.aim.api.measurement.utils.RecordCSVReader;
import org.aim.api.measurement.utils.RecordCSVWriter;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.extension.AbstractExtensionArtifact;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.LpeFileUtils;
import org.lpe.common.util.LpeStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.core.Spotter;
import org.spotter.core.config.interpretation.MeasurementEnvironmentFactory;
import org.spotter.core.instrumentation.ISpotterInstrumentation;
import org.spotter.core.instrumentation.InstrumentationBroker;
import org.spotter.core.measurement.IMeasurementController;
import org.spotter.core.measurement.MeasurementBroker;
import org.spotter.core.workload.IWorkloadAdapter;
import org.spotter.core.workload.WorkloadAdapterBroker;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.result.ResultsLocationConstants;
import org.spotter.shared.result.model.SpotterResult;
import org.spotter.shared.status.DiagnosisStatus;

import com.xeiam.xchart.BitmapEncoder;
import com.xeiam.xchart.Chart;

/**
 * The {@link AbstractDetectionController} comprises common functionality of all
 * detection controller classes, like initialization, result persistance, etc.
 * 
 * @author Alexander Wert
 * 
 */
public abstract class AbstractDetectionController extends AbstractExtensionArtifact implements IDetectionController {
	private static final double EPSELON = 0.5;

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDetectionController.class);

	/**
	 * property key for detection name.
	 */
	public static final long KILO = 1000L;
	public static final String DETECTABLE_KEY = "org.spotter.detection.detectable";
	private static final int SUT_WARMPUP_DURATION = GlobalConfiguration.getInstance().getPropertyAsInteger(ConfigKeys.PREWARUMUP_DURATION, 180);
	private static final int MIN_NUM_USERS = 1;
	protected static final String NUMBER_OF_USERS = "numUsers";
	protected static final String EXPERIMENT_STEPS_KEY = "numExperimentSteps";
	private static final int DPI = 300;

	private ISpotterInstrumentation instrumentationController;
	protected IMeasurementController measurementController;
	protected IWorkloadAdapter workloadAdapter;
	protected boolean instrumented = false;
	private boolean sutWarmedUp = false;
	private Properties problemDetectionConfiguration = new Properties();
	private int experimentCount = 0;
	private String dataPath;
	private String resourcePath;

	private List<IExperimentReuser> experimentReuser;
	private String parentDataDir;

	protected long measurementRampUpTime;
	private long estimatedDuration = 0L;
	protected long additionalDuration = 0L;

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *            Provider of the extension.
	 */
	public AbstractDetectionController(IExtension<IDetectionController> provider) {
		super(provider);
		experimentReuser = new ArrayList<>();
	}

	@Override
	public SpotterResult analyzeProblem() throws InstrumentationException, MeasurementException, WorkloadException {
		try {
			if (!GlobalConfiguration.getInstance().getPropertyAsBoolean(ConfigKeys.OMIT_WARMUP, false)) {
				additionalDuration += SUT_WARMPUP_DURATION;
			}
			calculateInitialEstimatedDuration();
			Spotter.getInstance().getProgress()
					.updateProgressStatus(getProvider().getName(), DiagnosisStatus.INITIALIZING);

			if (GlobalConfiguration.getInstance().getPropertyAsBoolean(ConfigKeys.OMIT_EXPERIMENTS, false)) {
				overwriteDataPath(GlobalConfiguration.getInstance().getProperty(ConfigKeys.DUMMY_EXPERIMENT_DATA));
			} else if (this instanceof IExperimentReuser) {
				overwriteDataPath(parentDataDir);
			} else {

				initialize();
				if (!GlobalConfiguration.getInstance().getPropertyAsBoolean(ConfigKeys.OMIT_WARMUP, false)) {
					warmUpSUT();
				}

				executeExperiments();
			}

			Spotter.getInstance().getProgress()
					.updateProgressStatus(getProvider().getName(), DiagnosisStatus.ANALYSING);
			return analyze(loadData());
		} catch (Exception e) {
			if (e instanceof InstrumentationException) {
				throw (InstrumentationException) e;
			} else {
				if (instrumented) {
					instrumentationController.uninstrument();
				}
				instrumented = false;

				String message = "Error during problem analysis by " + this.getClass().getSimpleName()
						+ ". Ignoring and resuming!";
				LOGGER.warn(message + " Cause: {}", e);
				SpotterResult result = new SpotterResult();
				result.addMessage(message);
				result.setDetected(false);
				return result;
			}

		}

	}

	/**
	 * Updates the current progress of this controller.
	 */
	public void updateEstimatedProgress() {
		long elapsedTime = (System.currentTimeMillis() - GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.PPD_RUN_TIMESTAMP, 0L))
				/ KILO;

		long currentEstimatedOverallDuration = getEstimatedOverallDuration();

		// as the estimated overall duration might not have been calculated yet
		// and return default

		// value 0, it must be checked to be greater 0
		if (currentEstimatedOverallDuration > 0) {
			Spotter.getInstance()
					.getProgress()
					.updateProgress(getProvider().getName(), (double) (elapsedTime / getEstimatedOverallDuration()),
							getEstimatedOverallDuration() - elapsedTime);
		}

	
	}

	/**
	 * This method triggers the load generators to put low load on the system
	 * under test in order to warm it up. E.g. all required classes of the SUT
	 * should be loaded.
	 * 
	 * @throws WorkloadException
	 */
	private void warmUpSUT() throws WorkloadException {
		if (!sutWarmedUp) {
			Spotter.getInstance().getProgress().updateProgressStatus(getProvider().getName(), DiagnosisStatus.WARM_UP);
			Properties wlProperties = new Properties();
			wlProperties.setProperty(IWorkloadAdapter.NUMBER_CURRENT_USERS, String.valueOf(1));
			wlProperties.setProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH, String.valueOf(1));
			wlProperties.setProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL, String.valueOf(1));
			wlProperties.setProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH, String.valueOf(1));
			wlProperties.setProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL, String.valueOf(1));
			wlProperties.setProperty(ConfigKeys.EXPERIMENT_DURATION, String.valueOf(SUT_WARMPUP_DURATION));

			workloadAdapter.startLoad(wlProperties);
			workloadAdapter.waitForFinishedLoad();
			sutWarmedUp = true;
		}
	}

	private void initialize() throws MeasurementException, InstrumentationException, WorkloadException {
		Spotter.getInstance().getProgress()
				.updateProgressMessage(getProvider().getName(), "Initializing measurement environment...");
		long startInitialization = System.currentTimeMillis();
		initInstrumentationController();

		initMeasurementController();

		initWorkloadAdapter();

		additionalDuration += (System.currentTimeMillis() - startInitialization) / KILO;
	}

	private void initWorkloadAdapter() throws WorkloadException {
		String measurementEnvironmentFile = GlobalConfiguration.getInstance().getProperty(
				ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE);
		if (measurementEnvironmentFile == null) {
			throw new WorkloadException("Measurement Environment File has not been specified!");
		}
		List<IWorkloadAdapter> wlAdapters = MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(
				measurementEnvironmentFile);
		workloadAdapter = WorkloadAdapterBroker.getInstance();
		((WorkloadAdapterBroker) workloadAdapter).setControllers(wlAdapters);
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
		measurementController = MeasurementBroker.getInstance();
		((MeasurementBroker) measurementController).setControllers(controllers);
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
		instrumentationController = InstrumentationBroker.getInstance();
		((InstrumentationBroker) instrumentationController).setControllers(instrumentations);
		instrumentationController.initialize();
	}

	protected void executeDefaultExperimentSeries(Class<? extends IDetectionController> detectionControllerClass,
			int numExperimentSteps, InstrumentationDescription instDescription) throws InstrumentationException,
			MeasurementException, WorkloadException {

		instrumentApplication(instDescription);

		int maxUsers = Integer.parseInt(LpeStringUtils.getPropertyOrFail(GlobalConfiguration.getInstance()
				.getProperties(), ConfigKeys.WORKLOAD_MAXUSERS, null));

		if (numExperimentSteps <= 1) {
			runExperiment(detectionControllerClass, maxUsers);
		} else {
			improveEstimatedDuration(numExperimentSteps);
			double dMinUsers = MIN_NUM_USERS;
			double dMaxUsers = maxUsers;
			double dStep = (dMaxUsers - dMinUsers) / (double) (numExperimentSteps - 1);

			for (double dUsers = dMinUsers; dUsers <= (dMaxUsers + EPSELON); dUsers += dStep) {
				int numUsers = new Double(dUsers).intValue();
				runExperiment(detectionControllerClass, numUsers);
			}
		}

		uninstrumentApplication();

	}

	protected void instrumentApplication(InstrumentationDescription instDescription) throws InstrumentationException {
		Spotter.getInstance().getProgress()
				.updateProgressStatus(getProvider().getName(), DiagnosisStatus.INSTRUMENTING);
		long instrumentationStart = System.currentTimeMillis();

		InstrumentationDescriptionBuilder descriptionBuilder = new InstrumentationDescriptionBuilder();
		descriptionBuilder.appendOtherDescription(instDescription);

		for (IExperimentReuser reuser : experimentReuser) {
			descriptionBuilder.appendOtherDescription(reuser.getInstrumentationDescription());
		}
		instrumentationController.instrument(descriptionBuilder.build());
		instrumented = true;
		additionalDuration += (System.currentTimeMillis() - instrumentationStart) / KILO;
	}

	protected void uninstrumentApplication() throws InstrumentationException {
		Spotter.getInstance().getProgress()
				.updateProgressStatus(getProvider().getName(), DiagnosisStatus.UNINSTRUMENTING);
		long uninstrumentationStart = System.currentTimeMillis();
		instrumentationController.uninstrument();
		instrumented = false;
		additionalDuration += (System.currentTimeMillis() - uninstrumentationStart) / KILO;
	}

	protected void runExperiment(Class<? extends IDetectionController> detectionControllerClass, int numUsers)
			throws WorkloadException, MeasurementException {

		LOGGER.info("{} started experiment with {} users ...", detectionControllerClass.getSimpleName(), numUsers);
		Properties wlProperties = new Properties();
		wlProperties.setProperty(IWorkloadAdapter.NUMBER_CURRENT_USERS, String.valueOf(numUsers));

		Spotter.getInstance().getProgress()
				.updateProgressStatus(getProvider().getName(), DiagnosisStatus.EXPERIMENTING_RAMP_UP);
		workloadAdapter.startLoad(wlProperties);

		workloadAdapter.waitForWarmupPhaseTermination();

		Spotter.getInstance().getProgress()
				.updateProgressStatus(getProvider().getName(), DiagnosisStatus.EXPERIMENTING_STABLE_PHASE);
		measurementController.enableMonitoring();

		workloadAdapter.waitForExperimentPhaseTermination();

		Spotter.getInstance().getProgress()
				.updateProgressStatus(getProvider().getName(), DiagnosisStatus.EXPERIMENTING_COOL_DOWN);
		measurementController.disableMonitoring();

		workloadAdapter.waitForFinishedLoad();

		Spotter.getInstance().getProgress()
				.updateProgressStatus(getProvider().getName(), DiagnosisStatus.COLLECTING_DATA);
		LOGGER.info("Storing data ...");
		long dataCollectionStart = System.currentTimeMillis();
		Parameter numOfUsersParameter = new Parameter(NUMBER_OF_USERS, numUsers);
		Set<Parameter> parameters = new TreeSet<>();
		parameters.add(numOfUsersParameter);
		storeResults(parameters);
		additionalDuration += (System.currentTimeMillis() - dataCollectionStart) / KILO;
		LOGGER.info("Data stored!");
	}

	protected void overwriteDataPath(String dataDirectory) {
		dataPath = dataDirectory;
	}

	protected void storeImageChartResource(Chart chart, String fileName, SpotterResult spotterResult) {
		String resourceName = fileName + ".png";
		String filePath = getAdditionalResourcesPath() + resourceName;
		try {
			BitmapEncoder.savePNGWithDPI(chart, filePath, DPI);
		} catch (IOException e) {
			// just ignore
			return;
		}
		spotterResult.addResourceFile(resourceName);
	}

	protected void storeTextResource(final String fileName, final SpotterResult spotterResult,
			final InputStream inStream) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String filePath = getAdditionalResourcesPath() + fileName + ".txt";
				BufferedWriter bWriter = null;
				BufferedReader bReader = null;
				try {

					FileWriter fWriter = new FileWriter(filePath);
					bWriter = new BufferedWriter(fWriter);
					bReader = new BufferedReader(new InputStreamReader(inStream));
					String line = bReader.readLine();
					while (line != null) {
						bWriter.write(line);
						bWriter.newLine();
						line = bReader.readLine();
					}

				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {

					try {
						if (bWriter != null) {
							bWriter.close();
						}
						if (bReader != null) {
							bReader.close();
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}

				}
				spotterResult.addResourceFile(filePath);
			}
		}).start();

	}

	protected void storeResults(Set<Parameter> parameters) throws MeasurementException {
		try {
			experimentCount++;
			final String path = getExperimentPath(experimentCount);
			final PipedOutputStream outStream = new PipedOutputStream();
			final PipedInputStream inStream = new PipedInputStream(outStream);

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						measurementController.pipeToOutputStream(outStream);
					} catch (MeasurementException e) {
						throw new RuntimeException("Failed Storing data!");
					}
				}
			}).start();

			RecordCSVWriter.getInstance().pipeDataToDatasetFiles(inStream, path, parameters);

			measurementController.storeReport(path);
		} catch (IOException e) {
			throw new RuntimeException("Failed Storing data!");
		}
	}

	private String getExperimentPath(int experimentCount) {
		StringBuilder pathBuilder = new StringBuilder(getDataPath());

		pathBuilder.append(String.valueOf(experimentCount));
		pathBuilder.append(System.getProperty("file.separator"));
		return pathBuilder.toString();
	}

	@Override
	public String getDataPath() {
		StringBuilder pathBuilder = new StringBuilder();
		if (dataPath == null) {
			pathBuilder.append(GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR));
			pathBuilder.append(getProvider().getName());
			pathBuilder.append(System.getProperty("file.separator"));

			pathBuilder.append(ResultsLocationConstants.CSV_SUB_DIR);
			pathBuilder.append(System.getProperty("file.separator"));

			dataPath = pathBuilder.toString();
		} else {
			pathBuilder.append(dataPath);
		}
		return pathBuilder.toString();
	}

	protected String getAdditionalResourcesPath() {
		StringBuilder pathBuilder = new StringBuilder();
		if (resourcePath == null) {
			pathBuilder.append(GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR));
			pathBuilder.append(getProvider().getName());
			pathBuilder.append(System.getProperty("file.separator"));

			pathBuilder.append(ResultsLocationConstants.RESULT_RESOURCES_SUB_DIR);
			pathBuilder.append(System.getProperty("file.separator"));

			resourcePath = pathBuilder.toString();
			File file = new File(resourcePath);
			if (!file.exists()) {
				LpeFileUtils.createDir(resourcePath);
			}
		} else {
			pathBuilder.append(resourcePath);
		}

		return pathBuilder.toString();
	}

	protected DatasetCollection loadData() {
		return RecordCSVReader.getInstance().readDatasetCollectionFromDirectory(dataPath);
	}

	protected abstract void executeExperiments() throws InstrumentationException, MeasurementException,
			WorkloadException;

	protected abstract int getNumOfExperiments();

	protected abstract SpotterResult analyze(DatasetCollection data);

	private void calculateInitialEstimatedDuration() {

		int numExperiments = getNumOfExperiments();
		long numUsers = GlobalConfiguration.getInstance().getPropertyAsLong(ConfigKeys.WORKLOAD_MAXUSERS, 1L);
		estimatedDuration = calculateExperimentDuration(numUsers) * numExperiments;
	}

	private long calculateExperimentDuration(long numUsers) {
		long rampUpUsersPerInterval = GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL, 0L);

		long coolDownUsersPerInterval = GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL, 0L);

		long rampUpInterval = GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH, 0L);

		long coolDownInterval = GlobalConfiguration.getInstance().getPropertyAsLong(
				ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH, 0L);

		long stablePhase = GlobalConfiguration.getInstance().getPropertyAsLong(ConfigKeys.EXPERIMENT_DURATION, 0L);

		long rampUp = 0;
		if (rampUpUsersPerInterval != 0) {
			rampUp = (numUsers / rampUpUsersPerInterval) * rampUpInterval;
		}

		long coolDown = 0;
		if (coolDownUsersPerInterval != 0) {
			coolDown = (numUsers / coolDownUsersPerInterval) * coolDownInterval;
		}

		return rampUp + stablePhase + coolDown;

	}

	private void improveEstimatedDuration(int numExperimentSteps) {
		double dMinUsers = MIN_NUM_USERS;
		long maxUsers = GlobalConfiguration.getInstance().getPropertyAsLong(ConfigKeys.WORKLOAD_MAXUSERS, 1L);
		double dMaxUsers = maxUsers;
		double dStep = (dMaxUsers - dMinUsers) / (double) (numExperimentSteps - 1);
		long duration = 0L;
		
		if (dStep <= 0.0 + 0.0001) {
			duration += calculateExperimentDuration(new Double(dMinUsers).intValue());
		} else {
			
			for (double dUsers = dMinUsers; dUsers <= dMaxUsers; dUsers += dStep) {
				int numUsers = new Double(dUsers).intValue();
				duration += calculateExperimentDuration(numUsers);
			}
			
		}

		estimatedDuration = duration;
	}

	/**
	 * @return the problem detection configuration
	 */
	public Properties getProblemDetectionConfiguration() {
		return problemDetectionConfiguration;
	}

	/**
	 * Sets the configuration for problem detection.
	 * 
	 * @param problemDetectionConfiguration
	 *            the new properties
	 */
	public void setProblemDetectionConfiguration(Properties problemDetectionConfiguration) {
		this.problemDetectionConfiguration = problemDetectionConfiguration;
	}

	@Override
	public void addExperimentReuser(IExperimentReuser reuser) {
		experimentReuser.add(reuser);
	}

	@Override
	public void setParentDataDir(String readDataFrom) {
		this.parentDataDir = readDataFrom;
	}

	/**
	 * @return the estimatedOverallDuration
	 */
	public long getEstimatedOverallDuration() {
		return estimatedDuration + additionalDuration;
	}

}
