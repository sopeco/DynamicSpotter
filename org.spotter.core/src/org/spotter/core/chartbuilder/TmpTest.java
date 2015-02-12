package org.spotter.core.chartbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lpe.common.util.NumericPairList;

public class TmpTest {
public static void main(String[] args) {
	
	NumericPairList<Double, Double> values = new NumericPairList<>();
	for(int i = 0; i < 100; i++){
		values.add((double)i*1200.0,0.1*(double)(i*i));
	}
	NumericPairList<Double, Double> values2 = new NumericPairList<>();
	List<Number> errors = new ArrayList<>();
	Random r = new Random(System.currentTimeMillis());
	for(int i = 0; i < 10; i++){
		values2.add((double)i*10,(double)i*100);
		errors.add(30.0 + 20.0*r.nextDouble());
	}
	
	
	RChartBuilder builder = new RChartBuilder();
	builder.startChart("Test", "testX", "testY");
//	builder.addCDFSeries(values.getValueList(), "cdf");
	builder.addLineSeries(values2, "ein noch längeres label dings");
//	builder.addTimeSeries(values, "sehr langes label");
//	builder.addTimeSeries(values, "ein noch längeres label dings");
//	builder.addTimeSeries(values, "sehr langes label");
//	builder.addScatterSeriesWithErrorBars(values2,errors, "mYTest2");
//	builder.addHorizontalLine(400, "line");
//	builder.addVerticalLine(40, "line 2");
	builder.build("C:/Users/c5170547/Desktop/test.pdf");
}
}
