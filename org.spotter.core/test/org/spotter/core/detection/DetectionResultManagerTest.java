package org.spotter.core.detection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import junit.framework.Assert;

import org.aim.api.exceptions.MeasurementException;
import org.aim.api.measurement.MeasurementData;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.api.measurement.dataset.Parameter;
import org.aim.artifacts.records.CPUUtilizationRecord;
import org.aim.artifacts.records.ResponseTimeRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.LpeFileUtils;
import org.lpe.common.util.system.LpeSystemUtils;
import org.spotter.core.test.dummies.satellites.DummyMeasurement;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.result.ResultsLocationConstants;
import org.spotter.shared.result.model.SpotterResult;

import com.xeiam.xchart.Chart;
import com.xeiam.xchart.ChartBuilder;

public class DetectionResultManagerTest {
	private static final String CONTROLLER_NAME = "testController";
	private String baseDir = "";
	private static final String PARENT_DIR = System.getProperty("file.separator") + "parent";
	private static final String DATA_DIR = System.getProperty("file.separator") + CONTROLLER_NAME + "-" + CONTROLLER_NAME.hashCode()
			+ System.getProperty("file.separator") + ResultsLocationConstants.CSV_SUB_DIR
			+ System.getProperty("file.separator");
	private static final String RESOURCES_DIR = System.getProperty("file.separator") + CONTROLLER_NAME + "-" + CONTROLLER_NAME.hashCode()
			+ System.getProperty("file.separator") + ResultsLocationConstants.RESULT_RESOURCES_SUB_DIR
			+ System.getProperty("file.separator");
	private File tempDir;

	@BeforeClass
	public static void initGlobalConfig() {
		GlobalConfiguration.initialize(new Properties());
	}

	@Before
	public void createTempDir() throws IOException {
		tempDir = new File("tempJUnit");
		if (tempDir.exists()) {
			LpeFileUtils.removeDir(tempDir.getAbsolutePath());
		}
		LpeFileUtils.createDir(tempDir.getAbsolutePath());
		baseDir = tempDir.getAbsolutePath();
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR,
				tempDir + System.getProperty("file.separator"));
	}

	@After
	public void removeTempDir() throws IOException {
		LpeFileUtils.removeDir(tempDir.getAbsolutePath());
	}

	@Test
	public void testDataPaths() {
		GlobalConfiguration.getInstance().putProperty(ConfigKeys.RESULT_DIR,
				baseDir + System.getProperty("file.separator"));
		DetectionResultManager drManager = new DetectionResultManager(CONTROLLER_NAME);
		drManager.setProblemId(CONTROLLER_NAME);
		drManager.setParentDataDir(baseDir + PARENT_DIR);
		Assert.assertEquals(baseDir + DATA_DIR, drManager.getDataPath());
		Assert.assertEquals(baseDir + DATA_DIR, drManager.getDataPath());
		Assert.assertEquals(baseDir + RESOURCES_DIR, drManager.getAdditionalResourcesPath());
		Assert.assertEquals(baseDir + RESOURCES_DIR, drManager.getAdditionalResourcesPath());

		drManager.overwriteDataPath("anotherPath");
		Assert.assertEquals("anotherPath", drManager.getDataPath());
		Assert.assertEquals(baseDir + RESOURCES_DIR, drManager.getAdditionalResourcesPath());

		drManager.useParentDataDir();
		Assert.assertEquals(baseDir + PARENT_DIR, drManager.getDataPath());
		Assert.assertEquals(baseDir + RESOURCES_DIR, drManager.getAdditionalResourcesPath());
	}

	@Test
	public void testDataStorage() throws IOException, MeasurementException {
		DummyMeasurement dMeasurement = new DummyMeasurement(null);

		MeasurementData mData = dMeasurement.getMeasurementData();
		Assert.assertEquals(dMeasurement.NUM_RECORDS, mData.getRecords().size());

		DetectionResultManager drManager = new DetectionResultManager(CONTROLLER_NAME);
		drManager.setProblemId(CONTROLLER_NAME);
		Set<Parameter> parameters = new HashSet<>();
		parameters.add(new Parameter("NumUsers", 1));
		drManager.storeResults(parameters, dMeasurement);
		DatasetCollection loadedData = drManager.loadData();

		Assert.assertEquals(2, loadedData.getDifferentRecordTypes().size());
		Assert.assertTrue(loadedData.getDifferentRecordTypes().contains(ResponseTimeRecord.class));
		Assert.assertTrue(loadedData.getDifferentRecordTypes().contains(CPUUtilizationRecord.class));
		Assert.assertEquals(mData.getRecords().size(), loadedData.getRecords().size());
		Assert.assertEquals(1, loadedData.getDataSet(ResponseTimeRecord.class).getValueSet("NumUsers").size());

	}

	@Test
	public void testChartStorage() throws IOException {
		ChartBuilder chartBuilder = new ChartBuilder();
		chartBuilder.width(100);
		chartBuilder.height(100);
		chartBuilder.title("title");
		chartBuilder.xAxisTitle("Experiment time ");
		chartBuilder.yAxisTitle("y-axis");
		Chart chart = chartBuilder.build();
		double[] x = { 1.0, 2.0, 3.0 };
		double[] y = { 1.0, 2.0, 3.0 };
		chart.addSeries("Test Series", x, y);
		DetectionResultManager drManager = new DetectionResultManager(CONTROLLER_NAME);
		drManager.setProblemId(CONTROLLER_NAME);
		String fileName = "chart";
		SpotterResult result = new SpotterResult();
		result.setDetected(true);
		drManager.storeImageChartResource(chart, fileName, result);

		Assert.assertEquals(1, result.getResourceFiles().size());
		Assert.assertEquals(fileName + ".png", result.getResourceFiles().get(0));

		File pngFile = new File(baseDir + RESOURCES_DIR + fileName + ".png");
		Assert.assertTrue(pngFile.isFile());
		Assert.assertTrue(pngFile.exists());
	}

	@Test
	public void testTextStorage() throws IOException {

		DetectionResultManager drManager = new DetectionResultManager(CONTROLLER_NAME);
		drManager.setProblemId(CONTROLLER_NAME);
		String fileName = "textFile";
		SpotterResult result = new SpotterResult();
		result.setDetected(true);
		final PipedOutputStream outStream = new PipedOutputStream();
		PipedInputStream inStream = new PipedInputStream(outStream);

		LpeSystemUtils.submitTask(new Runnable() {

			@Override
			public void run() {
				try {
					BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(outStream));

					bWriter.write("NumUsers;LockWaits");

					bWriter.newLine();
					for (int i = 0; i < 10; i++) {
						bWriter.write("Hallo");
						bWriter.newLine();
					}
					bWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		drManager.storeTextResource(fileName, result, inStream);

		Assert.assertEquals(1, result.getResourceFiles().size());
		Assert.assertEquals(fileName + ".txt", result.getResourceFiles().get(0));

		File txtFile = new File(baseDir + RESOURCES_DIR + fileName + ".txt");
		Assert.assertTrue(txtFile.isFile());
		Assert.assertTrue(txtFile.exists());
	}

}
