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
import java.util.Collections;
import java.util.List;

import org.aim.api.measurement.dataset.Dataset;
import org.aim.api.measurement.dataset.ParameterSelection;
import org.aim.artifacts.records.ResponseTimeRecord;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.LpeNumericUtils;
import org.lpe.common.util.NumericPair;
import org.lpe.common.util.NumericPairList;

public class MovingCenterOfGravityStrategy {
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

		int weightCalculationWindowSize = GlobalConfiguration.getInstance().getPropertyAsInteger("centerOfGravity.weightCalculationWindow", 31);
		int centerOfGravityWindow = GlobalConfiguration.getInstance().getPropertyAsInteger("centerOfGravity.centerOfGravityWindow", 101);

		double deviationFactor = 2.0;

		Double[] weights = new Double[outRTSeries.size()];

		for (int i = 0; i < outRTSeries.size(); i ++) {

			long timestamp = outRTSeries.getPairs().get(i).getKey();
			
			int windowStart = Math.max(i - (centerOfGravityWindow / 2), 0);
			int windowEnd = Math.min(i + (centerOfGravityWindow / 2), outRTSeries.size() - 1);

			double weightedSum = 0.0;
			double sumOfWeigths = 0.0;
		
			for (int j = windowStart; j <= windowEnd; j++) {
				if (weights[j] == null) {
					weights[j] = calculateWeight(outRTSeries, weightCalculationWindowSize, j);
				}
				weightedSum += outRTSeries.get(j).getValue() * weights[j];
				sumOfWeigths += weights[j];
			}

			double centerOfGravity = weightedSum / sumOfWeigths;
		

			if (!Double.isNaN(centerOfGravity) && !Double.isInfinite(centerOfGravity)) {
				detectionSeries.add(new NumericPair<Long, Double>(timestamp, centerOfGravity));
				if (rtWithoutOutliers != null) {
					rtWithoutOutliers.add(new NumericPair<Long, Double>(timestamp, centerOfGravity));
				}
			}

			
		}
		if (detectionSeries.getPairs().isEmpty()) {
			throw new RuntimeException("Empty set");
		}

		double mean = LpeNumericUtils.average(detectionSeries.getValueArrayAsDouble());
		double stdDev = LpeNumericUtils.stdDev(detectionSeries.getValueArrayAsDouble());
		double deviationThreshold = Math.max((mean + deviationFactor * stdDev),
				mean + hiccupConfig.getMinDeviationFromMeanFactor() * mean);
		deviationThreshold = Math.max(deviationThreshold, mean + 50);
		hiccupDetectionValues.setMean(mean);
		hiccupDetectionValues.setThreshold(deviationThreshold);
		List<Hiccup> hiccups = new ArrayList<Hiccup>();
		Hiccup currentHiccup = null;

		double sumPreprocessedValues = 0.0;
		double maxPreprocessedValues = Double.MIN_VALUE;
		double count = 1.0;
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

					currentHiccup.setStartTimestamp(pair.getKey());
					hiccups.add(currentHiccup);
					
					sumPreprocessedValues = 0.0;
					maxPreprocessedValues = Double.MIN_VALUE;
					count = 0.0;
				}
				currentHiccup.setEndTimestamp(pair.getKey());

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

	private static double calculateWeight(final NumericPairList<Long, Double> outRTSeries, int windowSize, int i) {
		double sum = 0;
		double count = 0;
		for (int j = i - windowSize / 2; j <= i + windowSize / 2; j++) {
			if (j < 0 || j >= outRTSeries.size() || i == j) {
				continue;
			}
			double invertedDistance = 1.0 / Math.abs(outRTSeries.get(i).getValue() - outRTSeries.get(j).getValue());
			if (!Double.isNaN(invertedDistance) && !Double.isInfinite(invertedDistance)) {
				sum += invertedDistance;
				count += 1.0;
			}

		}

		return sum / count;
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

	public static <T extends Number> NumericPairList<T, Double> weight(NumericPairList<T, Double> list,
			double noiseThreshold, double percentile) {
		NumericPairList<T, Double> result = new NumericPairList<>();

		if (noiseThreshold > 0) {
			double[] noiseMetrics = new double[list.size()];
			double maxNoise = Double.MIN_VALUE;
			int windowSize = 31;
			for (int i = 0; i < list.size(); i++) {
				double sum = 0;
				double count = 0;
				for (int j = i - windowSize / 2; j <= i + windowSize / 2; j++) {
					if (j < 0 || j >= list.size() || i == j) {
						continue;
					}
					sum += Math.abs(list.get(i).getValue() - list.get(j).getValue());
					count += 1.0;
				}

				noiseMetrics[i] = sum / count;
				if (noiseMetrics[i] > maxNoise) {
					maxNoise = noiseMetrics[i];
				}
			}

			for (int i = 0; i < noiseMetrics.length; i++) {
				double relativeNoise = noiseMetrics[i] / maxNoise;
				if (relativeNoise < noiseThreshold) {
					result.add(new NumericPair<T, Double>(list.get(i).getKey(), list.get(i).getValue()));
				}
			}
		} else {
			double[] noiseMetrics = new double[list.size()];
			double maxNoise = Double.MIN_VALUE;
			int windowSize = 31;
			for (int i = 0; i < list.size(); i++) {
				double sum = 0;
				double count = 0;
				for (int j = i - windowSize / 2; j <= i + windowSize / 2; j++) {
					if (j < 0 || j >= list.size() || i == j) {
						continue;
					}
					sum += Math.abs(list.get(i).getValue() - list.get(j).getValue());
					count += 1.0;
				}

				noiseMetrics[i] = sum / count;
				if (noiseMetrics[i] > maxNoise) {
					maxNoise = noiseMetrics[i];
				}
			}

			List<Double> noisemetricsList = new ArrayList<>();
			for (int i = 0; i < noiseMetrics.length; i++) {
				noiseMetrics[i] = noiseMetrics[i] / maxNoise;
				noisemetricsList.add(noiseMetrics[i]);
			}

			Collections.sort(noisemetricsList);

			int percentileIx = (int) (((double) noisemetricsList.size()) * percentile);
			noiseThreshold = noisemetricsList.get(percentileIx);

			for (int i = 0; i < noiseMetrics.length; i++) {
				if (noiseMetrics[i] < noiseThreshold) {
					result.add(new NumericPair<T, Double>(list.get(i).getKey(), list.get(i).getValue()));
				}
			}
		}

		return result;
	}
}
