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
package org.spotter.detection.olb;

import java.awt.Color;
import java.util.Map;
import java.util.Map.Entry;

import org.lpe.common.util.LpeStringUtils;

import com.xeiam.xchart.Chart;
import com.xeiam.xchart.ChartBuilder;
import com.xeiam.xchart.Series;
import com.xeiam.xchart.SeriesLineStyle;
import com.xeiam.xchart.SeriesMarker;
import com.xeiam.xchart.StyleManager.LegendPosition;

/**
 * Image exporter for OLB.
 * 
 * @author C5170547
 * 
 */
public final class OLBImageExporter {

	private static final double _100 = 100.0;

	private static final int IMAGE_WIDTH = 800;
	private static final int IMAGE_HEIGHT = 500;

	private OLBImageExporter() {
	}
	
	protected static Chart createCpuUtilChart(Map<Integer, Double> cpuMeans, double cpuThreshold) {

		Chart chart = new ChartBuilder().width(IMAGE_WIDTH).height(IMAGE_HEIGHT).title("CPU Utilization")
				.xAxisTitle("Number of Users").yAxisTitle("CPU Utilization [%]").build();

		chart.getStyleManager().setLegendPosition(LegendPosition.InsideSE);

		double[] numUsers = new double[cpuMeans.size()];
		double[] cpuUtils = new double[cpuMeans.size()];
		double[] threashold = new double[cpuMeans.size()];

		int i = 0;
		int maxUsers = 0;
		for (Entry<Integer, Double> entry : cpuMeans.entrySet()) {
			if (entry.getKey() > maxUsers) {
				maxUsers = entry.getKey();
			}
			numUsers[i] = entry.getKey().doubleValue();
			cpuUtils[i] = entry.getValue().doubleValue() * _100;
			threashold[i] = cpuThreshold * _100;
			i++;
		}

		Series cpuUtilSeries = chart.addSeries("utilization", numUsers, cpuUtils);
		cpuUtilSeries.setLineStyle(SeriesLineStyle.SOLID);
		cpuUtilSeries.setMarker(SeriesMarker.DIAMOND);
		cpuUtilSeries.setMarkerColor(Color.BLACK);
		cpuUtilSeries.setLineColor(Color.BLACK);

		Series thresholdSeries = chart.addSeries("threshold", numUsers, threashold);
		thresholdSeries.setLineStyle(SeriesLineStyle.DASH_DASH);
		thresholdSeries.setMarker(SeriesMarker.NONE);
		thresholdSeries.setLineColor(Color.RED);

		chart.getStyleManager().setYAxisMin(0);
		chart.getStyleManager().setYAxisMax(_100);
		chart.getStyleManager().setXAxisMin(0);
		chart.getStyleManager().setXAxisMax(maxUsers);
		return chart;
	}

	protected static Chart createOperationRTChart(String operation, Map<Integer, Double> rtMeans, Map<Integer, Double> stdDevs) {
		String simpleOperationName = LpeStringUtils.getSimpleMethodName(operation);
		Chart chart = new ChartBuilder().width(IMAGE_WIDTH).height(IMAGE_HEIGHT)
				.title("Responset times for " + simpleOperationName + "(...)").xAxisTitle("Number of Users")
				.yAxisTitle("Response time [ms]").build();

		chart.getStyleManager().setLegendPosition(LegendPosition.InsideSE);

		double[] numUsers = new double[rtMeans.size()];
		double[] responseTimes = new double[rtMeans.size()];
		double[] stdDeviations = new double[rtMeans.size()];

		int i = 0;
		int maxUsers = 0;
		double maxYValue = -1;
		for (Integer nUsers : rtMeans.keySet()) {
			if (nUsers > maxUsers) {
				maxUsers = nUsers;
			}

			numUsers[i] = nUsers;
			responseTimes[i] = rtMeans.get(nUsers);
			stdDeviations[i] = stdDevs.get(nUsers) / 2.0;
			if (rtMeans.get(nUsers) + stdDeviations[i] > maxYValue) {
				maxYValue = rtMeans.get(nUsers) + stdDeviations[i];
			}
			i++;
		}

		Series cpuUtilSeries = chart.addSeries("response times", numUsers, responseTimes, stdDeviations);
		cpuUtilSeries.setLineStyle(SeriesLineStyle.NONE);
		cpuUtilSeries.setMarker(SeriesMarker.DIAMOND);
		cpuUtilSeries.setMarkerColor(Color.RED);

		chart.getStyleManager().setYAxisMin(0);
		chart.getStyleManager().setYAxisMax(maxYValue);
		chart.getStyleManager().setXAxisMin(0);
		chart.getStyleManager().setXAxisMax(maxUsers);

		return chart;

	}
}
