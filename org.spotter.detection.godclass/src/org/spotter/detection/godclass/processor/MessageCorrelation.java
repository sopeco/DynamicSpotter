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

/**
 * Correlator for send and received messages.
 * 
 * @author Alexander Wert
 * 
 */
public class MessageCorrelation {
	private String senderId;
	private String receiverId;
	private double timeSend;
	private double timeReceived;

	/**
	 * Default Constructor.
	 */
	public MessageCorrelation() {
	}

	/**
	 * Construcotr.
	 * 
	 * @param senderId
	 *            id of the message sender
	 * @param receiverId
	 *            id of the message receiver
	 * @param timeSend
	 *            time when message has been send
	 * @param timeReceived
	 *            time when message has been received
	 */
	public MessageCorrelation(String senderId, String receiverId, double timeSend, double timeReceived) {
		this.setSenderId(senderId);
		this.setReceiverId(receiverId);
		this.setTimeSend(timeSend);
		this.setTimeReceived(timeReceived);
	}

	/**
	 * 
	 * @return time between sending and receiving the message
	 */
	public double getDuration() {
		return getTimeReceived() - getTimeSend();
	}

	/**
	 * @return the senderId
	 */
	public String getSenderId() {
		return senderId;
	}

	/**
	 * @param senderId
	 *            the senderId to set
	 */
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	/**
	 * @return the receiverId
	 */
	public String getReceiverId() {
		return receiverId;
	}

	/**
	 * @param receiverId
	 *            the receiverId to set
	 */
	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}

	/**
	 * @return the timeSend
	 */
	public double getTimeSend() {
		return timeSend;
	}

	/**
	 * @param timeSend
	 *            the timeSend to set
	 */
	public void setTimeSend(double timeSend) {
		this.timeSend = timeSend;
	}

	/**
	 * @return the timeReceived
	 */
	public double getTimeReceived() {
		return timeReceived;
	}

	/**
	 * @param timeReceived
	 *            the timeReceived to set
	 */
	public void setTimeReceived(double timeReceived) {
		this.timeReceived = timeReceived;
	}
}
