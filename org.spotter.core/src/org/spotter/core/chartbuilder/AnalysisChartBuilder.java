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
	protected double xScale = 1.0;
	protected double yScale = 1.0;

	abstract public void startChart(String title, String xLabel, String yLabel);

	abstract public void startChartWithoutLegend(String title, String xLabel, String yLabel);

	abstract public void build(String targetFile);

	abstract public void addUtilizationScatterSeries(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle, boolean scale);

	abstract public void addUtilizationLineSeries(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle, boolean scale);

	abstract public void addScatterSeries(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle);

	abstract public void addScatterSeriesWithLine(NumericPairList<? extends Number, ? extends Number> valuePairs,
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
		double xScale = getXScale(valuePairs);
		NumericPairList<Double, Double> scaledPairs = scaleSeriesXAxis(valuePairs, xScale);
		addScatterSeries(scaledPairs, seriesTitle);
		return xScale;
	}

	public double addTimeSeriesWithErrorBars(NumericPairList<? extends Number, ? extends Number> valuePairs,
			List<Number> errors, String seriesTitle) {
		double xScale = getXScale(valuePairs);
		NumericPairList<Double, Double> scaledPairs = scaleSeriesXAxis(valuePairs, xScale);
		addScatterSeriesWithErrorBars(scaledPairs, errors, seriesTitle);
		return xScale;
	}

	public double addTimeSeriesWithLine(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle) {
		double xScale = getXScale(valuePairs);
		NumericPairList<Double, Double> scaledPairs = scaleSeriesXAxis(valuePairs, xScale);
		addLineSeries(scaledPairs, seriesTitle);
		return xScale;
	}

	public void addFixScaledTimeSeries(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle, double scale) {
		NumericPairList<Double, Double> scaledPairs = scaleSeriesXAxis(valuePairs, scale);
		addScatterSeries(scaledPairs, seriesTitle);
	}

	public void addFixScaledTimeSeriesWithErrorBars(NumericPairList<? extends Number, ? extends Number> valuePairs,
			List<Number> errors, String seriesTitle, double scale) {
		NumericPairList<Double, Double> scaledPairs = scaleSeriesXAxis(valuePairs, scale);
		addScatterSeriesWithErrorBars(scaledPairs, errors, seriesTitle);
	}

	public void addFixScaledTimeSeriesWithLine(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle, double scale) {
		NumericPairList<Double, Double> scaledPairs = scaleSeriesXAxis(valuePairs, scale);
		addLineSeries(scaledPairs, seriesTitle);
	}

	protected double getXScale(NumericPairList<? extends Number, ? extends Number> valuePairs) {
		double maxTime = valuePairs.getKeyMax().doubleValue();
		return getScale(maxTime);
	}

	protected double getYScale(NumericPairList<? extends Number, ? extends Number> valuePairs) {
		double maxTime = valuePairs.getValueMax().doubleValue();
		return getScale(maxTime);
	}

	protected double getScale(double maxTime) {
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

	protected NumericPairList<Double, Double> scaleSeriesXAxis(
			NumericPairList<? extends Number, ? extends Number> valuePairs, double scale) {
		xScale = scale;
		String unit = getUnit(scale);

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

	protected NumericPairList<Double, Double> scaleSeriesYAxis(
			NumericPairList<? extends Number, ? extends Number> valuePairs, double scale) {
		scale = Math.min(scale, yScale);
		NumericPairList<Double, Double> scaledPairs = new NumericPairList<>();
		if (!(yLabel.contains("[ms]") || yLabel.contains("[s]") || yLabel.contains("[min]") || yLabel.contains("[h]"))) {
			for (NumericPair<? extends Number, ? extends Number> pair : valuePairs) {
				scaledPairs.add(pair.getKey().doubleValue(), pair.getValue().doubleValue());
			}

			return scaledPairs;
		}
		String unit = getUnit(scale);
		yScale = scale;
		if (yLabel.contains("[")) {
			yLabel = yLabel.substring(0, yLabel.lastIndexOf("["));
			yLabel = yLabel.trim();
		}
		yLabel += " " + unit;

		if (!unit.equals("[ms]")) {
			for (NumericPair<? extends Number, ? extends Number> pair : valuePairs) {
				scaledPairs.add(pair.getKey().doubleValue(), pair.getValue().doubleValue() * scale);
			}
		} else {
			for (NumericPair<? extends Number, ? extends Number> pair : valuePairs) {
				scaledPairs.add(pair.getKey().doubleValue(), pair.getValue().doubleValue());
			}
		}
		return scaledPairs;
	}

	protected String getUnit(double scale) {
		String unit = "[ms]";
		double[] scales = new double[3];
		scales[0] = 0.0011;
		scales[1] = 0.00002;
		scales[2] = 0.0000003;

		if (scale < scales[2]) {
			unit = "[h]";
		} else if (scale < scales[1]) {
			unit = "[min]";
		} else if (scale < scales[0]) {
			unit = "[s]";
		}
		return unit;
	}
}
