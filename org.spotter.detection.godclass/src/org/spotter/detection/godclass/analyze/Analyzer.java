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
package org.spotter.detection.godclass.analyze;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.lpe.common.util.LpeNumericUtils;
import org.spotter.detection.godclass.processor.data.Component;
import org.spotter.detection.godclass.processor.data.ProcessedData;
import org.spotter.shared.result.model.SpotterResult;

/**
 * God Class data analyzer.
 * 
 * @author Alexander Wert
 * 
 */
public final class Analyzer {

	private static final int PROBLEMATIC_DISTANCE_FACTOR = 2;

	private static final int CRITICAL_DISTANCE_FACTOR = 3;

	private static final double _100_PERCENT = 100D;

	private static Analyzer analyzer;

	/**
	 * Analyze the given ProcessedData and put the results in the specified
	 * SpotterResult object.
	 * 
	 * @param processData
	 *            Data to analyze
	 * @param result
	 *            Object for storing the results
	 */
	public static void analyzeData(ProcessedData processData, SpotterResult result) {
		if (result == null) {
			throw new NullPointerException("SpotterResult must not be null.");
		}
		if (analyzer == null) {
			analyzer = new Analyzer();
		}
		analyzer.analyze(processData, result);
	}

	private static final DecimalFormat df = new DecimalFormat("0.000");

	private enum Distance {
		DOUBLE, TRIPLE
	}

	private long highestReceiveCount = 0;

	private double mean;
	private double standardDeviation;

	/**
	 * Hide default constructor.
	 */
	private Analyzer() {
	}

	/**
	 * Analyze the given data.
	 * 
	 * @param processData
	 *            processed Data
	 * @param result
	 *            spotter result to add detection results to
	 */
	public void analyze(ProcessedData processData, SpotterResult result) {
		findHighestReceiveCount(processData);
		calculateMean(processData);
		calculateStandardDeviation(processData);

		// oldAnalysis(processData, result);

		newAnalysis(processData, result);
	}

	private void newAnalysis(ProcessedData processData, SpotterResult result) {
		for (Component outer : processData.getComponents()) {
			result.addMessage("Investigated component: " + outer.getId());

			List<Double> pctMsgReceivedList = new ArrayList<Double>();
			for (Component inner : processData.getComponents()) {
				if (inner == outer) {
					continue;
				}
				pctMsgReceivedList.add(getRelativeReceivePct(inner));
			}

			double currentMean = LpeNumericUtils.average(pctMsgReceivedList);
			double standardDeviation = LpeNumericUtils.stdDev(pctMsgReceivedList);

			result.addMessage("Component Pct Messages Sent:   " + df.format(getRelativeReceivePct(outer)) + "$");
			result.addMessage("Current Mean:   " + df.format(currentMean) + "$");
			result.addMessage("Current StdDev: " + df.format(standardDeviation) + "%");
			result.addMessage("Critical Threshold (Mean + 3 * SD): " + df.format(currentMean + 3 * standardDeviation)
					+ "%");

			if (currentMean + 3 * standardDeviation < getRelativeReceivePct(outer)) {
				result.addMessage("Result: As GodClass detected");
				result.setDetected(true);
			} else {
				result.addMessage("Result: not detected");
			}
			result.addMessage("* * * *");
		}
	}

	private void oldAnalysis(ProcessedData processData, SpotterResult result) {
		// Value information
		result.addMessage("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
		result.addMessage("Calculated Values - Pct Messages received relative to the highest value");
		result.addMessage("Messages sent: " + processData.getTotalMessagesSent());
		result.addMessage("* * * *");
		result.addMessage("Mean: " + df.format(mean) + "%");
		result.addMessage("StandardDeviation: " + df.format(standardDeviation) + "%");
		result.addMessage("Problematical Threshold (Mean + 2 * SD): " + df.format(getRange(Distance.DOUBLE)) + "%");
		result.addMessage("Critical Threshold (Mean + 3 * SD): " + df.format(getRange(Distance.TRIPLE)) + "%");
		result.addMessage("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");

		// Print problematical components
		List<Component> problematicalComponents = findProblematicalComponents(processData);
		if (!problematicalComponents.isEmpty()) {
			result.addMessage("Found potential problematical components:");
			for (Component c : problematicalComponents) {
				result.addMessage("Pct.: " + df.format(getRelativeReceivePct(c)) + "% - ID: " + c.getId());
			}
			result.addMessage("");
		}

		// Print critical components
		List<Component> criticalComponents = findCriticalComponents(processData);
		if (!criticalComponents.isEmpty()) {
			result.addMessage("Critical components:");
			result.setDetected(true);
			for (Component c : criticalComponents) {
				result.addMessage("Pct.: " + df.format(getRelativeReceivePct(c)) + "% - ID: " + c.getId());
			}
			result.addMessage("");
		}

		// Print critical components
		result.addMessage("Remaining components:");
		for (Component c : processData.getComponents()) {
			if (criticalComponents.contains(c) || problematicalComponents.contains(c)) {
				continue;
			}
			result.addMessage("Pct.: " + df.format(getRelativeReceivePct(c)) + "% - ID: " + c.getId());
		}

		result.addMessage("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
	}

	/**
	 * Find the highest message received count in the given data.
	 * 
	 * @param data
	 */
	private void findHighestReceiveCount(ProcessedData data) {
		for (Component c : data.getComponents()) {
			if (c.getMessagesReceived() > highestReceiveCount) {
				highestReceiveCount = c.getMessagesReceived();
			}
		}
	}

	private long findHighestReceiveCount(ProcessedData data, Component excluded) {
		long value = 0;
		for (Component c : data.getComponents()) {
			if (c == excluded) {
				continue;
			}
			if (c.getMessagesReceived() > value) {
				value = c.getMessagesReceived();
			}
		}
		return value;
	}

	/**
	 * Calculate the mean value of the relative percentage receive count.
	 * 
	 * @param data
	 */
	private void calculateMean(ProcessedData data) {
		for (Component c : data.getComponents()) {
			mean += getRelativeReceivePct(c);
		}
		mean /= data.getComponents().size();
	}

	/**
	 * Calculate the standard deviation value of the relative percentage receive
	 * count.
	 * 
	 * @param data
	 */
	private void calculateStandardDeviation(ProcessedData data) {
		for (Component c : data.getComponents()) {
			standardDeviation += Math.pow(getRelativeReceivePct(c) - mean, 2);
		}
		standardDeviation = Math.sqrt(standardDeviation / (data.getComponents().size() - 1));
	}

	/**
	 * Calculates relative to the component, which has received the most
	 * messages, the percentage of messages that were received by the specified
	 * component.
	 */
	private double getRelativeReceivePct(Component component) {
		return _100_PERCENT / highestReceiveCount * component.getMessagesReceived();
	}

	private List<Component> findCriticalComponents(ProcessedData data) {
		return findComponents(data, Distance.TRIPLE);
	}

	private List<Component> findProblematicalComponents(ProcessedData data) {
		return findComponents(data, Distance.DOUBLE);
	}

	private List<Component> findComponents(ProcessedData data, Distance distance) {
		List<Component> criticalList = new ArrayList<Component>();
		for (Component c : data.getComponents()) {
			if (inRange(getRelativeReceivePct(c), distance)) {
				criticalList.add(c);
			}
		}
		return criticalList;
	}

	private boolean inRange(double value, Distance distance) {
		switch (distance) {
		case DOUBLE:
			return value >= getRange(Distance.DOUBLE) && value < getRange(Distance.TRIPLE);
		case TRIPLE:
			return value >= getRange(Distance.TRIPLE);
		default:
			throw new IllegalArgumentException(distance + " is not a valid value");
		}
	}

	private double getRange(Distance distance) {
		return mean + ((distance == Distance.TRIPLE) ? CRITICAL_DISTANCE_FACTOR : PROBLEMATIC_DISTANCE_FACTOR)
				* standardDeviation;
	}
}
