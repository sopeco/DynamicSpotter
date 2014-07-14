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
package org.spotter.detection.godclass.processor.data;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A component is a participant of the messaging procedure.
 * 
 * @author Alexander Wert
 * 
 */
public class Component implements Serializable {

	private static final double _100_PERCENT = 100D;

	/** */
	private static final long serialVersionUID = 4873965184317389096L;

	private List<String> componentsWhoSentToMe = new ArrayList<String>();

	// LOCAL FIELDS
	private String id;
	private double totalMessageSentDuration;
	private double longestMessageSentDuration;
	private double shortestMessageSentDuration;
	private double averageMessageSentDuration;

	private long messagesReceived = 0;
	private long messagesSent = 0;

	
	private ProcessedData parentContainer;
	private Map<String, Long> sendToCountMap = new HashMap<String, Long>();
	private Map<String, Double> sendToDurationMap = new HashMap<String, Double>();
	private String[] stackTrace;

	Component(ProcessedData parent, String componentId) {
		parentContainer = parent;
		id = componentId;
	}

	/**
	 * Adds id of component which received messages from this component.
	 * 
	 * @param receiverId
	 *            id of the receiver component
	 */
	public void addSendMessageTo(String receiverId) {
		if (sendToCountMap.containsKey(receiverId)) {
			sendToCountMap.put(receiverId, sendToCountMap.get(receiverId) + 1L);
		} else {
			sendToCountMap.put(receiverId, 1L);
		}
	}
	
	/**
	 * Adds id of component which received messages from this component.
	 * 
	 * @param receiverId
	 *            id of the receiver component
	 */
	public void addSendMessageToDuration(String receiverId, double duration) {
		if (sendToDurationMap.containsKey(receiverId)) {
			sendToDurationMap.put(receiverId, sendToDurationMap.get(receiverId) + duration);
		} else {
			sendToDurationMap.put(receiverId, duration);
		}
	}

	/**
	 * 
	 * @return average percentage as target
	 */
	public double getAveragePercentageAsTarget() {
		double sum = 0;
		long cCount = 0;
		for (String componentId : componentsWhoSentToMe) {
			if (componentId.equals(id)) {
				continue;
			}
			cCount++;
			sum += parentContainer.getComponent(componentId).getPercentageSendToComponent(id);
		}
		return sum / cCount;
	}

	/**
	 * Adds a component which sent a message to this component.
	 * 
	 * @param clientId
	 *            id of the component who sent a message to this component
	 */
	public void addComponentWhoSentToMe(String clientId) {
		if (!componentsWhoSentToMe.contains(clientId)) {
			componentsWhoSentToMe.add(clientId);
		}
	}

	/**
	 * Returns number of messages send per millisecond.
	 * 
	 * @return number of messages send per millisecond.
	 */
	public double getMessageSendRate() {
		return messagesSent / parentContainer.getTotalExperimentDuration();
	}

	/**
	 * Returns the percentage of messages which was received by this component.
	 * 
	 * @return the percentage of messages which was received by this component.
	 */
	public double getPercentageMessageReceived() {
		return _100_PERCENT / parentContainer.getTotalMessagesReceived() * messagesReceived;
	}

	/**
	 * Returns the percentage of messages which was sent by this component.
	 * 
	 * @return the percentage of messages which was sent by this component.
	 */
	public double getPercentageMessageSend() {
		return _100_PERCENT / parentContainer.getTotalMessagesSent() * messagesSent;
	}

	/**
	 * 
	 * @param componentId
	 *            id of component of interest
	 * @return percentage of messages send to the given component
	 */
	public double getPercentageSendToComponent(String componentId) {
		if (!sendToCountMap.containsKey(componentId)) {
			return 0;
		}
		return _100_PERCENT / messagesSent * sendToCountMap.get(componentId);
	}

	/**
	 * 
	 * 
	 * @return messages received per millisecond.
	 */
	public double getMessageReceiveRate() {
		return messagesReceived / parentContainer.getTotalExperimentDuration();
	}

	/**
	 * Returns a sorted list with client-ids. Descending order according to the
	 * number of messages sent.
	 * 
	 * @return sorted list of messages sent
	 */
	public List<String> getSortedMessagesSendToList() {
		List<String> returnList = new ArrayList<String>();
		returnList.addAll(sendToCountMap.keySet());
		Collections.sort(returnList, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return (int) (sendToCountMap.get(o2) - sendToCountMap.get(o1));
			}
		});
		return returnList;
	}

	/**
	 * Increase the received message count.
	 */
	public void increaseMessageReceived() {
		messagesReceived++;
		parentContainer.increaseMessageReceivedCount();
	}

	/**
	 * Increase the sent message count.
	 */
	public void increaseMessageSent() {
		messagesSent++;
		parentContainer.increaseMessageSentCount();
	}

	/**
	 * Sets the stackTrace field.
	 * 
	 * @param stackTrace
	 *            string representation of the stacktrace
	 */
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace.split("#");
	}

	@Override
	public String toString() {
		return toString(false);
	}

	/**
	 * Creates a string for the component.
	 * 
	 * @param sendList
	 *            if send list exists
	 * @return string representation of this component
	 */
	public String toString(boolean sendList) {
		StringBuilder builder = new StringBuilder();

		builder.append("ID: ");
		builder.append(id);

		builder.append("\n\tMESSAGES SEND *************************************\n\t\tTotal:\t");
		builder.append(messagesSent);
		builder.append("\n\t\tPct.:\t");
		DecimalFormat dcFormat = new DecimalFormat("#0.00");

		builder.append(dcFormat.format(getPercentageMessageSend()));
		builder.append(" %\n\t\tRate:\t");
		builder.append(getMessageSendRate());
		builder.append(" Msg/ms\n\t\tMESSAGE SEND TO\n");

		if (sendList) {
			for (String key : getSortedMessagesSendToList()) {
				builder.append("\t\t\t");
				builder.append(sendToCountMap.get(key));
				builder.append("|");
				builder.append(dcFormat.format(getPercentageSendToComponent(key)));
				builder.append("% -> ");
				builder.append(key);
				builder.append("\n");
			}
		}

		builder.append("\tMESSAGES RECEIVED *********************************\n\t\tTotal:\t");
		builder.append(messagesReceived);
		builder.append("\n\t\tPct.:\t");
		builder.append(dcFormat.format(getPercentageMessageReceived()));
		builder.append(" %\n\t\tRate:\t");
		builder.append(getMessageReceiveRate());
		builder.append(" Msg/ms\n\t\tAvg target:\t");
		builder.append(dcFormat.format(getAveragePercentageAsTarget()));
		builder.append(" %\n\tSENT MSG DURRATION UNTIL RECEIVE ******************\n\t\tMessages totalTime:\t");
		builder.append(totalMessageSentDuration);
		builder.append("ms\n\t\tMessages avgTime:\t");
		builder.append(averageMessageSentDuration);

		builder.append("ms\n\t\tMessages longest:\t");
		builder.append(longestMessageSentDuration);
		builder.append("ms\n\t\tMessages shortest:\t");
		builder.append(shortestMessageSentDuration);

		builder.append("ms\n\tSTACKTRACE\n");

		for (int i = 0; i < stackTrace.length; i++) {
			for (int x = 0; x < i + 1; x++) {
				builder.append("\t");
			}
			builder.append(stackTrace[stackTrace.length - 1 - i]);
			builder.append("\n");
		}

		return builder.toString();
	}

	/**
	 * @return the componentsWhoSentToMe
	 */
	public List<String> getComponentsWhoSentToMe() {
		return componentsWhoSentToMe;
	}

	/**
	 * @param componentsWhoSentToMe
	 *            the componentsWhoSentToMe to set
	 */
	public void setComponentsWhoSentToMe(List<String> componentsWhoSentToMe) {
		this.componentsWhoSentToMe = componentsWhoSentToMe;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the totalMessageSentDuration
	 */
	public double getTotalMessageSentDuration() {
		return totalMessageSentDuration;
	}

	/**
	 * @param totalMessageSentDuration
	 *            the totalMessageSentDuration to set
	 */
	public void setTotalMessageSentDuration(double totalMessageSentDuration) {
		this.totalMessageSentDuration = totalMessageSentDuration;
	}

	/**
	 * @return the longestMessageSentDuration
	 */
	public double getLongestMessageSentDuration() {
		return longestMessageSentDuration;
	}

	/**
	 * @param longestMessageSentDuration
	 *            the longestMessageSentDuration to set
	 */
	public void setLongestMessageSentDuration(double longestMessageSentDuration) {
		this.longestMessageSentDuration = longestMessageSentDuration;
	}

	/**
	 * @return the shortestMessageSentDuration
	 */
	public double getShortestMessageSentDuration() {
		return shortestMessageSentDuration;
	}

	/**
	 * @param shortestMessageSentDuration
	 *            the shortestMessageSentDuration to set
	 */
	public void setShortestMessageSentDuration(double shortestMessageSentDuration) {
		this.shortestMessageSentDuration = shortestMessageSentDuration;
	}

	/**
	 * @return the averageMessageSentDuration
	 */
	public double getAverageMessageSentDuration() {
		return averageMessageSentDuration;
	}

	/**
	 * @param averageMessageSentDuration
	 *            the averageMessageSentDuration to set
	 */
	public void setAverageMessageSentDuration(double averageMessageSentDuration) {
		this.averageMessageSentDuration = averageMessageSentDuration;
	}

	/**
	 * @return the messagesReceived
	 */
	public long getMessagesReceived() {
		return messagesReceived;
	}

	/**
	 * @param messagesReceived
	 *            the messagesReceived to set
	 */
	public void setMessagesReceived(long messagesReceived) {
		this.messagesReceived = messagesReceived;
	}

	/**
	 * @return the messagesSent
	 */
	public long getMessagesSent() {
		return messagesSent;
	}

	/**
	 * @param messagesSent
	 *            the messagesSent to set
	 */
	public void setMessagesSent(long messagesSent) {
		this.messagesSent = messagesSent;
	}

	/**
	 * @return the sendToCountMap
	 */
	public Map<String, Long> getSendToCountMap() {
		return sendToCountMap;
	}

	/**
	 * @param sendToCountMap
	 *            the sendToCountMap to set
	 */
	public void setSendToCountMap(Map<String, Long> sendToCountMap) {
		this.sendToCountMap = sendToCountMap;
	}

	/**
	 * @return the stackTrace
	 */
	public String[] getStackTrace() {
		return stackTrace;
	}

	/**
	 * @param stackTrace
	 *            the stackTrace to set
	 */
	public void setStackTrace(String[] stackTrace) {
		this.stackTrace = stackTrace;
	}

	/**
	 * @return the sendToDurationMap
	 */
	public Map<String, Double> getSendToDurationMap() {
		return sendToDurationMap;
	}

	/**
	 * @param sendToDurationMap the sendToDurationMap to set
	 */
	public void setSendToDurationMap(Map<String, Double> sendToDurationMap) {
		this.sendToDurationMap = sendToDurationMap;
	}

}
