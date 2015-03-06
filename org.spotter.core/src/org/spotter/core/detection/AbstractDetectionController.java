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
import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.extension.AbstractExtensionArtifact;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.LpeStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.core.ProgressManager;
import org.spotter.core.instrumentation.IInstrumentationAdapter;
import org.spotter.core.instrumentation.InstrumentationBroker;
import org.spotter.core.measurement.IMeasurementAdapter;
import org.spotter.core.measurement.MeasurementBroker;
import org.spotter.core.workload.IWorkloadAdapter;
import org.spotter.core.workload.LoadConfig;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDetectionController.class);
	public static final double EPSILON = 0.5;
	public static final long SECOND = 1000L;

	private static final int MIN_NUM_USERS = 1;
	public static final String NUMBER_OF_USERS_KEY = "numUsers";
	public static final String EXPERIMENT_STEPS_KEY = "numExperimentSteps";
	public static boolean sutWarmedUp = false;

	private static int getSUTWarmUpDuration() {
		return GlobalConfiguration.getInstance().getPropertyAsInteger(ConfigKeys.PREWARUMUP_DURATION,
				ConfigKeys.DEFAULT_SUT_WARMUP_DURATION);
	}

	private String problemId;

	private final IInstrumentationAdapter instrumentationController;
	private final IMeasurementAdapter measurementController;
	private final IWorkloadAdapter workloadAdapter;
	private final DetectionResultManager resultManager;

	private boolean instrumented = false;

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
			if (!GlobalConfiguration.getInstance().getPropertyAsBoolean(ConfigKeys.OMIT_WARMUP, false) && !sutWarmedUp) {

				ProgressManager.getInstance().addAdditionalDuration(getSUTWarmUpDuration());
			}

			ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.INITIALIZING);
			ProgressManager.getInstance().setProblemName(getProblemId(), getProvider().getName());
			boolean reuser = Boolean.parseBoolean(this.getProblemDetectionConfiguration().getProperty(
					AbstractDetectionExtension.REUSE_EXPERIMENTS_FROM_PARENT, "false"));
			boolean omitExperiments = GlobalConfiguration.getInstance().getPropertyAsBoolean(
					ConfigKeys.OMIT_EXPERIMENTS, false);

			if (omitExperiments & reuser) {
				resultManager.useOverwrittenParentDataDir(GlobalConfiguration.getInstance().getProperty(
						ConfigKeys.DUMMY_EXPERIMENT_DATA));
			} else if (omitExperiments & !reuser) {
				resultManager.overwriteDataPath(GlobalConfiguration.getInstance().getProperty(
						ConfigKeys.DUMMY_EXPERIMENT_DATA));
			} else if (!omitExperiments & reuser) {
				resultManager.useParentDataDir();
			} else if (!omitExperiments & !reuser) {
				if (!GlobalConfiguration.getInstance().getPropertyAsBoolean(ConfigKeys.OMIT_WARMUP, false)
						&& !sutWarmedUp) {
					warmUpSUT();
				}

				executeExperiments();
			}

			ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.ANALYZING);
			return analyze(getResultManager().loadData());
		} finally {
			if (instrumented) {
				instrumentationController.uninstrument();

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

			LoadConfig lConfig = new LoadConfig();
			lConfig.setNumUsers(1);
			lConfig.setRampUpIntervalLength(1);
			lConfig.setRampUpUsersPerInterval(1);
			lConfig.setCoolDownIntervalLength(1);
			lConfig.setCoolDownUsersPerInterval(1);
			lConfig.setExperimentDuration(getSUTWarmUpDuration());
			getWorkloadAdapter().startLoad(lConfig);
			getWorkloadAdapter().waitForFinishedLoad();
			sutWarmedUp = true;
		}
	}

	/**
	 * Executes a default experiment series comprising
	 * {@link numExperimentSteps} experiments. Starts with a load of one user
	 * and increases the load from one experiment to the next until the maximum
	 * number of users is reached.
	 * 
	 * @param detectionController
	 *            the detection controller executing the experiments
	 * @param numExperimentSteps
	 *            number of experiment steps to execute
	 * @param instDescription
	 *            instrumentation description to use for instrumentation
	 * @throws InstrumentationException
	 *             if instrumentation fails
	 * @throws MeasurementException
	 *             if measurement data cannot be collected
	 * @throws WorkloadException
	 *             if load cannot be generated properly
	 */
	protected void executeDefaultExperimentSeries(IDetectionController detectionController, int numExperimentSteps,
			InstrumentationDescription instDescription) throws InstrumentationException, MeasurementException,
			WorkloadException {

		instrumentApplication(instDescription);

		int maxUsers = Integer.parseInt(LpeStringUtils.getPropertyOrFail(GlobalConfiguration.getInstance()
				.getProperties(), ConfigKeys.WORKLOAD_MAXUSERS, null));

		if (numExperimentSteps <= 1) {
			runExperiment(detectionController, maxUsers);
		} else {
			double dMinUsers = MIN_NUM_USERS;
			double dMaxUsers = maxUsers;
			double dStep = (dMaxUsers - dMinUsers) / (double) (numExperimentSteps - 1);

			// if we have the same number of maximum and minimum users, then we
			// have only one experiment run
			if (dStep <= 0.0 + EPSILON) {
				runExperiment(detectionController, MIN_NUM_USERS);
			} else {

				for (double dUsers = dMinUsers; dUsers <= (dMaxUsers + EPSILON); dUsers += dStep) {
					int numUsers = new Double(dUsers).intValue();
					runExperiment(detectionController, numUsers);
				}

			}
		}

		uninstrumentApplication();

	}

	/**
	 * Instruments the target application according to the passed
	 * {@link instDescription}.
	 * 
	 * @param instDescription
	 *            instrumentation description describing the desired
	 *            instrumentation state of the target application
	 * @throws InstrumentationException
	 *             if instrumentation fails
	 * @throws MeasurementException
	 *             if instrumentation fails
	 */
	protected void instrumentApplication(InstrumentationDescription instDescription) throws InstrumentationException,
			MeasurementException {
		ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.INSTRUMENTING);
		long instrumentationStart = System.currentTimeMillis();

		InstrumentationDescriptionBuilder descriptionBuilder = new InstrumentationDescriptionBuilder();
		String excludes = GlobalConfiguration.getInstance().getProperty(ConfigKeys.INSTRUMENTATION_EXCLUDES, "");
		for (String exc : excludes.split(ConfigParameterDescription.LIST_VALUE_SEPARATOR)) {
			descriptionBuilder.newGlobalRestriction().excludePackage(exc);
		}

		descriptionBuilder.appendOtherDescription(instDescription);

		for (IExperimentReuser reuser : experimentReuser) {
			descriptionBuilder.appendOtherDescription(reuser.getInstrumentationDescription());
		}
		InstrumentationDescription aggregatedDescription = descriptionBuilder.build();
		getInstrumentationController().instrument(aggregatedDescription);
		measurementController.prepareMonitoring(aggregatedDescription);
		instrumented = true;
		ProgressManager.getInstance().addAdditionalDuration(
				(System.currentTimeMillis() - instrumentationStart) / SECOND);

	}

	/**
	 * Reverts instrumentation.
	 * 
	 * @throws InstrumentationException
	 *             if reversion fails
	 * @throws MeasurementException
	 *             if reversion fails
	 */
	protected void uninstrumentApplication() throws InstrumentationException, MeasurementException {
		ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.UNINSTRUMENTING);
		long uninstrumentationStart = System.currentTimeMillis();
		getInstrumentationController().uninstrument();
		measurementController.resetMonitoring();
		instrumented = false;
		ProgressManager.getInstance().addAdditionalDuration(
				(System.currentTimeMillis() - uninstrumentationStart) / SECOND);
	}

	/**
	 * Runs a single experiment.
	 * 
	 * @param detectionController
	 *            the detection controller running the analysis
	 * @param numUsers
	 *            number of user to use for the load of this experiment
	 * @throws WorkloadException
	 *             if load generation fails
	 * @throws MeasurementException
	 *             if data collection fails
	 */
	protected void runExperiment(IDetectionController detectionController, int numUsers) throws WorkloadException,
			MeasurementException {

		LOGGER.info("{} detection controller started experiment with {} users ...", detectionController.getProvider()
				.getName(), numUsers);
		ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.EXPERIMENTING_RAMP_UP);
		LoadConfig lConfig = new LoadConfig();
		lConfig.setNumUsers(numUsers);
		lConfig.setRampUpIntervalLength(GlobalConfiguration.getInstance().getPropertyAsInteger(
				ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH));
		lConfig.setRampUpUsersPerInterval(GlobalConfiguration.getInstance().getPropertyAsInteger(
				ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL));
		lConfig.setCoolDownIntervalLength(GlobalConfiguration.getInstance().getPropertyAsInteger(
				ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH));
		lConfig.setCoolDownUsersPerInterval(GlobalConfiguration.getInstance().getPropertyAsInteger(
				ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL));
		lConfig.setExperimentDuration(GlobalConfiguration.getInstance().getPropertyAsInteger(
				ConfigKeys.EXPERIMENT_DURATION));
		getWorkloadAdapter().startLoad(lConfig);

		getWorkloadAdapter().waitForWarmupPhaseTermination();

		ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.EXPERIMENTING_STABLE_PHASE);
		getMeasurementController().enableMonitoring();

		getWorkloadAdapter().waitForExperimentPhaseTermination();

		ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.EXPERIMENTING_COOL_DOWN);
		getMeasurementController().disableMonitoring();

		getWorkloadAdapter().waitForFinishedLoad();

		ProgressManager.getInstance().updateProgressStatus(getProblemId(), DiagnosisStatus.COLLECTING_DATA);
		LOGGER.info("Storing data ...");
		long dataCollectionStart = System.currentTimeMillis();
		Parameter numOfUsersParameter = new Parameter(NUMBER_OF_USERS_KEY, numUsers);
		Set<Parameter> parameters = new TreeSet<>();
		parameters.add(numOfUsersParameter);
		getResultManager().storeResults(parameters, getMeasurementController());
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

	/**
	 * Analyzes the given measurement data.
	 * 
	 * @param data
	 *            experiment data to analyze
	 * @return detection result for the given performance problem under
	 *         investigation
	 */
	protected abstract SpotterResult analyze(DatasetCollection data);

	/**
	 * @return the measurementController
	 */
	protected IMeasurementAdapter getMeasurementController() {
		return measurementController;
	}

	/**
	 * @return the workloadAdapter
	 */
	protected IWorkloadAdapter getWorkloadAdapter() {
		return workloadAdapter;
	}

	/**
	 * @return the instrumentationController
	 */
	protected IInstrumentationAdapter getInstrumentationController() {
		return instrumentationController;
	}

}
