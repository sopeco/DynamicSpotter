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

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.lpe.common.util.LpeStringUtils;
import org.lpe.common.util.NumericPairList;

import com.xeiam.xchart.Chart;
import com.xeiam.xchart.ChartBuilder;
import com.xeiam.xchart.Series;
import com.xeiam.xchart.SeriesColor;
import com.xeiam.xchart.SeriesLineStyle;
import com.xeiam.xchart.SeriesMarker;
import com.xeiam.xchart.StyleManager.LegendPosition;

/**
 * Generator for Hiccup chart.
 * 
 * @author C5170547
 * 
 */
public final class HiccupChartExporter {
	private static final int IMAGE_WIDTH = 950;
	private static final int IMAGE_HEIGHT = 500;
	private static final double KILO = 1000.0;
	private static final double MINUTE = 60.0;
	private static final double HOUR = 60.0;
	private static final double NUM_OF_UNITS_THRESHOLD = 3.0;
	private static final int NUM_INDEXES_PER_HICCUP = 4;
	private static final String METHOD_FORMAT = ".*\\Q(\\E.*\\Q)\\E.*";

	private HiccupChartExporter() {
	}

	/**
	 * Creates a chart showing the response time series as recorded with
	 * detected hiccups.
	 * 
	 * @param operation
	 *            measured operation
	 * @param rtSeries
	 *            response time series
	 * @param hiccups
	 *            detected hiccups
	 * @return a chart
	 */
	public static Chart createRawDataChart(String operation, NumericPairList<Long, Double> rtSeries) {
		if (rtSeries.size() <= 0) {
			return null;
		}
		double maxX = rtSeries.getKeyMax().doubleValue();
		double maxY = rtSeries.getValueMax().doubleValue();

		StringBuilder xAxisUnitBuilder = new StringBuilder();
		StringBuilder yAxisUnitBuilder = new StringBuilder();
		double xScaleFactor = calculateTimeAxisScale(maxX, xAxisUnitBuilder);
		double yScaleFactor = calculateTimeAxisScale(maxY, yAxisUnitBuilder);

		String simpleMethod = getSimpleMethodNameIfPossible(operation);

		ChartBuilder chartBuilder = new ChartBuilder();
		chartBuilder.width(IMAGE_WIDTH);
		chartBuilder.height(IMAGE_HEIGHT);
		chartBuilder.title("Response time Series for method " + simpleMethod);
		chartBuilder.xAxisTitle("Experiment time " + xAxisUnitBuilder.toString());
		chartBuilder.yAxisTitle("Response time " + yAxisUnitBuilder.toString());
		Chart chart = chartBuilder.build();

		chart.getStyleManager().setLegendPosition(LegendPosition.OutsideE);

		Series responseTimeSeries = chart.addSeries("response times", rtSeries.getKeyListAsNumbers(xScaleFactor),
				rtSeries.getValueListAsNumbers(yScaleFactor));
		responseTimeSeries.setMarker(SeriesMarker.CIRCLE);
		responseTimeSeries.setMarkerColor(Color.BLUE);
		responseTimeSeries.setLineStyle(SeriesLineStyle.NONE);

		chart.getStyleManager().setYAxisMin(0);
		chart.getStyleManager().setYAxisMax(maxY * yScaleFactor);
		chart.getStyleManager().setXAxisMin(0);
		chart.getStyleManager().setXAxisMax(maxX * xScaleFactor);
		return chart;
	}

	/**
	 * Creates a chart showing the response time series as recorded with
	 * detected hiccups.
	 * 
	 * @param operation
	 *            measured operation
	 * @param rtSeries
	 *            response time series
	 * @param hiccups
	 *            detected hiccups
	 * @return a chart
	 */
	public static Chart createCombinedDataChart(String operation, NumericPairList<Long, Double> rtSeries,
			NumericPairList<Long, Double> preprocessedData) {
		if (rtSeries.size() <= 0) {
			return null;
		}
		double maxX = rtSeries.getKeyMax().doubleValue();
		double maxY = rtSeries.getValueMax().doubleValue();

		StringBuilder xAxisUnitBuilder = new StringBuilder();
		StringBuilder yAxisUnitBuilder = new StringBuilder();
		double xScaleFactor = calculateTimeAxisScale(maxX, xAxisUnitBuilder);
		double yScaleFactor = calculateTimeAxisScale(maxY, yAxisUnitBuilder);

		String simpleMethod = LpeStringUtils.getSimpleMethodName(operation) + "(...)";

		ChartBuilder chartBuilder = new ChartBuilder();
		chartBuilder.width(IMAGE_WIDTH);
		chartBuilder.height(IMAGE_HEIGHT);
		chartBuilder.title("Response time Series for method " + simpleMethod);
		chartBuilder.xAxisTitle("Experiment time " + xAxisUnitBuilder.toString());
		chartBuilder.yAxisTitle("Response time " + yAxisUnitBuilder.toString());
		Chart chart = chartBuilder.build();

		chart.getStyleManager().setLegendPosition(LegendPosition.OutsideE);

		Series responseTimeSeries = chart.addSeries("response times", rtSeries.getKeyArrayAsDouble(xScaleFactor),
				rtSeries.getValueArrayAsDouble(yScaleFactor));
		responseTimeSeries.setMarker(SeriesMarker.CIRCLE);
		responseTimeSeries.setMarkerColor(Color.BLUE);
		responseTimeSeries.setLineStyle(SeriesLineStyle.NONE);

		Series preprocessedSeries = chart.addSeries("preprocessed data",
				preprocessedData.getKeyArrayAsDouble(xScaleFactor),
				preprocessedData.getValueArrayAsDouble(yScaleFactor));
		preprocessedSeries.setMarker(SeriesMarker.CIRCLE);
		preprocessedSeries.setMarkerColor(Color.RED);
		preprocessedSeries.setLineStyle(SeriesLineStyle.NONE);

		chart.getStyleManager().setYAxisMin(0);
		chart.getStyleManager().setYAxisMax(maxY * yScaleFactor);
		chart.getStyleManager().setXAxisMin(0);
		chart.getStyleManager().setXAxisMax(maxX * xScaleFactor);
		return chart;
	}

	/**
	 * Creates a chart showing the response time series as recorded with
	 * detected hiccups.
	 * 
	 * @param operation
	 *            measured operation
	 * @param rtSeries
	 *            response time series
	 * @param hiccups
	 *            detected hiccups
	 * @return a chart
	 */
	public static Chart createHiccupChart(String operation, NumericPairList<Long, Double> rtSeries, List<Hiccup> hiccups) {
		if (rtSeries.size() <= 0) {
			return null;
		}
		double maxX = rtSeries.getKeyMax().doubleValue();
		double maxY = rtSeries.getValueMax().doubleValue();

		StringBuilder xAxisUnitBuilder = new StringBuilder();
		StringBuilder yAxisUnitBuilder = new StringBuilder();
		double xScaleFactor = calculateTimeAxisScale(maxX, xAxisUnitBuilder);
		double yScaleFactor = calculateTimeAxisScale(maxY, yAxisUnitBuilder);

		String simpleMethod = getSimpleMethodNameIfPossible(operation);

		ChartBuilder chartBuilder = new ChartBuilder();
		chartBuilder.width(IMAGE_WIDTH);
		chartBuilder.height(IMAGE_HEIGHT);
		chartBuilder.title("Response time Series for method " + simpleMethod);
		chartBuilder.xAxisTitle("Experiment time " + xAxisUnitBuilder.toString());
		chartBuilder.yAxisTitle("Response time " + yAxisUnitBuilder.toString());
		Chart chart = chartBuilder.build();

		chart.getStyleManager().setLegendPosition(LegendPosition.OutsideE);

		Series responseTimeSeries = chart.addSeries("response times", rtSeries.getKeyListAsNumbers(xScaleFactor),
				rtSeries.getValueListAsNumbers(yScaleFactor));
		responseTimeSeries.setMarker(SeriesMarker.CIRCLE);
		responseTimeSeries.setMarkerColor(Color.BLUE);
		responseTimeSeries.setLineStyle(SeriesLineStyle.NONE);

		if (!hiccups.isEmpty()) {
			addSeriesForHiccups(chart, hiccups, xScaleFactor, yScaleFactor, maxX);
		}

		chart.getStyleManager().setYAxisMin(0);
		chart.getStyleManager().setYAxisMax(maxY * yScaleFactor);
		chart.getStyleManager().setXAxisMin(0);
		chart.getStyleManager().setXAxisMax(maxX * xScaleFactor);
		return chart;
	}

	/**
	 * 
	 * @param operation
	 *            measured operation
	 * @param rtSeries
	 *            moving average series
	 * @param hiccups
	 *            detected hiccups
	 * @return chart
	 */
	public static Chart createHiccupDataChart(String operation, NumericPairList<Long, Double> rtSeries,
			List<Hiccup> hiccups, HiccupDetectionValues hiccupDetectionValues) {
		if (rtSeries.size() <= 0) {
			return null;
		}
		double maxX = rtSeries.getKeyMax().doubleValue();
		double minX = rtSeries.getKeyMin().doubleValue();
		double maxY = rtSeries.getValueMax().doubleValue();

		for (Hiccup hiccup : hiccups) {
			if (hiccup.getMaxHiccupResponseTime() > maxY) {
				maxY = hiccup.getMaxHiccupResponseTime();
			}
		}

		StringBuilder xAxisUnitBuilder = new StringBuilder();
		StringBuilder yAxisUnitBuilder = new StringBuilder();
		double xScaleFactor = calculateTimeAxisScale(maxX, xAxisUnitBuilder);
		double yScaleFactor = calculateTimeAxisScale(maxY, yAxisUnitBuilder);

		String simpleMethod = getSimpleMethodNameIfPossible(operation);

		ChartBuilder chartBuilder = new ChartBuilder();
		chartBuilder.width(IMAGE_WIDTH);
		chartBuilder.height(IMAGE_HEIGHT);
		chartBuilder.title("Hiccup detection series for method " + simpleMethod);
		chartBuilder.xAxisTitle("Experiment time " + xAxisUnitBuilder.toString());
		chartBuilder.yAxisTitle("Response time moving average" + yAxisUnitBuilder.toString());
		Chart chart = chartBuilder.build();

		chart.getStyleManager().setLegendPosition(LegendPosition.OutsideE);

		Series responseTimeSeries = chart.addSeries("response times", rtSeries.getKeyListAsNumbers(xScaleFactor),
				rtSeries.getValueListAsNumbers(yScaleFactor));
		responseTimeSeries.setMarker(SeriesMarker.CIRCLE);
		responseTimeSeries.setMarkerColor(Color.BLUE);
		responseTimeSeries.setLineStyle(SeriesLineStyle.NONE);

		addMeanSeries(hiccupDetectionValues.getMean() * yScaleFactor, maxX * xScaleFactor, minX * xScaleFactor, chart,
				yAxisUnitBuilder);
		addThresholdSeries(hiccupDetectionValues.getThreshold() * yScaleFactor, maxX * xScaleFactor, minX
				* xScaleFactor, chart, yAxisUnitBuilder);

		if (!hiccups.isEmpty()) {
			addSeriesForHiccups(chart, hiccups, xScaleFactor, yScaleFactor, maxX);
		}

		chart.getStyleManager().setYAxisMin(0);
		chart.getStyleManager().setYAxisMax(maxY * yScaleFactor);
		chart.getStyleManager().setXAxisMin(0);
		chart.getStyleManager().setXAxisMax(maxX * xScaleFactor);
		return chart;
	}

	/**
	 * 
	 * @param gcCPUTimes
	 *            ratios gc time vs. elapsed time
	 * @return chart
	 */
	public static Chart createGCCPUUtilChart(NumericPairList<Long, Double> gcCPUTimes) {
		if (gcCPUTimes.size() <= 0) {
			return null;
		}
		double maxX = gcCPUTimes.getKeyMax().doubleValue();
		double maxY = gcCPUTimes.getValueMax().doubleValue();

		StringBuilder xAxisUnitBuilder = new StringBuilder();
		double xScaleFactor = calculateTimeAxisScale(maxX, xAxisUnitBuilder);

		ChartBuilder chartBuilder = new ChartBuilder();
		chartBuilder.width(IMAGE_WIDTH);
		chartBuilder.height(IMAGE_HEIGHT);
		chartBuilder.title("GC CPU overhead");
		chartBuilder.xAxisTitle("Experiment time " + xAxisUnitBuilder.toString());
		chartBuilder.yAxisTitle("CTP [%]");
		Chart chart = chartBuilder.build();

		chart.getStyleManager().setLegendPosition(LegendPosition.OutsideE);

		Series responseTimeSeries = chart.addSeries("CPU time / elapsed time",
				gcCPUTimes.getKeyListAsNumbers(xScaleFactor), gcCPUTimes.getValueListAsNumbers());
		responseTimeSeries.setMarker(SeriesMarker.NONE);
		responseTimeSeries.setLineStyle(SeriesLineStyle.SOLID);
		responseTimeSeries.setLineColor(Color.BLUE);

		chart.getStyleManager().setYAxisMin(0);
		chart.getStyleManager().setYAxisMax(maxY);
		chart.getStyleManager().setXAxisMin(0);
		chart.getStyleManager().setXAxisMax(maxX * xScaleFactor);
		return chart;
	}

	/**
	 * 
	 * @param operation
	 *            measured operation
	 * @param mvaSeries
	 *            moving averages in response times
	 * @param hiccups
	 *            detected hiccups
	 * @param guiltyFullGCTimestamps
	 *            timestamps of full garbage collection causing a hiccup
	 * @param innocentFullGCTimestamps
	 *            timestamps of full garbage collection NOT causing a hiccup
	 * @return chart
	 */
	public static Chart createHiccupMVAChartWithGCTimes(String operation, NumericPairList<Long, Double> mvaSeries,
			List<Hiccup> hiccups, List<Long> guiltyFullGCTimestamps, List<Long> innocentFullGCTimestamps) {
		if (mvaSeries.size() <= 0) {
			return null;
		}
		double maxX = mvaSeries.getKeyMax().doubleValue();
		double maxY = mvaSeries.getValueMax().doubleValue();

		for (Hiccup hiccup : hiccups) {
			if (hiccup.getMaxHiccupResponseTime() > maxY) {
				maxY = hiccup.getMaxHiccupResponseTime();
			}
		}

		StringBuilder xAxisUnitBuilder = new StringBuilder();
		StringBuilder yAxisUnitBuilder = new StringBuilder();
		double xScaleFactor = calculateTimeAxisScale(maxX, xAxisUnitBuilder);
		double yScaleFactor = calculateTimeAxisScale(maxY, yAxisUnitBuilder);

		String simpleMethod = getSimpleMethodNameIfPossible(operation);

		ChartBuilder chartBuilder = new ChartBuilder();
		chartBuilder.width(IMAGE_WIDTH);
		chartBuilder.height(IMAGE_HEIGHT);
		chartBuilder.title("Hiccup detection series for method " + simpleMethod);
		chartBuilder.xAxisTitle("Experiment time " + xAxisUnitBuilder.toString());
		chartBuilder.yAxisTitle("Response time moving average" + yAxisUnitBuilder.toString());
		Chart chart = chartBuilder.build();

		chart.getStyleManager().setLegendPosition(LegendPosition.OutsideE);

		Series responseTimeSeries = chart.addSeries("response times", mvaSeries.getKeyListAsNumbers(xScaleFactor),
				mvaSeries.getValueListAsNumbers(yScaleFactor));
		responseTimeSeries.setMarker(SeriesMarker.CIRCLE);
		responseTimeSeries.setMarkerColor(Color.BLUE);
		responseTimeSeries.setLineStyle(SeriesLineStyle.NONE);

		if (!hiccups.isEmpty()) {
			addSeriesForHiccups(chart, hiccups, xScaleFactor, yScaleFactor, maxX);
		}

		if (!guiltyFullGCTimestamps.isEmpty()) {
			List<Number> gcGuiltyTimestamps = new ArrayList<>();
			List<Number> gcGuiltyValue = new ArrayList<>();
			for (Long gcTimestamp : guiltyFullGCTimestamps) {
				gcGuiltyTimestamps.add(gcTimestamp * xScaleFactor);
				gcGuiltyValue.add(0.0);
			}
			Series rgcGuiltyTimeSeries = chart.addSeries("Full GC causing hiccups", gcGuiltyTimestamps, gcGuiltyValue);
			rgcGuiltyTimeSeries.setMarker(SeriesMarker.SQUARE);
			rgcGuiltyTimeSeries.setMarkerColor(Color.RED);
			rgcGuiltyTimeSeries.setLineStyle(SeriesLineStyle.NONE);
		}

		if (!innocentFullGCTimestamps.isEmpty()) {
			List<Number> gcInnocentTimestamps = new ArrayList<>();
			List<Number> gcInnocentValue = new ArrayList<>();
			for (Long gcTimestamp : innocentFullGCTimestamps) {
				gcInnocentTimestamps.add(gcTimestamp * xScaleFactor);
				gcInnocentValue.add(0.0);
			}
			Series rgcInnocentTimeSeries = chart.addSeries("Full GC", gcInnocentTimestamps, gcInnocentValue);
			rgcInnocentTimeSeries.setMarker(SeriesMarker.SQUARE);
			rgcInnocentTimeSeries.setMarkerColor(Color.GRAY);
			rgcInnocentTimeSeries.setLineStyle(SeriesLineStyle.NONE);
		}

		chart.getStyleManager().setYAxisMin(0);
		chart.getStyleManager().setYAxisMax(maxY * yScaleFactor);
		chart.getStyleManager().setXAxisMin(0);
		chart.getStyleManager().setXAxisMax(maxX * xScaleFactor);
		return chart;
	}

	private static void addMeanSeries(double level, double maxX, double minX, Chart chart,
			StringBuilder yAxisUnitBuilder) {
		double[] meanSeriesX = { minX, maxX };
		double[] meanSeriesY = { level, level };

		DecimalFormat format = new DecimalFormat("0.0#");

		Series meanSeries = chart.addSeries("mean(R): " + format.format(level) + " " + yAxisUnitBuilder.toString(),
				meanSeriesX, meanSeriesY);
		meanSeries.setMarker(SeriesMarker.NONE);
		meanSeries.setLineStyle(SeriesLineStyle.DOT_DOT);
		meanSeries.setLineColor(SeriesColor.ORANGE);
	}

	private static void addThresholdSeries(double level, double maxX, double minX, Chart chart,
			StringBuilder yAxisUnitBuilder) {
		double[] thresholdSeriesX = { minX, maxX };
		double[] thresholdSeriesY = { level, level };
		DecimalFormat format = new DecimalFormat("0.0#");
		Series thresholdSeries = chart.addSeries(
				"hiccup threshold: " + format.format(level) + " " + yAxisUnitBuilder.toString(), thresholdSeriesX,
				thresholdSeriesY);
		thresholdSeries.setMarker(SeriesMarker.NONE);
		thresholdSeries.setLineStyle(SeriesLineStyle.DASH_DASH);
		thresholdSeries.setLineColor(SeriesColor.RED);
	}

	private static double calculateTimeAxisScale(double maxValue, StringBuilder axisTitleBuilder) {
		double scaleFactor = 0;

		if (maxValue > HOUR * MINUTE * KILO * NUM_OF_UNITS_THRESHOLD) {
			scaleFactor = 1.0 / (HOUR * MINUTE * KILO);
			axisTitleBuilder.append("[h]");
		} else if (maxValue > MINUTE * KILO * NUM_OF_UNITS_THRESHOLD) {
			scaleFactor = 1.0 / (MINUTE * KILO);
			axisTitleBuilder.append("[min]");
		} else if (maxValue > KILO * NUM_OF_UNITS_THRESHOLD) {
			scaleFactor = 1.0 / KILO;
			axisTitleBuilder.append("[s]");
		} else {
			scaleFactor = 1.0;
			axisTitleBuilder.append("[ms]");
		}
		return scaleFactor;
	}

	private static void addSeriesForHiccups(Chart chart, List<Hiccup> hiccups, double xScaleFactor,
			double yScaleFactor, double maxX) {

		double[] boxX = new double[hiccups.size() * NUM_INDEXES_PER_HICCUP + 2];
		double[] boxY = new double[hiccups.size() * NUM_INDEXES_PER_HICCUP + 2];
		int i = 0;
		boxX[0] = 0.0;
		boxX[hiccups.size() * NUM_INDEXES_PER_HICCUP + 1] = maxX * xScaleFactor;
		boxY[0] = 0.0;
		boxY[hiccups.size() * NUM_INDEXES_PER_HICCUP + 1] = 0.0;
		for (Hiccup hiccup : hiccups) {
			int index = i * NUM_INDEXES_PER_HICCUP + 1;
			boxX[index] = hiccup.getStartTimestamp() * xScaleFactor;
			boxY[index] = 0.0;
			index++;
			boxX[index] = hiccup.getStartTimestamp() * xScaleFactor + (1 / KILO);
			boxY[index] = hiccup.getMaxHiccupResponseTime() * yScaleFactor;
			index++;
			boxX[index] = hiccup.getEndTimestamp() * xScaleFactor;
			boxY[index] = hiccup.getMaxHiccupResponseTime() * yScaleFactor;
			index++;
			boxX[index] = hiccup.getEndTimestamp() * xScaleFactor + (1 / KILO);
			boxY[index] = 0.0;
			i++;
		}

		Series boxSeries = chart.addSeries("hiccups", boxX, boxY);
		boxSeries.setLineStyle(SeriesLineStyle.SOLID);
		boxSeries.setMarker(SeriesMarker.NONE);
		boxSeries.setLineColor(Color.ORANGE);
	}

	/*
	 * Method names of Loadrunner data does not match the expected
	 * method name format in LpeStringUtils.getSimpleMethodName(...)
	 */
	private static String getSimpleMethodNameIfPossible(String operation) {
		if (operation.matches(METHOD_FORMAT)) {
			return LpeStringUtils.getSimpleMethodName(operation) + "(...)";
		} else if (operation.length() > 30) {
			return operation.substring(0, 28) + "...";
		} else {
			return operation;
		}
	}
}
