package org.spotter.core.chartbuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lpe.common.util.LpeFileUtils;
import org.lpe.common.util.LpeNumericUtils;
import org.lpe.common.util.NumericPair;
import org.lpe.common.util.NumericPairList;
import org.lpe.common.util.system.LpeSystemUtils;

public class RChartBuilder extends AnalysisChartBuilder {

	private static final String DYNAMIC_SPOTTER_DIR = "DynamicSpotter";

	private static final String EMPTY_PLOT = "plot(c(), c(), main=plotTitle, "
			+ "xlab=xLabel, ylab=yLabel, type=\"n\",ylim=yRange,xlim=xRange,cex.lab=1.4,cex.axis=1.5)\n";

	private StringBuilder scriptBuilder = new StringBuilder();
	private StringBuilder legendLinesBuilder = null;
	private StringBuilder legendPointsBuilder = null;
	private StringBuilder legendNamesBuilder = null;

	private int pointCounter = 1;
	private int lineCounter = 1;

	@Override
	public void startChart(String title, String xLabel, String yLabel) {
		this.title = title;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		legendNamesBuilder = new StringBuilder();
		legendNamesBuilder.append("c(");
		legendPointsBuilder = new StringBuilder();
		legendPointsBuilder.append("c(");
		legendLinesBuilder = new StringBuilder();
		legendLinesBuilder.append("c(");
	}

	@Override
	public void startChartWithoutLegend(String title, String xLabel, String yLabel) {
		this.title = title;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
	}

	@Override
	public void build(String targetFile) {
		finishLegend();
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(getTitlesString());
		strBuilder.append(getRangesString());

		strBuilder.append("pdf(width=6.7, height=6, file='"
				+ targetFile.replace(System.getProperty("file.separator"), "/") + "')\n");
		strBuilder.append("par(mar=c(10.1, 4.4, 4.1, 2.1), xpd=TRUE)\n");
		strBuilder.append(EMPTY_PLOT);
		strBuilder.append(scriptBuilder.toString());
		if (legendNamesBuilder != null) {

			strBuilder.append("legend(\"bottom\",inset=c(0,-0.55),legend=" + legendNamesBuilder.toString() + ",lty="
					+ legendLinesBuilder.toString() + ",lwd=2,pch=" + legendPointsBuilder.toString()
					+ ",bg=\"white\",cex=1.3,bty =\"n\",ncol=" + (seriesCounter <= 1 ? "1" : "2") + ")\n");
		}
		strBuilder.append("dev.off()\n");
		String script = strBuilder.toString();
		String scriptFile = storeScriptFile(script);

		extractCopy(targetFile.replace(System.getProperty("file.separator"), "/"), script);
		
		switch (LpeSystemUtils.getOperatingSystemType()) {
		case LINUX:
			break;
		case MAC:
			break;
		case WINDOWS:
			try {
				Process p = Runtime.getRuntime().exec("Rscript.exe " + scriptFile);
				p.waitFor();
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			break;

		}
		cleanUpTmpFiles();
	}

	private String storeScriptFile(String script) {
		String scriptFile = LpeFileUtils.concatFileName(LpeSystemUtils.getSystemTempDir(), DYNAMIC_SPOTTER_DIR);
		scriptFile = LpeFileUtils.concatFileName(scriptFile, "chartTmp");
		scriptFile = LpeFileUtils.concatFileName(scriptFile, "script-" + seriesCounter + ".r");
		try {
			FileWriter fWriter = new FileWriter(scriptFile, false);
			fWriter.append(script);
			fWriter.flush();

			fWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return scriptFile;
	}

	@Override
	public void addUtilizationScatterSeries(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle, boolean scale) {
		updateAxisRanges(valuePairs.getKeyMin().doubleValue(), valuePairs.getKeyMax().doubleValue(), 0.0, _100_PERCENT);
		NumericPairList<Double, Double> scaledPairs = new NumericPairList<>();
		String dataFile = "";
		if (scale) {
			for (NumericPair<? extends Number, ? extends Number> pair : valuePairs) {
				scaledPairs.add(pair.getKey().doubleValue(), pair.getValue().doubleValue() * _100_PERCENT);
			}
			dataFile = storeCSV(scaledPairs);

		} else {
			dataFile = storeCSV(valuePairs);
		}
		scriptBuilder.append("data <- read.csv(file=\"" + dataFile + "\",head=TRUE,sep=\";\")\n");
		scriptBuilder.append("xData <- data[[1]]\n");
		scriptBuilder.append("yData <- data[[2]]\n");
		scriptBuilder.append("points(xData, yData,pch=" + pointCounter + ")\n");
		addLegendItem(seriesTitle, true, false);
		seriesCounter++;
		pointCounter++;
	}

	@Override
	public void addUtilizationLineSeries(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle, boolean scale) {
		updateAxisRanges(valuePairs.getKeyMin().doubleValue(), valuePairs.getKeyMax().doubleValue(), 0.0, _100_PERCENT);
		NumericPairList<Double, Double> scaledPairs = new NumericPairList<>();
		String dataFile = "";
		if (scale) {
			for (NumericPair<? extends Number, ? extends Number> pair : valuePairs) {
				scaledPairs.add(pair.getKey().doubleValue(), pair.getValue().doubleValue() * _100_PERCENT);
			}
			dataFile = storeCSV(scaledPairs);

		} else {
			dataFile = storeCSV(valuePairs);
		}
		scriptBuilder.append("data <- read.csv(file=\"" + dataFile + "\",head=TRUE,sep=\";\")\n");
		scriptBuilder.append("xData <- data[[1]]\n");
		scriptBuilder.append("yData <- data[[2]]\n");
		scriptBuilder.append("points(xData, yData,pch=" + pointCounter + ")\n");
		scriptBuilder.append("lines(xData, yData,lty=" + lineCounter + ",lwd=2)\n");
		addLegendItem(seriesTitle, true, true);
		seriesCounter++;
		pointCounter++;
		lineCounter++;
	}

	@Override
	public void addScatterSeries(NumericPairList<? extends Number, ? extends Number> valuePairs, String seriesTitle) {
		valuePairs = scaleSeriesYAxis(valuePairs, getYScale(valuePairs));
		updateAxisRanges(valuePairs);
		String dataFile = storeCSV(valuePairs);
		scriptBuilder.append("data <- read.csv(file=\"" + dataFile + "\",head=TRUE,sep=\";\")\n");
		scriptBuilder.append("xData <- data[[1]]\n");
		scriptBuilder.append("yData <- data[[2]]\n");
		scriptBuilder.append("points(xData, yData,pch=" + pointCounter + ")\n");
		addLegendItem(seriesTitle, true, false);
		seriesCounter++;
		pointCounter++;
	}

	@Override
	public void addScatterSeriesWithLine(NumericPairList<? extends Number, ? extends Number> valuePairs,
			String seriesTitle) {
		valuePairs = scaleSeriesYAxis(valuePairs, getYScale(valuePairs));
		updateAxisRanges(valuePairs);
		String dataFile = storeCSV(valuePairs);
		scriptBuilder.append("data <- read.csv(file=\"" + dataFile + "\",head=TRUE,sep=\";\")\n");
		scriptBuilder.append("xData <- data[[1]]\n");
		scriptBuilder.append("yData <- data[[2]]\n");
		scriptBuilder.append("points(xData, yData,pch=" + pointCounter + ")\n");
		scriptBuilder.append("lines(xData, yData,lty=" + lineCounter + ",lwd=2)\n");
		addLegendItem(seriesTitle, true, true);
		seriesCounter++;
		pointCounter++;
		lineCounter++;

	}

	@Override
	public void addScatterSeriesWithErrorBars(NumericPairList<? extends Number, ? extends Number> valuePairs,
			List<Number> errors, String seriesTitle) {
		double yScale = getYScale(valuePairs);
		valuePairs = scaleSeriesYAxis(valuePairs, yScale);
		List<Number> scaledErrors = new ArrayList<>();
		for (Number n : errors) {
			scaledErrors.add(n.doubleValue() * yScale);
		}
		errors = scaledErrors;

		NumericPairList<Double, Double> minPairs = new NumericPairList<>();
		NumericPairList<Double, Double> maxPairs = new NumericPairList<>();

		int i = 0;
		for (NumericPair<? extends Number, ? extends Number> pair : valuePairs) {
			minPairs.add(pair.getKey().doubleValue(), pair.getValue().doubleValue() - errors.get(i).doubleValue());
			maxPairs.add(pair.getKey().doubleValue(), pair.getValue().doubleValue() + errors.get(i).doubleValue());
			i++;
		}
		updateAxisRanges(minPairs);
		updateAxisRanges(maxPairs);
		String dataFile = storeCSV(valuePairs.getKeyList(), valuePairs.getValueList(), errors);
		scriptBuilder.append("data <- read.csv(file=\"" + dataFile + "\",head=TRUE,sep=\";\")\n");
		scriptBuilder.append("xData <- data[[1]]\n");
		scriptBuilder.append("yData <- data[[2]]\n");
		scriptBuilder.append("errors <- data[[3]]\n");
		scriptBuilder.append("points(xData, yData,pch=" + pointCounter + ")\n");
		scriptBuilder.append("arrows(xData, yData-errors,xData,yData+errors,length=0.05, angle=90, code=3)\n");
		addLegendItem(seriesTitle, true, false);
		seriesCounter++;
		pointCounter++;
	}

	@Override
	public void addLineSeries(NumericPairList<? extends Number, ? extends Number> valuePairs, String seriesTitle) {
		valuePairs = scaleSeriesYAxis(valuePairs, getYScale(valuePairs));
		updateAxisRanges(valuePairs);
		String dataFile = storeCSV(valuePairs);
		scriptBuilder.append("data <- read.csv(file=\"" + dataFile + "\",head=TRUE,sep=\";\")\n");
		scriptBuilder.append("xData <- data[[1]]\n");
		scriptBuilder.append("yData <- data[[2]]\n");
		scriptBuilder.append("lines(xData, yData,lty=" + lineCounter + ",lwd=2)\n");
		addLegendItem(seriesTitle, false, true);
		seriesCounter++;
		lineCounter++;
	}

	@Override
	public void addCDFSeries(Collection<? extends Number> values, String seriesTitle) {
		double scale = getScale(LpeNumericUtils.max(values).doubleValue());
		this.xScale = scale;
		updateAxisRanges(LpeNumericUtils.min(values).doubleValue() * scale, LpeNumericUtils.max(values).doubleValue()
				* scale, 0.0, 100.0);
		String unit = getUnit(scale);

		if (xLabel.contains("[")) {
			xLabel = xLabel.substring(0, xLabel.lastIndexOf("["));
			xLabel = xLabel.trim();
		}
		xLabel += " " + unit;

		int size = values.size();
		List<Number> xValues = new ArrayList<>(size);
		List<Number> yValues = new ArrayList<>(size);

		for (Number val : values) {
			xValues.add(val.doubleValue() * scale);
		}

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
		String dataFile = storeCSV(xValues, yValues);
		scriptBuilder.append("data <- read.csv(file=\"" + dataFile + "\",head=TRUE,sep=\";\")\n");
		scriptBuilder.append("xData <- data[[1]]\n");
		scriptBuilder.append("yData <- data[[2]]\n");
		scriptBuilder.append("lines(xData, yData,lty=" + lineCounter + ",lwd=2)\n");
		addLegendItem(seriesTitle, false, true);
		seriesCounter++;
		lineCounter++;
	}

	@Override
	public void addHorizontalLine(double yValue, String seriesTitle) {
		yValue *= yScale;
		updateAxisRanges(xMin, xMax, yValue, yValue);
		String valueStr = "c(" + yValue + "," + yValue + ")";
		scriptBuilder.append("lines(c(xRange), " + valueStr + ",lty=" + lineCounter + ",lwd=2)\n");
		addLegendItem(seriesTitle, false, true);
		seriesCounter++;
		lineCounter++;
	}

	@Override
	public void addVerticalLine(double xValue, String seriesTitle) {
		xValue *= xScale;
		updateAxisRanges(xValue, xValue, yMin, yMax);
		String valueStr = "c(" + xValue + "," + xValue + ")";
		scriptBuilder.append("lines(" + valueStr + ",c(yRange), lty=" + lineCounter + ",lwd=2)\n");
		addLegendItem(seriesTitle, false, true);
		seriesCounter++;
		lineCounter++;
	}

	private String getRangesString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("xRange <- c(");
		strBuilder.append(xMin);
		strBuilder.append(",");
		strBuilder.append(xMax);
		strBuilder.append(")");
		strBuilder.append("\n");
		strBuilder.append("yRange <- c(");
		strBuilder.append(yMin);
		strBuilder.append(",");
		strBuilder.append(yMax);
		strBuilder.append(")");
		strBuilder.append("\n");
		return strBuilder.toString();
	}

	private String getTitlesString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("plotTitle <- \"");
		strBuilder.append(title);
		strBuilder.append("\"");
		strBuilder.append("\n");

		strBuilder.append("xLabel <- \"");
		strBuilder.append(xLabel);
		strBuilder.append("\"");
		strBuilder.append("\n");

		strBuilder.append("yLabel <- \"");
		strBuilder.append(yLabel);
		strBuilder.append("\"");
		strBuilder.append("\n");
		return strBuilder.toString();

	}

	private void extractCopy(String targetFile, String scriptStr) {
		String fileName = targetFile.substring(targetFile.lastIndexOf("/") + 1, targetFile.lastIndexOf("."));
		String targetDir = targetFile.substring(0, targetFile.lastIndexOf("/"));

		targetDir = LpeFileUtils.concatFileName(targetDir, "chartData");
		targetDir = LpeFileUtils.concatFileName(targetDir, fileName);
		targetDir = targetDir.replace(System.getProperty("file.separator"), "/");
		LpeFileUtils.createDir(targetDir);
		copyCSVs(targetDir);

		String originalDir = LpeFileUtils.concatFileName(
				LpeFileUtils.concatFileName(LpeSystemUtils.getSystemTempDir(), DYNAMIC_SPOTTER_DIR), "chartTmp")
				.replace(System.getProperty("file.separator"), "/");
		scriptStr = scriptStr.replace(originalDir, targetDir);

		String scriptFile = LpeFileUtils.concatFileName(targetDir, "script-" + seriesCounter + ".r");
		try {
			FileWriter fWriter = new FileWriter(scriptFile, false);
			fWriter.append(scriptStr);
			fWriter.flush();

			fWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void copyCSVs(String target) {
		LpeFileUtils.createDir(target);

		String dir = LpeFileUtils.concatFileName(LpeSystemUtils.getSystemTempDir(), DYNAMIC_SPOTTER_DIR);
		dir = LpeFileUtils.concatFileName(dir, "chartTmp");
		try {
			LpeFileUtils.copyDirectory(dir, target);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String storeCSV(NumericPairList<? extends Number, ? extends Number> valuePairs) {
		String file = LpeFileUtils.concatFileName(LpeSystemUtils.getSystemTempDir(), DYNAMIC_SPOTTER_DIR);
		file = LpeFileUtils.concatFileName(file, "chartTmp");
		LpeFileUtils.createDir(file);
		file = LpeFileUtils.concatFileName(file, "data-" + seriesCounter + ".csv");
		LpeNumericUtils.exportAsCSV(valuePairs, file, "Col1", "Col2");

		return file.replace(System.getProperty("file.separator"), "/");
	}

	private String storeCSV(List<? extends Number>... data) {
		String file = LpeFileUtils.concatFileName(LpeSystemUtils.getSystemTempDir(), DYNAMIC_SPOTTER_DIR);
		file = LpeFileUtils.concatFileName(file, "chartTmp");
		LpeFileUtils.createDir(file);
		file = LpeFileUtils.concatFileName(file, "data-" + seriesCounter + ".csv");
		LpeNumericUtils.exportAsCSV(file, data);

		return file.replace(System.getProperty("file.separator"), "/");
	}

	private void cleanUpTmpFiles() {
		try {
			LpeFileUtils.removeDir(LpeFileUtils.concatFileName(LpeSystemUtils.getSystemTempDir(), DYNAMIC_SPOTTER_DIR));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addLegendItem(String legendTitle, boolean point, boolean line) {
		if (legendNamesBuilder != null) {
			if (!legendNamesBuilder.toString().endsWith("(")) {
				legendNamesBuilder.append(",");
				legendLinesBuilder.append(",");
				legendPointsBuilder.append(",");
			}
			legendNamesBuilder.append("\"" + legendTitle + "\"");
			if (point) {
				legendPointsBuilder.append(pointCounter);
			} else {
				legendPointsBuilder.append(-1);
			}
			if (line) {
				legendLinesBuilder.append(lineCounter);
			} else {
				legendLinesBuilder.append(-1);
			}
		}
	}

	private void finishLegend() {
		if (legendNamesBuilder != null) {
			legendNamesBuilder.append(")");
			legendPointsBuilder.append(")");
			legendLinesBuilder.append(")");
		}

	}

}
