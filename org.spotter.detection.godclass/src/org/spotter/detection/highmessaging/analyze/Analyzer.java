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
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.spotter.shared.result.model.SpotterResult;

/**
 * Abstract analyzer to analyze a set of x-y related values.
 * 
 * @author Marius Oehler
 * 
 */
public abstract class Analyzer {

	protected List<Integer> xValues;
	protected List<Double> yValues;
	protected SpotterResult result;

	/** Result of the analysis. */
	public enum AnalyzeResult {
		POSITIVE, NEGATIVE, UNKNOWN
	}

	/**
	 * Constructor with specified x and y values.
	 * 
	 * @param xValues
	 *            the x values
	 * @param yValues
	 *            the y values
	 */
	public Analyzer(SpotterResult result, List<Integer> xValues, List<Double> yValues) {
		this.result = result;
		this.xValues = xValues;
		this.yValues = yValues;
	}

	/**
	 * Analyze the data specified in the constructor.
	 * 
	 * @return the result of the analysis
	 */
	public abstract AnalyzeResult analyze();

	protected List<Double> smooth(List<Double> values, int range) {
		List<Double> retValues = new ArrayList<Double>();
		for (int i = 0; i < values.size(); i++) {
			double m = 0;
			int c = 0;
			for (int j = 1; j <= range; j++) {
				if (i - j >= 0) {
					m += values.get(i - j);
					c++;
				}
			}
			m += values.get(i);
			m /= (c + 1);
			retValues.add(m);
		}
		return retValues;
	}

	protected List<Double> normalize(List<Double> values) {
		double max = Collections.max(values);
		return normalize(values, max);
	}

	protected List<Double> normalize(List<Double> values, double base) {
		List<Double> retValues = new ArrayList<Double>();
		for (double val : values) {
			retValues.add(val / base);
		}
		return retValues;
	}

	protected double mean(List<Double> values) {
		SummaryStatistics stats = new SummaryStatistics();
		for (double val : values) {
			stats.addValue(val);
		}
		return stats.getMean();
	}

	protected double standardDeviation(List<Double> values) {
		SummaryStatistics stats = new SummaryStatistics();
		for (double val : values) {
			stats.addValue(val);
		}
		return stats.getStandardDeviation();
	}

	protected List<Double> slopes(List<Double> xValues, List<Double> yValues, int width) {
		List<Double> retValues = new ArrayList<Double>();

		for (int i = 0; i < xValues.size(); i++) {
			int min = (int) Math.max(0, i - width);
			int max = (int) Math.min(xValues.size() - 1, i + width);

			SimpleRegression regression = new SimpleRegression();
			for (int x = min; x <= max; x++) {
				regression.addData(xValues.get(x), yValues.get(x));
			}
			retValues.add(regression.getSlope());
		}

		return retValues;
	}

	@SuppressWarnings("rawtypes")
	protected double[] toDoubleArray(List values) {
		double[] ret = new double[values.size()];
		for (int i = 0; i < values.size(); i++) {
			ret[i] = Double.valueOf("" + values.get(i));
		}
		return ret;
	}

	@SuppressWarnings("rawtypes")
	protected double[][] toDoubleArray(List xVal, List yVal) {
		double[][] ret = new double[xVal.size()][2];
		for (int i = 0; i < xVal.size(); i++) {
			ret[i][0] = Double.valueOf(xVal.get(i).toString());
			ret[i][1] = Double.valueOf(yVal.get(i).toString());
		}
		return ret;
	}

	protected List<Double> toDoubleList(List<?> list) {
		List<Double> retValues = new ArrayList<Double>();
		for (int i = 0; i < list.size(); i++) {
			retValues.add(Double.valueOf(list.get(i).toString()));
		}
		return retValues;
	}

	protected List<Double> subList(List<?> values, int start) {
		return subList(values, start, values.size());
	}

	protected List<Double> subList(List<?> values, int start, int end) {
		List<Double> retValues = new ArrayList<Double>();
		for (int i = start; i < end; i++) {
			retValues.add(Double.valueOf(values.get(i).toString()));
		}
		return retValues;
	}

	protected int[] subArray(int[] source, int start, int length) {
		int[] ret = new int[length];
		for (int i = 0; i < length; i++) {
			ret[i] = source[start + i];
		}
		return ret;
	}

	protected double[] subArray(double[] source, int start, int length) {
		double[] ret = new double[length];
		for (int i = 0; i < length; i++) {
			ret[i] = source[start + i];
		}
		return ret;
	}
}
