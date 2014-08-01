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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.api.measurement.dataset.Parameter;
import org.aim.description.InstrumentationDescription;
import org.aim.description.builder.InstrumentationDescriptionBuilder;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.extension.AbstractExtensionArtifact;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.LpeStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.core.ProgressManager;
import org.spotter.core.instrumentation.ISpotterInstrumentation;
import org.spotter.core.instrumentation.InstrumentationBroker;
import org.spotter.core.measurement.IMeasurementController;
import org.spotter.core.measurement.MeasurementBroker;
import org.spotter.core.workload.IWorkloadAdapter;
import org.spotter.core.workload.WorkloadAdapterBroker;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.result.model.SpotterResult;
import org.spotter.shared.status.DiagnosisStatus;

/**
 * The {@link AbstractDetectionController} comprises common functionality of all
 * detection controller classes, like initialization, result persistence, etc.
 * 
 * @author Alexander Wert
 * 
 */
public abstract class AbstractDetectionController extends AbstractExtensionArtifact implements IDetectionController {
	private static final double EPSILON = 0.5;

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDetectionController.class);

	/**
	 * property key for detection name.
	 */
	public static final long SECOND = 1000L;
	public static final String DETECTABLE_KEY = "org.spotter.detection.detectable";
	private static final int SUT_WARMPUP_DURATION = GlobalConfiguration.getInstance().getPropertyAsInteger(
			ConfigKeys.PREWARUMUP_DURATION, 180);
	private static final int MIN_NUM_USERS = 1;
	protected static final String NUMBER_OF_USERS_KEY = "numUsers";
	protected static final String EXPERIMENT_STEPS_KEY = "numExperimentSteps";

	private ISpotterInstrumentation instrumentationController;
	private String problemId;

	protected IMeasurementController measurementController;
	protected IWorkloadAdapter workloadAdapter;
	private final DetectionResultManager resultManager;

	protected boolean instrumented = false;
	private boolean sutWarmedUp = false;
	private Properties problemDetectionConfiguration = new Properties();

	private List<IExperimentReuser> experimentReuser;

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *            Provider of the extension.
	 */
	public AbstractDetectionController(IExtension<IDetectionController> provider) {
		super(provider);
		resultManager = new DetectionResultManager(provider.getName());
		experimentReuser = new ArrayList<>();
		instrumentationController = InstrumentationBroker.getInstance();
		measurementController = MeasurementBroker.getInstance();
		workloadAdapter = WorkloadAdapterBroker.getInstance();
	}

	@Override
	public SpotterResult analyzeProblem() throws InstrumentationException, MeasurementException, WorkloadException {
		try {
			if (!GlobalConfiguration.getInstance().getPropertyAsBoolean(ConfigKeys.OMIT_WARMUP, false)) {

				ProgressManager.getInstance().addAdditionalDuration(SUT_WARMPUP_DURATION);
			}
			ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.INITIALIZING);

			if (GlobalConfiguration.getInstance().getPropertyAsBoolean(ConfigKeys.OMIT_EXPERIMENTS, false)) {
				resultManager.overwriteDataPath(GlobalConfiguration.getInstance().getProperty(
						ConfigKeys.DUMMY_EXPERIMENT_DATA));
			} else if (this instanceof IExperimentReuser) {
				resultManager.useParentDataDir();
			} else {
				if (!GlobalConfiguration.getInstance().getPropertyAsBoolean(ConfigKeys.OMIT_WARMUP, false)) {
					warmUpSUT();
				}

				executeExperiments();
			}

			ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.ANALYSING);
			return analyze(getResultManager().loadData());
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
	 * This method triggers the load generators to put low load on the system
	 * under test in order to warm it up. E.g. all required classes of the SUT
	 * should be loaded.
	 * 
	 * @throws WorkloadException
	 */
	private void warmUpSUT() throws WorkloadException {
		if (!sutWarmedUp) {
			ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.WARM_UP);
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

	protected void executeDefaultExperimentSeries(Class<? extends IDetectionController> detectionControllerClass,
			int numExperimentSteps, InstrumentationDescription instDescription) throws InstrumentationException,
			MeasurementException, WorkloadException {

		instrumentApplication(instDescription);

		int maxUsers = Integer.parseInt(LpeStringUtils.getPropertyOrFail(GlobalConfiguration.getInstance()
				.getProperties(), ConfigKeys.WORKLOAD_MAXUSERS, null));

		if (numExperimentSteps <= 1) {
			runExperiment(detectionControllerClass, maxUsers);
		} else {
			double dMinUsers = MIN_NUM_USERS;
			double dMaxUsers = maxUsers;
			double dStep = (dMaxUsers - dMinUsers) / (double) (numExperimentSteps - 1);

			// if we have the same number of maximum and minimum users, then we
			// have only one experiment run
			if (dStep <= 0.0 + EPSILON) {
				runExperiment(detectionControllerClass, MIN_NUM_USERS);
			} else {

				for (double dUsers = dMinUsers; dUsers <= (dMaxUsers + EPSILON); dUsers += dStep) {
					int numUsers = new Double(dUsers).intValue();
					runExperiment(detectionControllerClass, numUsers);
				}

			}
		}

		uninstrumentApplication();

	}

	protected void instrumentApplication(InstrumentationDescription instDescription) throws InstrumentationException {
		ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.INSTRUMENTING);
		long instrumentationStart = System.currentTimeMillis();

		InstrumentationDescriptionBuilder descriptionBuilder = new InstrumentationDescriptionBuilder();
		descriptionBuilder.appendOtherDescription(instDescription);

		for (IExperimentReuser reuser : experimentReuser) {
			descriptionBuilder.appendOtherDescription(reuser.getInstrumentationDescription());
		}
		instrumentationController.instrument(descriptionBuilder.build());
		instrumented = true;
		ProgressManager.getInstance().addAdditionalDuration(
				(System.currentTimeMillis() - instrumentationStart) / SECOND);

	}

	protected void uninstrumentApplication() throws InstrumentationException {
		ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.UNINSTRUMENTING);
		long uninstrumentationStart = System.currentTimeMillis();
		instrumentationController.uninstrument();
		instrumented = false;
		ProgressManager.getInstance().addAdditionalDuration(
				(System.currentTimeMillis() - uninstrumentationStart) / SECOND);
	}

	protected void runExperiment(Class<? extends IDetectionController> detectionControllerClass, int numUsers)
			throws WorkloadException, MeasurementException {

		LOGGER.info("{} started experiment with {} users ...", detectionControllerClass.getSimpleName(), numUsers);
		Properties wlProperties = new Properties();
		wlProperties.setProperty(IWorkloadAdapter.NUMBER_CURRENT_USERS, String.valueOf(numUsers));

		ProgressManager.getInstance().updateProgressStatus(getProblemId(),
				DiagnosisStatus.EXPERIMENTING_RAMP_UP);
		workloadAdapter.startLoad(wlProperties);

		workloadAdapter.waitForWarmupPhaseTermination();

		ProgressManager.getInstance().updateProgressStatus(getProblemId(),
				DiagnosisStatus.EXPERIMENTING_STABLE_PHASE);
		measurementController.enableMonitoring();

		workloadAdapter.waitForExperimentPhaseTermination();

		ProgressManager.getInstance().updateProgressStatus(getProblemId(),
				DiagnosisStatus.EXPERIMENTING_COOL_DOWN);
		measurementController.disableMonitoring();

		workloadAdapter.waitForFinishedLoad();

		ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.COLLECTING_DATA);
		LOGGER.info("Storing data ...");
		long dataCollectionStart = System.currentTimeMillis();
		Parameter numOfUsersParameter = new Parameter(NUMBER_OF_USERS_KEY, numUsers);
		Set<Parameter> parameters = new TreeSet<>();
		parameters.add(numOfUsersParameter);
		getResultManager().storeResults(parameters, measurementController);
		ProgressManager.getInstance()
				.addAdditionalDuration((System.currentTimeMillis() - dataCollectionStart) / SECOND);
		LOGGER.info("Data stored!");
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
	public DetectionResultManager getResultManager() {
		return resultManager;
	}

	@Override
	public String getProblemId() {
		return problemId;
	}

	@Override
	public void setProblemId(String problemId) {
		this.problemId = problemId;
		resultManager.setProblemId(problemId);
	}

	protected abstract void executeExperiments() throws InstrumentationException, MeasurementException,
			WorkloadException;

	/**
	 * Analyzes the given measurement data.
	 * 
	 * @param data
	 *            experiment data to analyze
	 * @return detection result for the given performance problem under
	 *         investigation
	 */
	protected abstract SpotterResult analyze(DatasetCollection data);

}
