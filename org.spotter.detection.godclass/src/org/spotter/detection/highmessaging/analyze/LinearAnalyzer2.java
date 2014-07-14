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

import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.spotter.shared.result.model.SpotterResult;

public class LinearAnalyzer2 extends Analyzer {

	public static final int SMOOTH_WIDE = 1;
	private double threshold2;

	public LinearAnalyzer2(SpotterResult result, List<Integer> xValues, List<Double> yValues, double threshold) {
		super(result, xValues, yValues);
		threshold2 = threshold;
	}

	@Override
	public AnalyzeResult analyze() {
		result.addMessage("*************************************************");
		result.addMessage("Testing data for linear progression..");

		List<Double> smoothed = smooth(yValues, SMOOTH_WIDE);
		List<Double> normalized = normalize(smoothed, smoothed.get(0));

		SimpleRegression base = new SimpleRegression();
		base.addData(toDoubleArray(subList(xValues, 0, 5), subList(normalized, 0, 5)));

		SimpleRegression regression = new SimpleRegression();
		regression.addData(toDoubleArray(xValues, normalized));

		result.addMessage("Slope of data progression: " + regression.getSlope());
		result.addMessage("Slope in first " + 5 + " values: " + base.getSlope());
		result.addMessage("Slope threshold: " + base.getSlope() * threshold2);

		if (regression.getSlope() > base.getSlope() * threshold2) {
			result.addMessage("> detected");
			return AnalyzeResult.POSITIVE;
		} else {
			result.addMessage("> not detected");
			return AnalyzeResult.NEGATIVE;
		}
	}

}
