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

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.measurement.dataset.Parameter;
import org.aim.api.measurement.utils.RecordCSVWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.LpeFileUtils;
import org.lpe.common.util.system.LpeSystemUtils;
import org.spotter.core.config.interpretation.HierarchyFactory;
import org.spotter.core.config.interpretation.HierarchyTest;
import org.spotter.core.config.interpretation.MeasurementEnvironmentFactory;
import org.spotter.core.config.interpretation.PerformanceProblem;
import org.spotter.core.instrumentation.IInstrumentationAdapter;
import org.spotter.core.instrumentation.InstrumentationBroker;
import org.spotter.core.measurement.IMeasurementAdapter;
import org.spotter.core.measurement.MeasurementBroker;
import org.spotter.core.test.dummies.detection.MockDetection;
import org.spotter.core.test.dummies.satellites.DummyMeasurement;
import org.spotter.core.test.dummies.satellites.DummyWorkload;
import org.spotter.core.workload.IWorkloadAdapter;
import org.spotter.core.workload.WorkloadAdapterBroker;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.result.model.ResultsContainer;
import org.spotter.shared.result.model.SpotterResult;

public class AbstractDetectionControllerTest {
	private AbstractDetectionController detectionController;
	private File tempDir;

	@Before
	public void initialize() throws URISyntaxException, IOException, InstrumentationException, MeasurementException,
			WorkloadException {
		GlobalConfiguration.initialize(new Properties());
		String baseDir = creeateTempDir();
		initGlobalConfigs(baseDir);

		initializeMeasurementEnvironment();

		readHierarchy();

	}

	/**
	 * Initializes the measurement environment. Reads the environment
	 * description from XML file and creates corresponding Java objects
	 * 
	 * @throws URISyntaxException
	 */
	private void initializeMeasurementEnvironment() throws InstrumentationException, MeasurementException,
			WorkloadException, URISyntaxException {
		URL url = AbstractDetectionControllerTest.class.getResource("/test-env.xml");
		String meFile = url.toURI().getPath();
		initInstrumentationController(meFile);

		initMeasurementController(meFile);

		initWorkloadAdapter(meFile);
	}

	private void initWorkloadAdapter(String measurementEnvironmentFile) throws WorkloadException {

		if (measurementEnvironmentFile == null) {
			throw new WorkloadException("Measurement Environment File has not been specified!");
		}
		List<IWorkloadAdapter> wlAdapters = MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(
				measurementEnvironmentFile);
		WorkloadAdapterBroker workloadAdapter = WorkloadAdapterBroker.getInstance();
		workloadAdapter.setControllers(wlAdapters);
		workloadAdapter.initialize();
	}

	private void initMeasurementController(String measurementEnvironmentFile) throws InstrumentationException,
			MeasurementException {

		if (measurementEnvironmentFile == null) {
			throw new InstrumentationException("Measurement Environment File has not been specified!");
		}
		List<IMeasurementAdapter> controllers = MeasurementEnvironmentFactory.getInstance()
				.createMeasurementControllers(measurementEnvironmentFile);
		MeasurementBroker measurementController = MeasurementBroker.getInstance();
		measurementController.setControllers(controllers);
		measurementController.initialize();

	}

	private void initInstrumentationController(String measurementEnvironmentFile) throws InstrumentationException {

		if (measurementEnvironmentFile == null) {
			throw new InstrumentationException("Measurement Environment File has not been specified!");
		}
		List<IInstrumentationAdapter> instrumentations = MeasurementEnvironmentFactory.getInstance()
				.createInstrumentationControllers(measurementEnvironmentFile);
		InstrumentationBroker instrumentationController = InstrumentationBroker.getInstance();
		instrumentationController.setControllers(instrumentations);
		instrumentationController.initialize();
	}

	private void readHierarchy() throws URISyntaxException {
		URL url = HierarchyTest.class.getResource("/simple-hierarchy.xml");
		String hierarchyFile = url.toURI().getPath();
		ResultsContainer rContainer = new ResultsContainer();

		PerformanceProblem root = HierarchyFactory.getInstance().createPerformanceProblemHierarchy(hierarchyFile,
				rContainer);

		detectionController = (AbstractDetectionController) root.getChildren().get(0).getDetectionController();
	}

	private void initGlobalConfigs(String baseDir) {
		String dir = System.getProperty("user.dir");
		Properties properties = new Properties();
		properties.setProperty("org.lpe.common.extension.appRootDir", dir);
		properties.setProperty("org.spotter.conf.pluginDirNames", "plugins");
		properties.setProperty(ConfigKeys.RESULT_DIR, baseDir + System.getProperty("file.separator"));
		properties.setProperty(ConfigKeys.EXPERIMENT_DURATION, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL, "1");
		properties.setProperty(ConfigKeys.WORKLOAD_MAXUSERS, "10");
		GlobalConfiguration.reinitialize(properties);
	}

	private String creeateTempDir() throws IOException {
		tempDir = new File("tempJUnit");
		if (tempDir.exists()) {
			LpeFileUtils.removeDir(tempDir.getAbsolutePath());
		}
		LpeFileUtils.createDir(tempDir.getAbsolutePath());
		return tempDir.getAbsolutePath();
	}

	private void preGenerateData(String dataDir, final DummyMeasurement dMeasurement, String controllerName)
			throws MeasurementException {
		for (int i = 1; i <= MockDetection.NUM_EXPERIMENTS; i++) {
			try {
				StringBuilder pathBuilder = new StringBuilder(dataDir);
				pathBuilder.append(controllerName);
				pathBuilder.append(System.getProperty("file.separator"));
				pathBuilder.append("csv");
				pathBuilder.append(System.getProperty("file.separator"));
				pathBuilder.append(String.valueOf(i));
				pathBuilder.append(System.getProperty("file.separator"));
				final String path = pathBuilder.toString();

				final PipedOutputStream outStream = new PipedOutputStream();
				final PipedInputStream inStream = new PipedInputStream(outStream);

				Future<?> future = LpeSystemUtils.submitTask(new Runnable() {
					@Override
					public void run() {
						try {
							dMeasurement.pipeToOutputStream(outStream);
						} catch (MeasurementException e) {
							throw new RuntimeException("Failed Storing data!");
						}
					}
				});

				RecordCSVWriter.getInstance().pipeDataToDatasetFiles(inStream, path, new HashSet<Parameter>());

				future.get();

			} catch (IOException | InterruptedException | ExecutionException e) {
				throw new MeasurementException("Failed Storing data!", e);
			}
		}
	}

	@After
	public void cleanUp() throws IOException {
		LpeFileUtils.removeDir(tempDir.getAbsolutePath());
	}

	@Test
	public void testSimpleAnalysis() throws InstrumentationException, MeasurementException, WorkloadException {
		Assert.assertEquals("MockDetection", detectionController.getProvider().getName());
		Assert.assertEquals("test.value", detectionController.getProblemDetectionConfiguration().get("test.key"));
		SpotterResult result = detectionController.analyzeProblem();
		Assert.assertTrue(result.isDetected());

		Assert.assertEquals(MockDetection.NUM_EXPERIMENTS + 1, DummyWorkload.numExperiments);
	}

	@Test
	public void testWithoutWarmUp() throws InstrumentationException, MeasurementException, WorkloadException {
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.OMIT_WARMUP, "true");
		Assert.assertEquals("MockDetection", detectionController.getProvider().getName());
		Assert.assertEquals("test.value", detectionController.getProblemDetectionConfiguration().get("test.key"));
		SpotterResult result = detectionController.analyzeProblem();
		Assert.assertTrue(result.isDetected());

		Assert.assertEquals(MockDetection.NUM_EXPERIMENTS, DummyWorkload.numExperiments);

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.OMIT_WARMUP, "false");
	}

	@Test
	public void testWithoutExperiments() throws InstrumentationException, MeasurementException, WorkloadException,
			IOException {
		String dataDir = tempDir.getAbsolutePath() + System.getProperty("file.separator") + "data"
				+ System.getProperty("file.separator");

		final DummyMeasurement dMeasurement = new DummyMeasurement(null);

		

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.OMIT_EXPERIMENTS, "true");
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.DUMMY_EXPERIMENT_DATA, dataDir);
		Assert.assertEquals("MockDetection", detectionController.getProvider().getName());
		Assert.assertEquals("test.value", detectionController.getProblemDetectionConfiguration().get("test.key"));
		preGenerateData(dataDir, dMeasurement, "MockDetection-" + detectionController.getProblemId().hashCode());
		SpotterResult result = detectionController.analyzeProblem();
		Assert.assertTrue(result.isDetected());

		Assert.assertEquals(0, DummyWorkload.numExperiments);

		GlobalConfiguration.getInstance().putProperty(ConfigKeys.OMIT_EXPERIMENTS, "false");
	}

}
