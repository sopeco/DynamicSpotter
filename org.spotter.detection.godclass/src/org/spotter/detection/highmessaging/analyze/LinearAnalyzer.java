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
package org.spotter.detection.highmessaging.analyze;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.spotter.shared.result.model.SpotterResult;

public class LinearAnalyzer extends Analyzer {

	private double pctThreshold;
	private int smoothingWide = 2;
	private double minBorderMarginForDetection = 0.3;
	private int slidingWindowSize = 4;

	public LinearAnalyzer(SpotterResult result, List<Integer> xValues, List<Double> yValues, double pctThreshold) {
		super(result, xValues, yValues);
		this.pctThreshold = pctThreshold;
	}

	@Override
	public AnalyzeResult analyze() {
		// Values smoothed
		List<Double> valuesSmoothed = smooth(yValues, smoothingWide);

		// Values smoothed and normalized relative to the first
		List<Double> valuesSmoothedNormalized = normalize(valuesSmoothed, valuesSmoothed.get(0));

		// Check if linear increasing
		List<Double> slopes = new ArrayList<Double>();
		SummaryStatistics statsSlopesSmoothedNormalized = new SummaryStatistics();
		for (int i = 0; i < xValues.size(); i++) {
			double slope = 1;

			if (i != 0) {
				slope = (valuesSmoothedNormalized.get(i) - valuesSmoothedNormalized.get(i - 1))
						/ (xValues.get(i) - xValues.get(i - 1));
			}

			statsSlopesSmoothedNormalized.addValue(slope);
			slopes.add(slope);
			// System.out.println((slope + "").replaceAll("\\.", ","));
		}

		List<Double> slopesSmoothed = smooth(slopes, 2);

		int outlierCount = 0;
		double lowerBound = statsSlopesSmoothedNormalized.getMean()
				- statsSlopesSmoothedNormalized.getStandardDeviation();
		lowerBound = 0.5;
		for (double val : slopesSmoothed) {
			// System.out.println((val + "").replaceAll("\\.", ","));
			if (lowerBound > val) {
				outlierCount++;
			}
		}

		double pctOutlier = 1D / slopes.size() * outlierCount;
//		System.out.println(String.format("> Outlier: %d Pct Outlier: %.2f", outlierCount, pctOutlier));

		if (pctOutlier > 1D - pctThreshold) {
			// Too much outside
			return AnalyzeResult.NEGATIVE;
		} else {
			// Alles ok
			return AnalyzeResult.POSITIVE;
		}
	}
}
