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
package org.spotter.core.chartbuilder;

import java.util.Collection;
import java.util.List;

import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.util.NumericPair;
import org.lpe.common.util.NumericPairList;
import org.spotter.shared.configuration.ConfigKeys;

import com.xeiam.xchart.Chart;

/**
 * Image exporter for OLB.
 * 
 * @author C5170547
 * 
 */
public abstract class AnalysisChartBuilder {
	protected static final double _100_PERCENT = 100.0;
	protected static final int IMAGE_WIDTH = 800;
	protected static final int IMAGE_HEIGHT = 500;
	protected int seriesCounter = 0;
	protected double xMin = Double.MAX_VALUE;
	protected double xMax = Double.MIN_VALUE;
	protected double yMin = Double.MAX_VALUE;
	protected double yMax = Double.MIN_VALUE;
	protected String title = "";
	protected String xLabel = "";
	protected String yLabel = "";

	abstract public void startChart(String title, String xLabel, String yLabel);

	abstract public void startChartWithoutLegend(String title, String xLabel, String yLabel);

	abstract public void build(String targetFile);

	abstract public void addUtilizationScatterSeries(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle, boolean scale);

	abstract public void addUtilizationLineSeries(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle, boolean scale);

	abstract public void addScatterSeries(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle);

	abstract public void addScatterSeriesWithErrorBars(NumericPairList<? extends Number, ? extends Number> valuePairs,
			List<Number> errors, String seriesTitle);

	abstract public void addLineSeries(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle);

	abstract public void addCDFSeries(Collection<? extends Number> values, String seriesTitle);

	abstract public void addHorizontalLine(double yValue, String seriesTitle);

	abstract public void addVerticalLine(double xValue, String seriesTitle);


	protected void updateAxisRanges(NumericPairList<? extends Number, ? extends Number> valuePairs) {
		double tmpXMin = valuePairs.getKeyMin().doubleValue();
		double tmpXMax = valuePairs.getKeyMax().doubleValue();
		double tmpYMin = valuePairs.getValueMin().doubleValue();
		double tmpYMax = valuePairs.getValueMax().doubleValue();
		xMin = xMin > tmpXMin ? tmpXMin : xMin;
		xMax = xMax < tmpXMax ? tmpXMax : xMax;
		yMin = yMin > tmpYMin ? tmpYMin : yMin;
		yMax = yMax < tmpYMax ? tmpYMax : yMax;
	}

	protected void updateAxisRanges(double tmpXMin, double tmpXMax, double tmpYMin, double tmpYMax) {
		xMin = xMin > tmpXMin ? tmpXMin : xMin;
		xMax = xMax < tmpXMax ? tmpXMax : xMax;
		yMin = yMin > tmpYMin ? tmpYMin : yMin;
		yMax = yMax < tmpYMax ? tmpYMax : yMax;
	}

	public static AnalysisChartBuilder getChartBuilder() {
		String builderStr = GlobalConfiguration.getInstance().getProperty(ConfigKeys.CHART_BUILDER_KEY,
				ConfigKeys.CHART_BUILDER_XCHART);
		switch (builderStr) {
		case ConfigKeys.CHART_BUILDER_RCHART:
			return new RChartBuilder();
		case ConfigKeys.CHART_BUILDER_XCHART:
		default:
			return new XChartBuilder();
		}
	}

	
	public double addTimeSeries(NumericPairList<? extends Number, ? extends Number> valuePairs, String seriesTitle) {
		double scale = getScale(valuePairs);
		NumericPairList<Double, Double> scaledPairs = scaleSeries(valuePairs,scale);
		addScatterSeries(scaledPairs, seriesTitle);
		return scale;
	}

	public double addTimeSeriesWithErrorBars(NumericPairList<? extends Number, ? extends Number> valuePairs,
			List<Number> errors, String seriesTitle) {
		double scale = getScale(valuePairs);
		NumericPairList<Double, Double> scaledPairs = scaleSeries(valuePairs,scale);
		addScatterSeriesWithErrorBars(scaledPairs, errors, seriesTitle);
		return scale;
	}

	public double addTimeSeriesWithLine(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle) {
		double scale = getScale(valuePairs);
		NumericPairList<Double, Double> scaledPairs = scaleSeries(valuePairs,scale);
		addLineSeries(scaledPairs, seriesTitle);
		return scale;
	}
	
	public void addFixScaledTimeSeries(NumericPairList<? extends Number, ? extends Number> valuePairs, String seriesTitle, double scale) {
		NumericPairList<Double, Double> scaledPairs = scaleSeries(valuePairs,scale);
		addScatterSeries(scaledPairs, seriesTitle);
	}

	public void addFixScaledTimeSeriesWithErrorBars(NumericPairList<? extends Number, ? extends Number> valuePairs,
			List<Number> errors, String seriesTitle, double scale) {
		NumericPairList<Double, Double> scaledPairs = scaleSeries(valuePairs,scale);
		addScatterSeriesWithErrorBars(scaledPairs, errors, seriesTitle);
	}

	public void addFixScaledTimeSeriesWithLine(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle, double scale) {
		NumericPairList<Double, Double> scaledPairs = scaleSeries(valuePairs,scale);
		addLineSeries(scaledPairs, seriesTitle);
	}

	private double getScale(NumericPairList<? extends Number, ? extends Number> valuePairs) {
		double maxTime = valuePairs.getKeyMax().doubleValue();
		double scale = 1.0;
		if (maxTime / 1000.0 > 2.0) {
			maxTime = maxTime / 1000.0;
			scale = scale / 1000.0;
			if (maxTime / 60.0 > 2.0) {
				maxTime = maxTime / 60.0;
				scale = scale / 60.0;
				if (maxTime / 60.0 > 2.0) {
					maxTime = maxTime / 60.0;
					scale = scale / 60.0;
				}
			}
		}
		return scale;
	}

	private NumericPairList<Double, Double> scaleSeries(NumericPairList<? extends Number, ? extends Number> valuePairs,
			double scale) {
		double maxTime = valuePairs.getKeyMax().doubleValue();
		String unit = "[ms]";
		double[] scales = new double[3];
		scales[0] = 0.0011;
		scales[1] = 0.00002;
		scales[2] = 0.0000003;

		if (scale < scales[0]) {
			unit = "[s]";
		} else if (scale < scales[1]) {
			unit = "[min]";
		} else if (scale < scales[2]) {
			unit = "[h]";
		}

		if (xLabel.contains("[")) {
			xLabel = xLabel.substring(0, xLabel.lastIndexOf("["));
			xLabel = xLabel.trim();
		}
		xLabel += " " + unit;
		NumericPairList<Double, Double> scaledPairs = new NumericPairList<>();
		if (!unit.equals("[ms]")) {
			for (NumericPair<? extends Number, ? extends Number> pair : valuePairs) {
				scaledPairs.add(pair.getKey().doubleValue() * scale, pair.getValue().doubleValue());
			}
		} else {
			for (NumericPair<? extends Number, ? extends Number> pair : valuePairs) {
				scaledPairs.add(pair.getKey().doubleValue(), pair.getValue().doubleValue());
			}
		}
		return scaledPairs;
	}
}
