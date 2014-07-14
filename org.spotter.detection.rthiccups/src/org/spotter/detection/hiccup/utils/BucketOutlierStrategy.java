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
import org.aim.api.measurement.dataset.ParameterSelection;
import org.aim.artifacts.records.ResponseTimeRecord;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.LpeNumericUtils;
import org.lpe.common.util.NumericPair;
import org.lpe.common.util.NumericPairList;

public class BucketOutlierStrategy {

	/**
	 * Searches for hiccups in a response time series.
	 * 
	 * @param rtDataSet
	 *            dataset containing response times
	 * @param outRTSeries
	 *            list where calculated response time values should be written
	 *            to
	 * @param detectionSeries
	 *            list where to write calculated moving averages to
	 * @param hiccupConfig
	 *            configures hiccup detection
	 * @return list of detected hiccups
	 */
	public static List<Hiccup> findHiccups(Dataset rtDataSet, final NumericPairList<Long, Double> outRTSeries,
			final NumericPairList<Long, Double> detectionSeries, final NumericPairList<Long, Double> rtWithoutOutliers,
			final HiccupDetectionConfig hiccupConfig, final HiccupDetectionValues hiccupDetectionValues) {
		int numtopResponseTimes = GlobalConfiguration.getInstance().getPropertyAsInteger("numTopRT", 5);

		toTimestampRTPairs(rtDataSet, outRTSeries);
		List<NumericPair<Long, Long>> intervals = new ArrayList<>();
		NumericPairList<Long, Double> currentBucket = new NumericPairList<>();
		int j = 0;
		long minTimestamp = Long.MAX_VALUE;
		long maxTimestamp = Long.MIN_VALUE;
		// create buckets and remove outlier 
		for (NumericPair<Long, Double> pair : outRTSeries) {
			currentBucket.add(pair);
			long ts = pair.getKey();
			if (ts < minTimestamp) {
				minTimestamp = ts;
			}
			if (ts > maxTimestamp) {
				maxTimestamp = ts;
			}
			if (currentBucket.size() == hiccupConfig.getMvaWindowSize() || j == outRTSeries.size() - 1) {
				NumericPairList<Long, Double> pairsWithoutOutliers = LpeNumericUtils
						.filterOutliersInValuesUsingIQR(currentBucket);
				if (rtWithoutOutliers != null) {
					rtWithoutOutliers.getPairs().addAll(pairsWithoutOutliers.getPairs());
				}

				pairsWithoutOutliers.sortByValue();

				double sumRT = 0.0;
				long timestamp = 0L;
				int min = pairsWithoutOutliers.size() - Math.min(pairsWithoutOutliers.size(), numtopResponseTimes);
				int max = pairsWithoutOutliers.size();
				int medianIndex = min + ((max - min) / 2);
				int count = 0;

				for (int i = min; i < max; i++) {
					sumRT += pairsWithoutOutliers.getPairs().get(i).getValue();
					if (i == medianIndex) {
						timestamp = pairsWithoutOutliers.getPairs().get(i).getKey();
					}

					count++;
				}
				detectionSeries.add(new NumericPair<Long, Double>(timestamp, sumRT / (double) count));
				intervals.add(new NumericPair<Long, Long>(minTimestamp, maxTimestamp));
				currentBucket.getPairs().clear();
				minTimestamp = Long.MAX_VALUE;
				maxTimestamp = Long.MIN_VALUE;
			}
			j++;
		}

		double mean = LpeNumericUtils.average(detectionSeries.getValueArrayAsDouble());
		double stdDev = LpeNumericUtils.stdDev(detectionSeries.getValueArrayAsDouble());
		double deviationThreshold = Math.max((mean + hiccupConfig.getOutlierDeviationFactor() * stdDev), mean
				+ hiccupConfig.getMinDeviationFromMeanFactor() * mean);
		deviationThreshold = Math.max(deviationThreshold, mean + 50);
		hiccupDetectionValues.setMean(mean);
		hiccupDetectionValues.setThreshold(deviationThreshold);
		List<Hiccup> hiccups = new ArrayList<Hiccup>();
		Hiccup currentHiccup = null;
		double sumPreprocessedValues = 0.0;
		double maxPreprocessedValues = Double.MIN_VALUE;
		double count = 1.0;
		j = 0;
		for (NumericPair<Long, Double> pair : detectionSeries) {
			double responseTime = pair.getValue();
			long timestamp = pair.getKey();

			if (responseTime > maxPreprocessedValues) {
				maxPreprocessedValues = responseTime;
			}
			sumPreprocessedValues += responseTime;
			if (responseTime >= deviationThreshold) {
				if (currentHiccup == null) {
					// new hiccup begin detected
					currentHiccup = new Hiccup();
					currentHiccup.setAvgResponeTimeWithoutOutliers(mean);
					currentHiccup.setStdDevWithoutOutliers(stdDev);
					currentHiccup.setDeviationThreshold(deviationThreshold);

					currentHiccup.setStartTimestamp(intervals.get(j).getKey());
					hiccups.add(currentHiccup);

					sumPreprocessedValues = 0.0;
					maxPreprocessedValues = Double.MIN_VALUE;
					count = 0.0;
				}
				currentHiccup.setEndTimestamp(intervals.get(j).getValue());

			} else if (currentHiccup != null
					&& (timestamp - currentHiccup.getEndTimestamp()) > hiccupConfig.getInterHiccupThreshold()) {
				ParameterSelection selection = ParameterSelection.newSelection().between(
						ResponseTimeRecord.PAR_TIMESTAMP, currentHiccup.getStartTimestamp(),
						currentHiccup.getEndTimestamp());

				List<Long> hiccupResponseTimes = selection.applyTo(rtDataSet).getValues(
						ResponseTimeRecord.PAR_RESPONSE_TIME, Long.class);

				currentHiccup.setAvgHiccupResponseTime(LpeNumericUtils.average(hiccupResponseTimes));
				currentHiccup.setMaxHiccupResponseTime(LpeNumericUtils.max(hiccupResponseTimes).doubleValue());

				currentHiccup.setAvgPreprocessedResponseTime(sumPreprocessedValues / count);
				currentHiccup.setMaxPreprocessedResponseTime(maxPreprocessedValues);
				currentHiccup = null;

			}
			count += 1.0;
			j++;
		}

		if (currentHiccup != null) {
			ParameterSelection selection = ParameterSelection.newSelection().between(ResponseTimeRecord.PAR_TIMESTAMP,
					currentHiccup.getStartTimestamp(), currentHiccup.getEndTimestamp());

			List<Long> hiccupResponseTimes = selection.applyTo(rtDataSet).getValues(
					ResponseTimeRecord.PAR_RESPONSE_TIME, Long.class);

			currentHiccup.setAvgHiccupResponseTime(LpeNumericUtils.average(hiccupResponseTimes));
			currentHiccup.setMaxHiccupResponseTime(LpeNumericUtils.max(hiccupResponseTimes).doubleValue());
			currentHiccup.setAvgPreprocessedResponseTime(sumPreprocessedValues / count);
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
