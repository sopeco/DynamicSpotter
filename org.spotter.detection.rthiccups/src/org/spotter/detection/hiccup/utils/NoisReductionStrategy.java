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
package org.spotter.detection.hiccup.utils;

import java.util.ArrayList;
import java.util.List;

import org.aim.api.measurement.dataset.Dataset;
import org.aim.artifacts.records.ResponseTimeRecord;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.LpeNumericUtils;
import org.lpe.common.util.NumericPair;
import org.lpe.common.util.NumericPairList;

public class NoisReductionStrategy {
	/**
	 * Searches for hiccups in a response time series.
	 * 
	 * @param rtDataSet
	 *            dataset containing response times
	 * @param outRTSeries
	 *            list where calculated response time values should be written
	 *            to
	 * @param detectionSeries
	 *            list where to write calculated detection data to
	 * @param hiccupConfig
	 *            configures hiccup detection
	 * @return list of detected hiccups
	 */
	public static List<Hiccup> findHiccups(Dataset rtDataSet, final NumericPairList<Long, Double> outRTSeries,
			final NumericPairList<Long, Double> detectionSeries, final NumericPairList<Long, Double> rtWithoutOutliers,
			final HiccupDetectionConfig hiccupConfig, final HiccupDetectionValues hiccupDetectionValues) {

		toTimestampRTPairs(rtDataSet, outRTSeries);

		double threshold = GlobalConfiguration.getInstance().getPropertyAsDouble("test.threshold", 0.5);
		double percentile = GlobalConfiguration.getInstance().getPropertyAsDouble("test.percentile", -1.0);
		NumericPairList<Long, Double> responseTimesWithoutOutliers = LpeNumericUtils.removeNoiseInValues(outRTSeries,
				threshold, percentile, 31);

		if (rtWithoutOutliers != null) {
			rtWithoutOutliers.getPairs().addAll(responseTimesWithoutOutliers.getPairs());
			detectionSeries.getPairs().addAll(responseTimesWithoutOutliers.getPairs());
		}

		double mean = LpeNumericUtils.average(responseTimesWithoutOutliers.getValueArrayAsDouble());
		double stdDev = LpeNumericUtils.stdDev(responseTimesWithoutOutliers.getValueArrayAsDouble());
		double deviationThreshold = Math.max((mean + hiccupConfig.getOutlierDeviationFactor() * stdDev), mean
				+ hiccupConfig.getMinDeviationFromMeanFactor() * mean);
		deviationThreshold = Math.max(deviationThreshold, mean + 50);
		hiccupDetectionValues.setMean(mean);
		hiccupDetectionValues.setThreshold(deviationThreshold);
		List<Hiccup> hiccups = new ArrayList<Hiccup>();
		Hiccup currentHiccup = null;

		int counter = 0;
		double rtSum = 0.0;
		double maxRT = 0.0;
		long lastPeakTimestamp = 0;
		double sumPreprocessedValues = 0.0;
		double maxPreprocessedValues = Double.MIN_VALUE;
		for (int i = 0; i < responseTimesWithoutOutliers.size(); i++) {
			NumericPair<Long, Double> currentPair = responseTimesWithoutOutliers.get(i);
			long timestamp = currentPair.getKey();

			if (currentPair.getValue() > maxPreprocessedValues) {
				maxPreprocessedValues = currentPair.getValue();
			}
			sumPreprocessedValues += currentPair.getValue();

			if (currentHiccup == null) {
				if (currentPair.getValue() > deviationThreshold) {
					// new hiccup
					currentHiccup = new Hiccup();
					currentHiccup.setAvgResponeTimeWithoutOutliers(mean);
					currentHiccup.setStdDevWithoutOutliers(stdDev);
					currentHiccup.setDeviationThreshold(deviationThreshold);
					currentHiccup.setStartTimestamp(timestamp);
					currentHiccup.setEndTimestamp(timestamp);
					hiccups.add(currentHiccup);
					lastPeakTimestamp = timestamp;
					sumPreprocessedValues = 0.0;
					maxPreprocessedValues = Double.MIN_VALUE;
					counter = 0;
					rtSum = 0.0;
					maxRT = 0.0;
				}
			} else {

				double actualRt = currentPair.getValue();
				rtSum += actualRt;
				if (actualRt > maxRT) {
					maxRT = actualRt;
				}
				if (currentPair.getValue() > deviationThreshold) {
					lastPeakTimestamp = timestamp;
					currentHiccup.setEndTimestamp(timestamp);
				} else if (timestamp > lastPeakTimestamp + hiccupConfig.getInterHiccupThreshold()) {
					// hiccup ended
					currentHiccup.setAvgHiccupResponseTime(rtSum / (double) counter);
					currentHiccup.setMaxHiccupResponseTime(maxRT);
					currentHiccup.setAvgPreprocessedResponseTime(sumPreprocessedValues / counter);
					currentHiccup.setMaxPreprocessedResponseTime(maxPreprocessedValues);

					currentHiccup = null;

				}
			}
			counter++;
		}
		if (currentHiccup != null) {
			currentHiccup.setAvgHiccupResponseTime(rtSum / (double) counter);
			currentHiccup.setMaxHiccupResponseTime(maxRT);
			currentHiccup.setAvgPreprocessedResponseTime(sumPreprocessedValues / counter);
			currentHiccup.setMaxPreprocessedResponseTime(maxPreprocessedValues);
		}

		return hiccups;
	}

	/**
	 * Creates from a response time dataset a list of timestamp response time
	 * pairs.
	 * 
	 * @param rtDataSet
	 *            dataset to read from
	 * @param outRTSeries
	 *            list where to write the values to
	 */
	public static void toTimestampRTPairs(Dataset rtDataSet, final NumericPairList<Long, Double> outRTSeries) {
		for (ResponseTimeRecord rtRecord : rtDataSet.getRecords(ResponseTimeRecord.class)) {
			outRTSeries.add(rtRecord.getTimeStamp(), (double) rtRecord.getResponseTime());
		}
		outRTSeries.sort();
	}

}
