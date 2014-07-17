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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Messaging representation of the measured data.
 * 
 * @author Alexander Wert
 * 
 */
public class ProcessedData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8612019032543363844L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessedData.class);

	private Map<String, Component> components;
	private double totalExperimentDuration = 0;

	private long totalMessagesReceived = 0;

	private long totalMessagesSent = 0;

	/**
	 * Default Constructor.
	 */
	public ProcessedData() {
		components = new HashMap<String, Component>();
	}

	/**
	 * Creates a component with the given id.
	 * 
	 * @param componentId
	 *            id for the new component
	 * @return new component
	 */
	public Component createComponent(String componentId) {
		if (components.containsKey(componentId)) {
			LOGGER.debug("Component '" + componentId + "' already exists.");
			return getComponent(componentId);
		} else {
			Component temp = new Component(this, componentId);
			components.put(componentId, temp);
			return temp;
		}
	}

	/**
	 * Returns the component for the passed id.
	 * 
	 * @param clientId
	 *            id of the client of interest
	 * @return component for the passed id.
	 */
	public Component getComponent(String clientId) {
		if (!components.containsKey(clientId)) {
			return createComponent(clientId);
		}
		return components.get(clientId);
	}

	/**
	 * Returns an array with all existing client ids.
	 * 
	 * @return an array with all existing client ids.
	 */
	public String[] getComponentIds() {
		String[] ids = new String[components.size()];
		int count = 0;
		for (String id : components.keySet()) {
			ids[count++] = id;
		}
		return ids;
	}

	/**
	 * 
	 * @return all participating components
	 */
	public Collection<Component> getComponents() {
		return components.values();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("########################################");
		buffer.append("Total time: " + getTotalExperimentDuration() + "ms");
		buffer.append("Total messages sent: " + getTotalMessagesSent());
		buffer.append("Total messages received: " + getTotalMessagesReceived());
		buffer.append("########################################");

		for (Component component : getComponents()) {
			buffer.append(component.toString());
		}

		return buffer.toString();
	}

	void increaseMessageReceivedCount() {
		totalMessagesReceived++;
	}

	void increaseMessageSentCount() {
		totalMessagesSent++;
	}

	/**
	 * @return the totalDuration
	 */
	public double getTotalExperimentDuration() {
		return totalExperimentDuration;
	}

	/**
	 * @param totalDuration
	 *            the totalDuration to set
	 */
	public void setTotalExperimentDuration(double totalDuration) {
		this.totalExperimentDuration = totalDuration;
	}
	
	public double getTotalMessagingTime(){
		double duration = 0.0;
		for(Component comp : components.values()){
			duration += comp.getTotalMessageSentDuration();
		}
		return duration;
	}

	/**
	 * @return the totalMessagesReceived
	 */
	public long getTotalMessagesReceived() {
		return totalMessagesReceived;
	}

	/**
	 * @param totalMessagesReceived
	 *            the totalMessagesReceived to set
	 */
	public void setTotalMessagesReceived(long totalMessagesReceived) {
		this.totalMessagesReceived = totalMessagesReceived;
	}

	/**
	 * @return the totalMessagesSent
	 */
	public long getTotalMessagesSent() {
		return totalMessagesSent;
	}

	/**
	 * @param totalMessagesSent
	 *            the totalMessagesSent to set
	 */
	public void setTotalMessagesSent(long totalMessagesSent) {
		this.totalMessagesSent = totalMessagesSent;
	}

}
