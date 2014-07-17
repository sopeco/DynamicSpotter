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

import java.util.List;

import org.aim.api.measurement.dataset.Dataset;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.NumericPairList;

/**
 * Analyzes response times for hiccups.
 * 
 * @author Alexander Wert
 * 
 */
public final class HiccupAnalyzer {

	private HiccupAnalyzer() {
	}

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
		String strategy = GlobalConfiguration.getInstance().getProperty("hiccupStrategy");
		if (strategy == null) {
			return MVAStrategy.findHiccups(rtDataSet, outRTSeries, detectionSeries, rtWithoutOutliers, hiccupConfig, hiccupDetectionValues);
		} else {
			if (strategy.equals("MVAStrategy")) {
				return MVAStrategy.findHiccups(rtDataSet, outRTSeries, detectionSeries, rtWithoutOutliers, hiccupConfig, hiccupDetectionValues);
			} else if (strategy.equals("NoiseReduction")) {
				return NoisReductionStrategy.findHiccups(rtDataSet, outRTSeries, detectionSeries, rtWithoutOutliers,
						hiccupConfig, hiccupDetectionValues);
			} else if (strategy.equals("NoiseAndOutlier")) {
				return NoiseAndOutlierStrategy.findHiccups(rtDataSet, outRTSeries, detectionSeries, rtWithoutOutliers,
						hiccupConfig, hiccupDetectionValues);
			}else if (strategy.equals("BucketOutlierStrategy")) {
				return BucketOutlierStrategy.findHiccups(rtDataSet, outRTSeries, detectionSeries, rtWithoutOutliers,
						hiccupConfig, hiccupDetectionValues);
			}else if (strategy.equals("CenterOfGravityStrategy")) {
				return CenterOfGravityStrategy.findHiccups(rtDataSet, outRTSeries, detectionSeries, rtWithoutOutliers,
						hiccupConfig, hiccupDetectionValues);
			}else if (strategy.equals("MovingCenterOfGravityStrategy")) {
				return MovingCenterOfGravityStrategy.findHiccups(rtDataSet, outRTSeries, detectionSeries, rtWithoutOutliers,
						hiccupConfig, hiccupDetectionValues);
			}
			
			
			
			return MVAStrategy.findHiccups(rtDataSet, outRTSeries, detectionSeries, rtWithoutOutliers, hiccupConfig, hiccupDetectionValues);
		}

	}

}
