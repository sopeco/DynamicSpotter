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
package org.spotter.detection.gc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.instrumentation.description.InstrumentationDescription;
import org.aim.api.instrumentation.description.InstrumentationDescriptionBuilder;
import org.aim.api.measurement.dataset.Dataset;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.api.measurement.dataset.ParameterSelection;
import org.aim.artifacts.records.GCSamplingStatsRecord;
import org.aim.artifacts.records.ResponseTimeRecord;
import org.aim.artifacts.sampler.GarbageCollectionSampler;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.LpeNumericUtils;
import org.lpe.common.util.NumericPairList;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;
import org.spotter.core.detection.IExperimentReuser;
import org.spotter.detection.hiccup.utils.Hiccup;
import org.spotter.detection.hiccup.utils.HiccupAnalyzer;
import org.spotter.detection.hiccup.utils.HiccupChartExporter;
import org.spotter.detection.hiccup.utils.HiccupDetectionConfig;
import org.spotter.detection.hiccup.utils.HiccupDetectionValues;
import org.spotter.detection.hiccup.utils.MVAStrategy;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.SpotterResult;

import com.xeiam.xchart.Chart;

/**
 * Detection controller for the Garbage Collection Overloading (GCO) problem.
 * 
 * @author Le-Huan Stefan Tran
 */
public class GCHiccupDetectionController extends AbstractDetectionController implements IExperimentReuser {
	private static final double HUNDRED = 100.0;
	private static final int CPU_UTIL_WINDOW_SIZE = 5;

	private final HiccupDetectionConfig hiccupDetectionConfig;
	private long gcSamplingDelay;
	private double gcCPUOverloadThreshold;
	private double guiltyFullGCRatioThreshold;
	private double hiccupGCCausePercentageTHreshold;
	private long minHiccupInterval;
	private double hiccupIntervalFactor;

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *            extension provider.
	 */
	public GCHiccupDetectionController(IExtension<IDetectionController> provider) {
		super(provider);
		hiccupDetectionConfig = new HiccupDetectionConfig();

	}

	@Override
	protected void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadProperties() {
		String outlierDeviationFactorStr = getProblemDetectionConfiguration().getProperty(
				HiccupDetectionConfig.OUTLIER_DEVIATION_FACTOR_KEY);
		double outlierDeviationFactor = outlierDeviationFactorStr != null ? Double
				.parseDouble(outlierDeviationFactorStr) : HiccupDetectionConfig.OUTLIER_DEVIATION_FACTOR_DEFAULT;

		String minDeviationFromMeanFactorStr = getProblemDetectionConfiguration().getProperty(
				HiccupDetectionConfig.MIN_DEVIATION_FROM_MEAN_FACTOR_KEY);
		double minDeviationFromMeanFactor = minDeviationFromMeanFactorStr != null ? Double
				.parseDouble(minDeviationFromMeanFactorStr)
				: HiccupDetectionConfig.MIN_DEVIATION_FROM_MEAN_FACTOR_DEFAULT;

		String interHiccupThresholdStr = getProblemDetectionConfiguration().getProperty(
				HiccupDetectionConfig.OUTLIER_DEVIATION_FACTOR_KEY);
		long interHiccupThreshold = interHiccupThresholdStr != null ? Long.parseLong(interHiccupThresholdStr)
				: HiccupDetectionConfig.INTER_HICCUP_TIME_DEFAULT;

		String mvaWindowSizeStr = getProblemDetectionConfiguration().getProperty(
				HiccupDetectionConfig.MOVING_AVERAGE_WINDOW_SIZE_KEY);
		int mvaWindowSize = mvaWindowSizeStr != null ? Integer.parseInt(mvaWindowSizeStr)
				: HiccupDetectionConfig.MOVING_AVERAGE_WINDOW_SIZE_DEFAULT;

		hiccupDetectionConfig.setInterHiccupThreshold(interHiccupThreshold);
		hiccupDetectionConfig.setMinDeviationFromMeanFactor(minDeviationFromMeanFactor);
		hiccupDetectionConfig.setMvaWindowSize(mvaWindowSize);
		hiccupDetectionConfig.setOutlierDeviationFactor(outlierDeviationFactor);

		String gcSamplingDelayStr = getProblemDetectionConfiguration().getProperty(
				GCHiccupExtension.GC_SAMPLING_DELAY_KEY);
		gcSamplingDelay = gcSamplingDelayStr != null ? Long.parseLong(gcSamplingDelayStr)
				: GCHiccupExtension.GC_SAMPLING_DELAY_DEFAULT;

		String gcCPUOverloadThresholdStr = getProblemDetectionConfiguration().getProperty(
				GCHiccupExtension.GC_CPU_OVERLOAD_THRESHOLD_KEY);
		gcCPUOverloadThreshold = gcCPUOverloadThresholdStr != null ? Double.parseDouble(gcCPUOverloadThresholdStr)
				: GCHiccupExtension.GC_CPU_OVERLOAD_THRESHOLD_DEFAULT;

		String guiltyFullGCRatioThresholdStr = getProblemDetectionConfiguration().getProperty(
				GCHiccupExtension.GC_GUILTY_GC_RATIO_THRESHOLD_KEY);
		guiltyFullGCRatioThreshold = guiltyFullGCRatioThresholdStr != null ? Double
				.parseDouble(guiltyFullGCRatioThresholdStr) : GCHiccupExtension.GC_GUILTY_GC_RATIO_THRESHOLD_DEFAULT;

		String hiccupGCCausePercentageTHresholdStr = getProblemDetectionConfiguration().getProperty(
				GCHiccupExtension.HICCUP_GC_CAUSE_PERCENTAGE_THRESHOLD_KEY);
		hiccupGCCausePercentageTHreshold = hiccupGCCausePercentageTHresholdStr != null ? Double
				.parseDouble(hiccupGCCausePercentageTHresholdStr)
				: GCHiccupExtension.HICCUP_GC_CAUSE_PERCENTAGE_THRESHOLD_DEFAULT;

		String minHiccupIntervalStr = getProblemDetectionConfiguration().getProperty(
				GCHiccupExtension.MIN_HICCUP_INTERVAL_KEY);
		minHiccupInterval = minHiccupIntervalStr != null ? Long.parseLong(minHiccupIntervalStr)
				: GCHiccupExtension.MIN_HICCUP_INTERVAL_DEFAULT;

		String hiccupIntervalFactorStr = getProblemDetectionConfiguration().getProperty(
				GCHiccupExtension.HICCUP_INTERVAL_FACTOR_KEY);
		hiccupIntervalFactor = hiccupIntervalFactorStr != null ? Double.parseDouble(hiccupIntervalFactorStr)
				: GCHiccupExtension.HICCUP_INTERVAL_FACTOR_DEFAULT;

	}

	@Override
	public InstrumentationDescription getInstrumentationDescription() {
		InstrumentationDescriptionBuilder idBuilder = new InstrumentationDescriptionBuilder();
		return idBuilder.addSamplingInstruction(GarbageCollectionSampler.class, gcSamplingDelay).build();
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

		Dataset gcDataset = data.getDataSet(GCSamplingStatsRecord.class);

		if (gcDataset == null || gcDataset.size() == 0) {
			result.addMessage("Warning: No data available for conducting GC statistics!");
			return result;
		}

		List<Long> fullGCTimestamps = new ArrayList<>();
		NumericPairList<Long, Double> gcCPUTimes = new NumericPairList<>();
		analyzeGCStatistics(gcDataset, fullGCTimestamps, gcCPUTimes);

		double meanTimeConsumption = LpeNumericUtils.average(gcCPUTimes.getValueList());
		if (meanTimeConsumption > gcCPUOverloadThreshold) {
			result.setDetected(true);
			result.addMessage("Garbage Collection consumes " + meanTimeConsumption
					+ "% of the elapsed time. Threshold is: " + gcCPUOverloadThreshold);
		}

		Chart gcCpuChart = HiccupChartExporter.createGCCPUUtilChart(gcCPUTimes);
		if (gcCpuChart != null) {
			storeImageChartResource(gcCpuChart, "GC-CPU-Util", result);
		}

		for (String operation : rtDataSet.getValueSet(ResponseTimeRecord.PAR_OPERATION, String.class)) {
			ParameterSelection operationSelection = new ParameterSelection().select(ResponseTimeRecord.PAR_OPERATION,
					operation);
			Dataset selectedDataset = operationSelection.applyTo(rtDataSet);

			NumericPairList<Long, Double> responseTimeSeries = new NumericPairList<>();
			NumericPairList<Long, Double> mvaTimeSeries = new NumericPairList<>();
			HiccupDetectionValues hiccupDetectionValues = new HiccupDetectionValues();
			List<Hiccup> hiccups = HiccupAnalyzer.findHiccups(selectedDataset, responseTimeSeries, mvaTimeSeries, null,
					hiccupDetectionConfig, hiccupDetectionValues);

			List<Long> guiltyFullGCTimestamps = new ArrayList<>();
			List<Long> innocentFullGCTimestamps = new ArrayList<>();
			Set<Hiccup> hiccupsCausedByGC = new HashSet<>();
			findGuiltyGCRuns(result, fullGCTimestamps, hiccups, guiltyFullGCTimestamps, innocentFullGCTimestamps,
					hiccupsCausedByGC);

			Chart mvaChart = HiccupChartExporter.createHiccupMVAChartWithGCTimes(operation, mvaTimeSeries, hiccups,
					guiltyFullGCTimestamps, innocentFullGCTimestamps);
			if (mvaChart != null) {
				storeImageChartResource(mvaChart, "GC-Hiccups", result);
			}

		}

		return result;

	}

	private void findGuiltyGCRuns(SpotterResult result, List<Long> fullGCTimestamps, List<Hiccup> hiccups,
			List<Long> guiltyFullGCTimestamps, List<Long> innocentFullGCTimestamps, Set<Hiccup> hiccupsCausedByGC) {
		timestampLoop: for (Long gcTimestamp : fullGCTimestamps) {
			for (Hiccup hiccup : hiccups) {
				long leftIntervalBoundary = Math.min(
						hiccup.getStartTimestamp() - (long) hiccupIntervalFactor * hiccup.getHiccupDuration(),
						hiccup.getStartTimestamp() - minHiccupInterval);
				long rightIntervalBoundary = Math.max(
						hiccup.getEndTimestamp() + (long) hiccupIntervalFactor * hiccup.getHiccupDuration(),
						hiccup.getEndTimestamp() + minHiccupInterval);
				if (gcTimestamp >= leftIntervalBoundary && gcTimestamp <= rightIntervalBoundary) {
					guiltyFullGCTimestamps.add(gcTimestamp);
					hiccupsCausedByGC.add(hiccup);
					continue timestampLoop;
				}
			}
			innocentFullGCTimestamps.add(gcTimestamp);
		}

		double guiltyPercentage = (double) guiltyFullGCTimestamps.size()
				/ (double) (innocentFullGCTimestamps.size() + guiltyFullGCTimestamps.size());
		double percentageHiccupsCausedByGC = (double) hiccupsCausedByGC.size() / (double) hiccups.size();
		if (guiltyPercentage > guiltyFullGCRatioThreshold
				&& percentageHiccupsCausedByGC > hiccupGCCausePercentageTHreshold) {
			result.setDetected(true);
			DecimalFormat dcFormat = new DecimalFormat("#0.00");
			result.addMessage(dcFormat.format(percentageHiccupsCausedByGC * HUNDRED) + " % ("
					+ hiccupsCausedByGC.size() + " of  " + hiccups.size()
					+ ") response time hiccups have been caused by full garbage collection.");
			result.addMessage(dcFormat.format(guiltyPercentage * HUNDRED)
					+ " % of full garbage collections caused a response time hiccup.");
		}
	}

	private void analyzeGCStatistics(Dataset gcDataSet, List<Long> fullGCTimestamps,
			NumericPairList<Long, Double> gcCPUTimes) {

		List<GCSamplingStatsRecord> gcRecords = gcDataSet.getRecords(GCSamplingStatsRecord.class);
		Collections.sort(gcRecords, new Comparator<GCSamplingStatsRecord>() {

			@Override
			public int compare(GCSamplingStatsRecord o1, GCSamplingStatsRecord o2) {
				long diff = o1.getTimeStamp() - o2.getTimeStamp();
				if (diff < 0) {
					return -1;
				} else if (diff > 0) {
					return 1;
				}
				return 0;
			}
		});

		long prevCount = -1;
		long prevTimestamp = -1;
		for (GCSamplingStatsRecord record : gcRecords) {
			if (prevCount >= 0 && (record.getGcOldGenCount() - prevCount > 0)) {
				fullGCTimestamps.add(prevTimestamp);
			}
			prevCount = record.getGcOldGenCount();
			prevTimestamp = record.getTimeStamp();
			gcCPUTimes.add(record.getTimeStamp(), (double) record.getGcNewGenCPUTime() + record.getGcOldGenCPUTime());
		}

		for (int i = 0; i < gcCPUTimes.size(); i++) {
			double percentage = 0.0;
			if (i < gcCPUTimes.size() - 1) {
				double cpuTimeDiff = gcCPUTimes.get(i + 1).getValue() - gcCPUTimes.get(i).getValue();
				long timeDiff = gcCPUTimes.get(i + 1).getKey() - gcCPUTimes.get(i).getKey();
				percentage = cpuTimeDiff / (double) timeDiff;
			}

			gcCPUTimes.get(i).setValue(percentage);
		}

		for (int i = 0; i < gcCPUTimes.size(); i++) {
			double mva = 0.0;
			if (i >= CPU_UTIL_WINDOW_SIZE / 2 && i < (gcCPUTimes.size() - (CPU_UTIL_WINDOW_SIZE / 2))) {
				mva = MVAStrategy.calculateWindowAverage(gcCPUTimes, i, CPU_UTIL_WINDOW_SIZE);
			} else {
				mva = gcCPUTimes.get(i).getValue();
			}
			gcCPUTimes.get(i).setValue(mva);
		}

	}

	@Override
	protected int getNumOfExperiments() {
		return 0;
	}

}
