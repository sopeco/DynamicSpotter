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

import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.spotter.detection.trendlines.LogTrendLine;
import org.spotter.detection.trendlines.TrendLine;
import org.spotter.shared.result.model.SpotterResult;

public class LogAnalyzer extends Analyzer {

	public static final int SMOOTH_WIDE = 1;
	private double maxSlopeFactor;

	/**
	 * 
	 * @param xValues
	 *            x values
	 * @param yValues
	 *            y values
	 * @param maxSlopeFactor
	 *            The factor of the slop of the second half of the measurement
	 *            data relative to the first part.
	 */
	public LogAnalyzer(SpotterResult result, List<Integer> xValues, List<Double> yValues, double maxSlopeFactor) {
		super(result, xValues, yValues);
		this.maxSlopeFactor = maxSlopeFactor;
	}

	@Override
	public AnalyzeResult analyze() {
		result.addMessage("*************************************************");
		result.addMessage("Testing data for stagnating progression..");
		
		TrendLine logTrend = new LogTrendLine();
		logTrend.setValues(toDoubleArray(normalize(yValues, Collections.min(yValues))), toDoubleArray(xValues));

		List<Double> smoothed = smooth(yValues, SMOOTH_WIDE);

		SimpleRegression first = new SimpleRegression();
		first.addData(toDoubleArray(subList(xValues, 0, xValues.size() / 2), subList(smoothed, 0, xValues.size() / 2)));

		SimpleRegression second = new SimpleRegression();
		second.addData(toDoubleArray(subList(xValues, xValues.size() / 2), subList(smoothed, xValues.size() / 2)));

		result.addMessage("> Slope 0%-50%:   " + first.getSlope());
		result.addMessage("> Slope 50%-100%: " + second.getSlope());
		result.addMessage("> Threshold:      " + maxSlopeFactor * first.getSlope());
		
		if (Math.abs(second.getSlope()) < maxSlopeFactor * first.getSlope()) {
			result.addMessage("> detected");
			return AnalyzeResult.POSITIVE;
		} else {
			result.addMessage("> not detected");
			return AnalyzeResult.NEGATIVE;
		}
	}

}
