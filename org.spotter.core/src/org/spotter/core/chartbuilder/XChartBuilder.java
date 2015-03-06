package org.spotter.core.chartbuilder;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lpe.common.util.LpeNumericUtils;
import org.lpe.common.util.NumericPair;
import org.lpe.common.util.NumericPairList;

import com.xeiam.xchart.BitmapEncoder;
import com.xeiam.xchart.Chart;
import com.xeiam.xchart.ChartBuilder;
import com.xeiam.xchart.Series;
import com.xeiam.xchart.SeriesLineStyle;
import com.xeiam.xchart.SeriesMarker;
import com.xeiam.xchart.StyleManager.LegendPosition;

public class XChartBuilder extends AnalysisChartBuilder{


	private static final Color[] COLORS = { Color.BLACK, Color.RED, Color.BLUE, Color.ORANGE, Color.GREEN,
			Color.YELLOW, Color.PINK, Color.MAGENTA };

	private Chart chart = null;

	public XChartBuilder() {
	}

	public void startChart(String title, String xLabel, String yLabel) {
		chart = new ChartBuilder().width(IMAGE_WIDTH).height(IMAGE_HEIGHT).title(title).xAxisTitle(xLabel)
				.yAxisTitle(yLabel).build();
		chart.getStyleManager().setLegendPosition(LegendPosition.InsideSE);
	}
	
	public void startChartWithoutLegend(String title, String xLabel, String yLabel) {
		chart = new ChartBuilder().width(IMAGE_WIDTH).height(IMAGE_HEIGHT).title(title).xAxisTitle(xLabel)
				.yAxisTitle(yLabel).build();
		chart.getStyleManager().setLegendPosition(LegendPosition.InsideSE);
		chart.getStyleManager().setLegendVisible(false);
	}

	public void build(String targetFile) {
		chart.getStyleManager().setXAxisMin(xMin);
		chart.getStyleManager().setXAxisMax(xMax);
		chart.getStyleManager().setYAxisMin(yMin);
		chart.getStyleManager().setYAxisMax(yMax);
		try {
			// using savePNGWithDPI method results in the problem, that the
			// file
			// is not released (is blocked)
			BitmapEncoder.savePNG(chart, targetFile);
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	public void addUtilizationScatterSeries(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle, boolean scale) {
		updateAxisRanges(valuePairs.getKeyMin().doubleValue(), valuePairs.getKeyMax().doubleValue(), 0.0, _100_PERCENT);
		Series scatterSeries;
		NumericPairList<Double, Double> scaledPairs = new NumericPairList<>();

		if (scale) {
			for (NumericPair<? extends Number, ? extends Number> pair : valuePairs) {
				scaledPairs.add(pair.getKey().doubleValue(), pair.getValue().doubleValue() * _100_PERCENT);
			}
			scatterSeries = chart.addSeries(seriesTitle, scaledPairs.getKeyListAsNumbers(),
					scaledPairs.getValueListAsNumbers());
		} else {
			scatterSeries = chart.addSeries(seriesTitle, valuePairs.getKeyListAsNumbers(),
					valuePairs.getValueListAsNumbers());
		}

		scatterSeries.setLineStyle(SeriesLineStyle.NONE);
		scatterSeries.setMarker(SeriesMarker.SQUARE);
		scatterSeries.setMarkerColor(COLORS[seriesCounter % COLORS.length]);
		seriesCounter++;
	}

	public void addUtilizationLineSeries(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle, boolean scale) {
		updateAxisRanges(valuePairs.getKeyMin().doubleValue(), valuePairs.getKeyMax().doubleValue(), 0.0, _100_PERCENT);
		Series scatterSeries;
		NumericPairList<Double, Double> scaledPairs = new NumericPairList<>();

		if (scale) {
			for (NumericPair<? extends Number, ? extends Number> pair : valuePairs) {
				scaledPairs.add(pair.getKey().doubleValue(), pair.getValue().doubleValue() * _100_PERCENT);
			}
			scatterSeries = chart.addSeries(seriesTitle, scaledPairs.getKeyListAsNumbers(),
					scaledPairs.getValueListAsNumbers());
		} else {
			scatterSeries = chart.addSeries(seriesTitle, valuePairs.getKeyListAsNumbers(),
					valuePairs.getValueListAsNumbers());
		}
		scatterSeries.setLineStyle(SeriesLineStyle.DASH_DASH);
		scatterSeries.setMarker(SeriesMarker.SQUARE);
		scatterSeries.setMarkerColor(COLORS[seriesCounter % COLORS.length]);
		seriesCounter++;
	}

	public void addScatterSeries(NumericPairList<? extends Number, ? extends Number> valuePairs, String seriesTitle) {
		updateAxisRanges(valuePairs);
		Series scatterSeries = chart.addSeries(seriesTitle, valuePairs.getKeyListAsNumbers(),
				valuePairs.getValueListAsNumbers());
		scatterSeries.setLineStyle(SeriesLineStyle.NONE);
		scatterSeries.setMarker(SeriesMarker.CIRCLE);
		scatterSeries.setMarkerColor(COLORS[seriesCounter % COLORS.length]);
		seriesCounter++;
	}

	public void addScatterSeriesWithErrorBars(NumericPairList<? extends Number, ? extends Number> valuePairs,
			List<Number> errors, String seriesTitle) {
		updateAxisRanges(valuePairs);
		Series scatterSeries = chart.addSeries(seriesTitle, valuePairs.getKeyListAsNumbers(),
				valuePairs.getValueListAsNumbers(), errors);
		scatterSeries.setLineStyle(SeriesLineStyle.NONE);
		scatterSeries.setMarker(SeriesMarker.CIRCLE);
		scatterSeries.setMarkerColor(COLORS[seriesCounter % COLORS.length]);
		seriesCounter++;
	}

	public void addLineSeries(NumericPairList<? extends Number, ? extends Number> valuePairs, String seriesTitle) {
		updateAxisRanges(valuePairs);
		Series scatterSeries = chart.addSeries(seriesTitle, valuePairs.getKeyListAsNumbers(),
				valuePairs.getValueListAsNumbers());
		scatterSeries.setLineStyle(SeriesLineStyle.SOLID);
		scatterSeries.setMarker(SeriesMarker.NONE);
		scatterSeries.setMarkerColor(COLORS[seriesCounter % COLORS.length]);
		seriesCounter++;
	}

	public void addCDFSeries(Collection<? extends Number> values, String seriesTitle) {
		updateAxisRanges(LpeNumericUtils.min(values).doubleValue(), LpeNumericUtils.max(values).doubleValue(), 0.0,
				100.0);
		int size = values.size();
		List<Number> xValues = new ArrayList<>(size);
		List<Number> yValues = new ArrayList<>(size);

		xValues.addAll(values);

		Collections.sort(xValues, new Comparator<Number>() {
			@Override
			public int compare(Number o1, Number o2) {
				if (o1.doubleValue() < o2.doubleValue()) {
					return -1;
				} else if (o1.doubleValue() == o2.doubleValue()) {
					return 0;
				} else {
					return 1;
				}
			}
		});

		double inc = 100.0 / (double) size;
		double sum = 0.0;
		for (int i = 0; i < xValues.size(); i++) {
			sum += inc;
			yValues.add(sum);
		}

		Series scatterSeries = chart.addSeries(seriesTitle, xValues, yValues);
		scatterSeries.setLineStyle(SeriesLineStyle.SOLID);
		scatterSeries.setMarker(SeriesMarker.NONE);
		scatterSeries.setMarkerColor(COLORS[seriesCounter % COLORS.length]);
		seriesCounter++;
	}

	public void addHorizontalLine(double yValue, String seriesTitle) {
		double[] xValues = new double[2];
		double[] yValues = new double[2];
		xValues[0] = xMin;
		xValues[1] = xMax;
		yValues[0] = yValue;
		yValues[1] = yValue;
		Series scatterSeries = chart.addSeries(seriesTitle, xValues, yValues);
		scatterSeries.setLineStyle(SeriesLineStyle.SOLID);
		scatterSeries.setMarker(SeriesMarker.NONE);
		scatterSeries.setMarkerColor(COLORS[seriesCounter % COLORS.length]);
		seriesCounter++;
	}

	public void addVerticalLine(double xValue, String seriesTitle) {
		double[] xValues = new double[2];
		double[] yValues = new double[2];
		xValues[0] = xValue;
		xValues[1] = xValue;
		yValues[0] = yMin;
		yValues[1] = yMax;
		Series scatterSeries = chart.addSeries(seriesTitle, xValues, yValues);
		scatterSeries.setLineStyle(SeriesLineStyle.SOLID);
		scatterSeries.setMarker(SeriesMarker.NONE);
		scatterSeries.setMarkerColor(COLORS[seriesCounter % COLORS.length]);
		seriesCounter++;
	}

	@Override
	public void addScatterSeriesWithLine(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle) {
		updateAxisRanges(valuePairs);
		Series scatterSeries = chart.addSeries(seriesTitle, valuePairs.getKeyListAsNumbers(),
				valuePairs.getValueListAsNumbers());
		scatterSeries.setLineStyle(SeriesLineStyle.SOLID);
		scatterSeries.setMarker(SeriesMarker.CIRCLE);
		scatterSeries.setMarkerColor(COLORS[seriesCounter % COLORS.length]);
		seriesCounter++;
		
	}
}
