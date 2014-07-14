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
package org.spotter.detection.highmessaging;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lpe.common.util.LpeNumericUtils;

import com.xeiam.xchart.Chart;
import com.xeiam.xchart.ChartBuilder;
import com.xeiam.xchart.Series;
import com.xeiam.xchart.SeriesLineStyle;
import com.xeiam.xchart.SeriesMarker;
import com.xeiam.xchart.StyleManager.LegendPosition;

/**
 * Generator for Hiccup chart.
 * 
 * @author C5170547
 * 
 */
public final class ChartExporter {
	private static final int IMAGE_WIDTH = 950;
	private static final int IMAGE_HEIGHT = 500;


	private ChartExporter() {
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
	public static Chart createRawDataChart(String title, String xLabel, String yLabel, List<? extends Number> keys,
			List<? extends Number> values) {
		double maxX = LpeNumericUtils.max(keys).doubleValue();
		double maxY = LpeNumericUtils.max(values).doubleValue();

	
		ChartBuilder chartBuilder = new ChartBuilder();
		chartBuilder.width(IMAGE_WIDTH);
		chartBuilder.height(IMAGE_HEIGHT);
		chartBuilder.title(title);
		chartBuilder.xAxisTitle(xLabel);
		chartBuilder.yAxisTitle(yLabel);
		Chart chart = chartBuilder.build();

		chart.getStyleManager().setLegendPosition(LegendPosition.OutsideE);

		List<Number> xValues = new ArrayList<>();
		for (Number n : keys) {
			xValues.add(n);
		}
		List<Number> yValues = new ArrayList<>();
		for (Number n : values) {
			yValues.add(n);
		}
		Series responseTimeSeries = chart.addSeries("series", xValues, yValues);
		responseTimeSeries.setMarker(SeriesMarker.CIRCLE);
		responseTimeSeries.setMarkerColor(Color.BLUE);
		responseTimeSeries.setLineStyle(SeriesLineStyle.NONE);

		chart.getStyleManager().setYAxisMin(0);
		chart.getStyleManager().setYAxisMax(maxY);
		chart.getStyleManager().setXAxisMin(0);
		chart.getStyleManager().setXAxisMax(maxX);
		return chart;
	}

	
}
