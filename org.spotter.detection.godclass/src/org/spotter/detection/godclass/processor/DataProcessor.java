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
package org.spotter.detection.godclass.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aim.api.measurement.dataset.Dataset;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.artifacts.records.JmsRecord;
import org.spotter.detection.godclass.processor.data.Component;
import org.spotter.detection.godclass.processor.data.ProcessedData;

/**
 * Processes raw measurement data with respect to messaging.
 * 
 * @author Alexander Wert
 * 
 */
public final class DataProcessor {

	/**
	 * Process the given WrappedMeasurementData.
	 * 
	 * @param data
	 *            Data to process
	 * @return the processed data
	 */
	public static ProcessedData processData(DatasetCollection data) {
		return new DataProcessor().process(data);
	}

	// INFOS
	private double firstTimestamp;
	private double lastTimestamp;
	private Map<String, List<Double>> messageDurations;

	// TEMP
	private Map<String, MessageCorrelation> messageMap;
	private ProcessedData processedData;
	private Map<String, String> tempStackTraceMap;

	/**
	 * Hide default constructor.
	 */
	private DataProcessor() {
	}

	/**
	 * Starts to process the input data.
	 * 
	 * @param data
	 *            data to process
	 * @return transformed data
	 */
	public ProcessedData process(DatasetCollection data) {
		initVariables();

		buildMessageMap(data);
		evaluateMessageMap();
		callcualteMessageDurations();
		assignStackTrace(data);

		return processedData;
	}

	private void addMessageDuration(String clientId, double duration) {
		if (!messageDurations.containsKey(clientId)) {
			messageDurations.put(clientId, new ArrayList<Double>());
		}
		messageDurations.get(clientId).add(duration);
	}

	/**
	 * Assigns to each ComponentInfo the correlating StackTrace
	 */
	private void assignStackTrace(DatasetCollection data) {
		// Assigns a stacktrace to each ComponentInfo
		// for (WrappedDataSet set :
		// data.getDataSets(JmsStartConnectionRecord.class)) {
		// for (JmsStartConnectionRecord jscr :
		// set.getRecords(JmsStartConnectionRecord.class)) {
		// processedData.getComponent(jscr.getClientId()).setStackTrace(jscr.getStackTrace());
		// }
		// }
		for (Entry<String, String> pair : tempStackTraceMap.entrySet()) {
			processedData.getComponent(pair.getKey()).setStackTrace(pair.getValue());
		}
	}

	/**
	 * Builds the messageMap out of the input data. Creates relations between
	 * send and received messages.
	 */
	private void buildMessageMap(DatasetCollection data) {
		for (Dataset set : data.getDataSets()) {
			for (JmsRecord record : set.getRecords(JmsRecord.class)) {

				if (record.getClientId() == null) {
					System.err.println("ClientId is null..");
					continue;
				}

				MessageCorrelation correlation = getMessageCorrelation(record.getMessageCorrelationHash());

				if (record.wasSent() == 0) {
					correlation.setReceiverId(record.getClientId());
					correlation.setTimeReceived(record.getTimeStamp());
				} else if (record.wasSent() == 1) {
					correlation.setSenderId(record.getClientId());
					correlation.setTimeSend(record.getTimeStamp());
				} else {
					throw new RuntimeException("'wasSent' must be 0 or 1 and NOT " + record.wasSent());
				}

				if (record.getStackTrace() != null && !record.getStackTrace().isEmpty()) {
					// TODO client id can have multiple stacktraces because of
					// multiple method instrumentations
					tempStackTraceMap.put(record.getClientId(), record.getStackTrace());
				}
			}
		}
	}

	/**
	 * Aggregates the durations of all messages.
	 */
	private void callcualteMessageDurations() {
		for (Component component : processedData.getComponents()) {
			List<Double> list = messageDurations.get(component.getId());

			double shortest = Double.MAX_VALUE;
			double longest = 0;
			double average = 0;
			double total = 0;

			for (double duration : list) {
				if (duration < shortest) {
					shortest = duration;
				}
				if (duration > longest) {
					longest = duration;
				}
				total += duration;
			}
			average = total / list.size();

			component.setShortestMessageSentDuration(shortest);
			component.setLongestMessageSentDuration(longest);
			component.setAverageMessageSentDuration(average);
			component.setTotalMessageSentDuration(total);
		}
	}

	/**
	 * Evaluates the messageMap and builds the componentMap during this action.
	 */
	private void evaluateMessageMap() {
		for (MessageCorrelation mc : messageMap.values()) {
			// Check clientIds
			if (mc.getSenderId() == null || mc.getReceiverId() == null) {
				System.err.println("Nullpointer!");
				continue;
			}

			// Get relevant Component objects
			Component senderInfo = processedData.getComponent(mc.getSenderId());
			Component receiverInfo = processedData.getComponent(mc.getReceiverId());

			// Increase their message count
			senderInfo.increaseMessageSent();
			receiverInfo.increaseMessageReceived();

			// Increase the messageCount to the specified receiver
			senderInfo.addSendMessageTo(receiverInfo.getId());
			senderInfo.addSendMessageToDuration(receiverInfo.getId(), mc.getDuration());

			// Add message duration to sending component
			addMessageDuration(senderInfo.getId(), mc.getDuration());

			// Remember sender
			receiverInfo.addComponentWhoSentToMe(senderInfo.getId());

			// Update timestamps if necessary
			if (mc.getTimeSend() < firstTimestamp) {
				firstTimestamp = mc.getTimeSend();
			}
			if (mc.getTimeReceived() > lastTimestamp) {
				lastTimestamp = mc.getTimeReceived();
			}
		}

		processedData.setTotalExperimentDuration(lastTimestamp - firstTimestamp);
	}

	/**
	 * Returns the MessageCorrelation object of the messageMap which is related
	 * to the given key. If no object is stored with the given key, it will be
	 * created.
	 * 
	 * @param key
	 * @return
	 */
	private MessageCorrelation getMessageCorrelation(String key) {
		if (messageMap.containsKey(key)) {
			return messageMap.get(key);
		} else {
			MessageCorrelation correlation = new MessageCorrelation();
			messageMap.put(key, correlation);
			return correlation;
		}
	}

	/**
	 * Initializes all local variables.
	 */
	private void initVariables() {
		messageMap = new HashMap<String, MessageCorrelation>();
		processedData = new ProcessedData();
		messageDurations = new HashMap<String, List<Double>>();
		tempStackTraceMap = new HashMap<String, String>();

		firstTimestamp = Double.MAX_VALUE;
		lastTimestamp = 0;
	}
}
