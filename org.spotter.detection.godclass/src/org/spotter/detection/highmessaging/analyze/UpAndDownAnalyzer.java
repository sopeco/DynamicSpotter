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

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.spotter.shared.result.model.SpotterResult;

public class UpAndDownAnalyzer extends Analyzer {

	int minCountIncrease = 3;
	double minBorderMarginForDetection = 0.20;

	public UpAndDownAnalyzer(SpotterResult result, List<Integer> xValues, List<Double> yValues) {
		super(result, xValues, yValues);
	}

	@Override
	public AnalyzeResult analyze() {
		result.addMessage("*************************************************");
		result.addMessage("Testing data for increasing and decreasing progression..");
		
		List<Double> valuesSmoothed = smooth(yValues, 3);

		List<Double> normalizedValues = normalize(valuesSmoothed, valuesSmoothed.get(0));

		List<Integer> peakPoints = new ArrayList<Integer>();
		int p = 0;
		while ((p = nextPeak(normalizedValues, p)) != -1) {
			peakPoints.add(p);
		}

		double standardDeviation = standardDeviation(normalizedValues);

		// Find highest peak where the following doesnt exceed value+stdev
		int highestPeak = 0;
		for (; highestPeak < peakPoints.size();) {
			boolean found = false;
			for (int n = highestPeak + 1; n < peakPoints.size(); n++) {
				if (normalizedValues.get(peakPoints.get(highestPeak)) + standardDeviation < normalizedValues
						.get(peakPoints.get(n))) {
					highestPeak = n;
					found = true;
				}
			}
			if (!found) {
				break;
			}
		}
		// System.out.println("h: " + peakPoints.get(highestPeak));

		// Bei keinem Peak => NEGATIVE
		if (peakPoints.isEmpty()) {
			return AnalyzeResult.NEGATIVE;
		}

		// Peak liegt zu nahe an messrand
		if (1D / normalizedValues.size() * peakPoints.get(highestPeak) > 1D - minBorderMarginForDetection) {
			return AnalyzeResult.UNKNOWN;
		}

		List<Double> subYValues = subList(normalizedValues, peakPoints.get(highestPeak));
		List<Double> subXValues = subList(xValues, peakPoints.get(highestPeak));

		SimpleRegression regression = new SimpleRegression();
		regression.addData(toDoubleArray(subXValues, subYValues));

		// System.out.println(regression.getSlope());

		if (regression.getSlope() < 0) {
			result.addMessage("> detected");
			return AnalyzeResult.POSITIVE;
		} else {
			result.addMessage("> not detected");
			return AnalyzeResult.NEGATIVE;
		}
	}

	private int nextPeak(List<Double> values, int start) {
		int up = 0, down = 0, last = 0;
		double peak = 0;
		for (int i = start; i < values.size(); i++) {
			if (i == 0 || peak < values.get(i)) {
				peak = values.get(i);
				up++;
				down = 0;
				last = i;
			} else {
				if (down++ >= 2) {
					if (up >= minCountIncrease) {
						return last;
					}
					up = 0;
					peak = 0;
				}
			}
		}
		return -1;
	}
}
