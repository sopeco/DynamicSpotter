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
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.aim.api.exceptions.MeasurementException;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.api.measurement.dataset.Parameter;
import org.aim.api.measurement.utils.RecordCSVReader;
import org.aim.api.measurement.utils.RecordCSVWriter;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.LpeFileUtils;
import org.lpe.common.util.system.LpeSystemUtils;
import org.spotter.core.measurement.IMeasurementController;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.result.ResultsLocationConstants;
import org.spotter.shared.result.model.SpotterResult;

import com.xeiam.xchart.BitmapEncoder;
import com.xeiam.xchart.Chart;

/**
 * Manages the storage of results for a detection controller.
 * 
 * @author Alexander Wert
 * 
 */
public class DetectionResultManager {
	private static final int DPI = 300;
	private String dataPath;
	private String resourcePath;
	private String parentDataDir;
	private String controllerName;
	private int resultCount = 0;

	/**
	 * Constructor.
	 * 
	 * @param controllerName
	 *            name of the controller this result manager is responsible for.
	 */
	public DetectionResultManager(String controllerName) {
		this.controllerName = controllerName;
	}

	/**
	 * Sets the data directory of the parent controller.
	 * 
	 * @param readDataFrom
	 *            directory where to read data from
	 */
	public void setParentDataDir(String readDataFrom) {
		this.parentDataDir = readDataFrom;
	}

	/**
	 * Returns the directory where raw experiment data is store in.
	 * 
	 * @return directory where raw experiment data is store in.
	 */
	public String getDataPath() {
		StringBuilder pathBuilder = new StringBuilder();
		if (dataPath == null) {
			pathBuilder.append(GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR));
			pathBuilder.append(controllerName);
			pathBuilder.append(System.getProperty("file.separator"));

			pathBuilder.append(ResultsLocationConstants.CSV_SUB_DIR);
			pathBuilder.append(System.getProperty("file.separator"));

			dataPath = pathBuilder.toString();
		} else {
			pathBuilder.append(dataPath);
		}
		return pathBuilder.toString();
	}

	/**
	 * Returns the path for additional resources.
	 * 
	 * @return directory where additional resources shall be stored
	 */
	public String getAdditionalResourcesPath() {
		StringBuilder pathBuilder = new StringBuilder();
		if (resourcePath == null) {
			pathBuilder.append(GlobalConfiguration.getInstance().getProperty(ConfigKeys.RESULT_DIR));
			pathBuilder.append(controllerName);
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

	private String getExperimentPath(int experimentCount) {
		StringBuilder pathBuilder = new StringBuilder(getDataPath());

		pathBuilder.append(String.valueOf(experimentCount));
		pathBuilder.append(System.getProperty("file.separator"));
		return pathBuilder.toString();
	}

	/**
	 * Overwrites the experiment data path to the given directory.
	 * 
	 * @param dataDirectory
	 *            data directory to use
	 */
	public void overwriteDataPath(String dataDirectory) {
		dataPath = dataDirectory;
	}

	/**
	 * Use parent data directory.
	 */
	public void useParentDataDir() {
		dataPath = parentDataDir;
	}

	/**
	 * Stores a xChart image.
	 * 
	 * @param chart
	 *            chart to store
	 * @param fileName
	 *            file name of the image
	 * @param spotterResult
	 *            corresponding result object
	 */
	public void storeImageChartResource(Chart chart, String fileName, SpotterResult spotterResult) {
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

	/**
	 * Stores a text resource.
	 * 
	 * @param fileName
	 *            name of the text file
	 * @param spotterResult
	 *            corresponding spotter result
	 * @param inStream
	 *            input stream representing the text resource
	 */
	public void storeTextResource(final String fileName, final SpotterResult spotterResult, final InputStream inStream) {

		Future<?> future = LpeSystemUtils.submitTask(new Runnable() {

			@Override
			public void run() {
				String resourceName = fileName + ".txt";
				String filePath = getAdditionalResourcesPath() + resourceName;
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
				spotterResult.addResourceFile(resourceName);
			}
		});

		try {
			future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("Storing text resource failed!", e);
		}

	}

	/**
	 * Stores experiment raw data.
	 * 
	 * @param parameters
	 *            additional independent experiment parameters
	 * @param measurementController
	 *            measurement controller where to retrieve data from
	 * @throws MeasurementException
	 *             thrown if storing raw data fails
	 */
	public void storeResults(final Set<Parameter> parameters, final IMeasurementController measurementController)
			throws MeasurementException {
		try {
			resultCount++;
			final String path = getExperimentPath(resultCount);
			final PipedOutputStream outStream = new PipedOutputStream();
			final PipedInputStream inStream = new PipedInputStream(outStream);

			Future<?> future = LpeSystemUtils.submitTask(new Runnable() {
				@Override
				public void run() {
					try {
						measurementController.pipeToOutputStream(outStream);
					} catch (MeasurementException e) {
						throw new RuntimeException("Failed Storing data!");
					}
				}
			});

			RecordCSVWriter.getInstance().pipeDataToDatasetFiles(inStream, path, parameters);

			future.get();

			measurementController.storeReport(path);
		} catch (IOException | InterruptedException | ExecutionException e) {
			throw new MeasurementException("Failed Storing data!", e);
		}
	}

	/**
	 * Loads experiment raw data for that controller.
	 * 
	 * @return a collection of data sets
	 */
	public DatasetCollection loadData() {
		return RecordCSVReader.getInstance().readDatasetCollectionFromDirectory(dataPath);
	}
}
