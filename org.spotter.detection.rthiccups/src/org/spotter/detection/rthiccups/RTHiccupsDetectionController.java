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
package org.spotter.detection.rthiccups;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.instrumentation.description.InstrumentationDescription;
import org.aim.api.instrumentation.description.InstrumentationDescriptionBuilder;
import org.aim.api.measurement.dataset.Dataset;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.api.measurement.dataset.ParameterSelection;
import org.aim.artifacts.probes.ResponsetimeProbe;
import org.aim.artifacts.records.ResponseTimeRecord;
import org.aim.artifacts.scopes.ServletScope;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.LpeNumericUtils;
import org.lpe.common.util.NumericPairList;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;
import org.spotter.detection.hiccup.utils.Hiccup;
import org.spotter.detection.hiccup.utils.HiccupAnalyzer;
import org.spotter.detection.hiccup.utils.HiccupDetectionConfig;
import org.spotter.detection.hiccup.utils.HiccupDetectionValues;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.SpotterResult;

/**
 * Detection controller for the Response Time Hiccups (RTH) problem.
 * 
 * @author Le-Huan Stefan Tran
 */
public class RTHiccupsDetectionController extends AbstractDetectionController {
	private static final int MILLIS_IN_SECOND = 1000;
	private static final int NUM_EXPERIMENTS = 1;

	private final HiccupDetectionConfig hiccupDetectionConfig;

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *            extension provider.
	 */
	public RTHiccupsDetectionController(IExtension<IDetectionController> provider) {
		super(provider);
		hiccupDetectionConfig = new HiccupDetectionConfig();

	}

	@Override
	protected void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException {
		executeDefaultExperimentSeries(RTHiccupsDetectionController.class, 1, getInstrumentationDescription());
	}

	@Override
	public void loadProperties() {

		String outlierDeviationFactorStr = getProblemDetectionConfiguration().getProperty(
				HiccupDetectionConfig.OUTLIER_DEVIATION_FACTOR_KEY);
		double outlierDeviationFactor = outlierDeviationFactorStr != null && !outlierDeviationFactorStr.isEmpty() ? Double
				.parseDouble(outlierDeviationFactorStr) : HiccupDetectionConfig.OUTLIER_DEVIATION_FACTOR_DEFAULT;

		String minDeviationFromMeanFactorStr = getProblemDetectionConfiguration().getProperty(
				HiccupDetectionConfig.MIN_DEVIATION_FROM_MEAN_FACTOR_KEY);
		double minDeviationFromMeanFactor = minDeviationFromMeanFactorStr != null
				&& !minDeviationFromMeanFactorStr.isEmpty() ? Double.parseDouble(minDeviationFromMeanFactorStr)
				: HiccupDetectionConfig.MIN_DEVIATION_FROM_MEAN_FACTOR_DEFAULT;

		String interHiccupThresholdStr = getProblemDetectionConfiguration().getProperty(
				HiccupDetectionConfig.OUTLIER_DEVIATION_FACTOR_KEY);
		long interHiccupThreshold = interHiccupThresholdStr != null && !interHiccupThresholdStr.isEmpty() ? Long
				.parseLong(interHiccupThresholdStr) : HiccupDetectionConfig.INTER_HICCUP_TIME_DEFAULT;

		String mvaWindowSizeStr = getProblemDetectionConfiguration().getProperty(
				HiccupDetectionConfig.MOVING_AVERAGE_WINDOW_SIZE_KEY);
		int mvaWindowSize = mvaWindowSizeStr != null && !mvaWindowSizeStr.isEmpty() ? Integer
				.parseInt(mvaWindowSizeStr) : HiccupDetectionConfig.MOVING_AVERAGE_WINDOW_SIZE_DEFAULT;

		hiccupDetectionConfig.setInterHiccupThreshold(interHiccupThreshold);
		hiccupDetectionConfig.setMinDeviationFromMeanFactor(minDeviationFromMeanFactor);
		hiccupDetectionConfig.setMvaWindowSize(mvaWindowSize);
		hiccupDetectionConfig.setOutlierDeviationFactor(outlierDeviationFactor);

	}

	private InstrumentationDescription getInstrumentationDescription() throws InstrumentationException {
		InstrumentationDescriptionBuilder idBuilder = new InstrumentationDescriptionBuilder();
		return idBuilder.addAPIInstrumentation(ServletScope.class).addProbe(ResponsetimeProbe.class).entityDone()
				.build();
	}

	@Override
	protected SpotterResult analyze(DatasetCollection data) {
		SpotterResult result = new SpotterResult();
		result.setDetected(false);

		Dataset rtDataSet = data.getDataSet(ResponseTimeRecord.class);
		if (rtDataSet == null || rtDataSet.size() == 0) {
			result.addMessage("Warning: No data available for conducting response time hiccup analysis!");
			return result;
		}
		int i = 0;
		for (String operation : rtDataSet.getValueSet(ResponseTimeRecord.PAR_OPERATION, String.class)) {
			ParameterSelection operationSelection = new ParameterSelection().select(ResponseTimeRecord.PAR_OPERATION,
					operation);
			Dataset selectedDataset = operationSelection.applyTo(rtDataSet);

			NumericPairList<Long, Double> responseTimeSeries = new NumericPairList<>();
			NumericPairList<Long, Double> detectionTimeSeries = new NumericPairList<>();
			NumericPairList<Long, Double> preprocessedDataSeries = new NumericPairList<>();
			List<Hiccup> hiccups = null;
			HiccupDetectionValues hiccupDetectionValues = new HiccupDetectionValues();
			try {

				hiccups = HiccupAnalyzer.findHiccups(selectedDataset, responseTimeSeries, detectionTimeSeries,
						preprocessedDataSeries, hiccupDetectionConfig, hiccupDetectionValues);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Ignoring exception and resuming analysis!");
				i++;
				continue;
			}

			appendCalculatedThresholdsMetaInfo(operation, i, hiccupDetectionValues.getMean(),
					hiccupDetectionValues.getThreshold());
			appendHiccupData(operation, i, hiccups);

			LpeNumericUtils.exportAsCSV(preprocessedDataSeries, getAdditionalResourcesPath() + "noiseReduced-" + i
					+ ".csv", "timestamp", "responsetime");
			LpeNumericUtils.exportAsCSV(responseTimeSeries, getAdditionalResourcesPath() + "ResponseTimeSeries-" + i
					+ ".csv", "timestamp", "responsetime");
			LpeNumericUtils.exportAsCSV(detectionTimeSeries, getAdditionalResourcesPath() + "detection-" + i + ".csv",
					"timestamp", "responsetime");

			// Chart preprocessedChart =
			// HiccupChartExporter.createCombinedDataChart(operation,
			// responseTimeSeries,
			// preprocessedDataSeries);
			// if (preprocessedChart != null) {
			// storeImageChartResource(preprocessedChart, "preProcessed-" + i,
			// result);
			// LpeNumericUtils.exportAsCSV(preprocessedDataSeries,
			// getAdditionalResourcesPath() + "noiseReduced-" + i
			// + ".csv", "timestamp", "responsetime");
			// }
			//
			// Chart rtChart = HiccupChartExporter.createRawDataChart(operation,
			// responseTimeSeries);
			// if (rtChart != null) {
			// storeImageChartResource(rtChart, "ResponseTimeSeries-" + i,
			// result);
			// LpeNumericUtils.exportAsCSV(responseTimeSeries,
			// getAdditionalResourcesPath() + "ResponseTimeSeries-"
			// + i + ".csv", "timestamp", "responsetime");
			// }
			//
			// Chart mvaChart =
			// HiccupChartExporter.createHiccupDataChart(operation,
			// detectionTimeSeries, hiccups,
			// hiccupDetectionValues);
			// if (mvaChart != null) {
			// storeImageChartResource(mvaChart, "detection-" + i, result);
			// LpeNumericUtils.exportAsCSV(detectionTimeSeries,
			// getAdditionalResourcesPath() + "detection-" + i
			// + ".csv", "timestamp", "responsetime");
			// }
			//
			// Chart finalChart =
			// HiccupChartExporter.createHiccupDataChart(operation,
			// responseTimeSeries, hiccups,
			// hiccupDetectionValues);
			// if (rtChart != null) {
			// storeImageChartResource(finalChart, "final-" + i, result);
			// }
			appendOperationMetaInfo(operation, i);
			generateResultMessage(result, operation, hiccups);
			i++;
		}

		return result;

	}

	private void appendOperationMetaInfo(String operation, int number) {
		try {
			FileWriter writer = new FileWriter(getAdditionalResourcesPath() + "operation.info", true);
			writer.append(String.valueOf(number) + " - " + operation);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void appendCalculatedThresholdsMetaInfo(String operation, int number, double mean, double threshold) {
		try {
			FileWriter writer = new FileWriter(getAdditionalResourcesPath() + "thresholds_" + number + ".csv", true);
			writer.append("mean;threshold");
			writer.append("\n");
			writer.append(String.valueOf(mean) + ";" + String.valueOf(threshold));
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void appendHiccupData(String operation, int number, List<Hiccup> hiccups) {
		try {
			FileWriter writer = new FileWriter(getAdditionalResourcesPath() + "hiccups_" + number + ".csv", true);
			writer.append("\"starttime\";\"endtime\";\"maxHeight\";\"avgHeight\";\"maxPreproccedHeight\";\"avgPreproccedHeight\"");
			writer.append("\n");
			for (Hiccup hiccup : hiccups) {
				writer.append("\"" + String.valueOf(hiccup.getStartTimestamp()) + "\";\""
						+ String.valueOf(hiccup.getEndTimestamp()) + "\";\""
						+ String.valueOf(hiccup.getMaxHiccupResponseTime()) + "\";\""
						+ String.valueOf(hiccup.getAvgHiccupResponseTime()) + "\";\""
						+ String.valueOf(hiccup.getMaxPreprocessedResponseTime()) + "\";\""
						+ String.valueOf(hiccup.getAvgPreprocessedResponseTime()) + "\"");
				writer.append("\n");

			}
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void generateResultMessage(SpotterResult result, String operation, List<Hiccup> hiccups) {
		if (!hiccups.isEmpty()) {
			result.setDetected(true);

			double[] peaks = new double[hiccups.size()];
			int i = 0;
			long minTimestamp = Long.MAX_VALUE;
			long maxTimestamp = Long.MIN_VALUE;
			for (Hiccup hiccup : hiccups) {
				peaks[i] = hiccup.getMaxHiccupResponseTime();
				if (hiccup.getStartTimestamp() < minTimestamp) {
					minTimestamp = hiccup.getStartTimestamp();
				}
				if (hiccup.getEndTimestamp() > maxTimestamp) {
					maxTimestamp = hiccup.getEndTimestamp();
				}
				i++;
			}
			double avgPeakHeight = LpeNumericUtils.average(peaks);

			result.addMessage(hiccups.size() + " response time hiccups have been detected!");
			result.addMessage("Operation: " + operation);
			result.addMessage("Average peak height: " + avgPeakHeight + " [ms]");
			if (hiccups.size() > 1) {
				double frequence = MILLIS_IN_SECOND * (double) (hiccups.size() - 1)
						/ (double) (maxTimestamp - minTimestamp); // [1/s]
				result.addMessage("Average hiccup frequence: " + frequence + " [1/s]");
			}
			result.addMessage("Average response time (excluding outliers): "
					+ hiccups.get(0).getAvgResponeTimeWithoutOutliers() + " [ms]");
			result.addMessage("Standard deviation of response times (excluding outliers): "
					+ hiccups.get(0).getStdDevWithoutOutliers() + " [ms]");
			result.addMessage("Hiccup detection threshold: " + hiccups.get(0).getDeviationThreshold() + " [ms]");

		}
	}

	@Override
	protected int getNumOfExperiments() {
		return NUM_EXPERIMENTS;
	}

}
