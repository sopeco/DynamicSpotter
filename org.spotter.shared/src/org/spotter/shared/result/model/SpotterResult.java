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
package org.spotter.shared.result.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link SpotterResult} represents the diagnosis result.
 * 
 * @author Alexander Wert
 * 
 */
public class SpotterResult implements Serializable {

	private static final long serialVersionUID = 6058864897164235148L;

	private final StringBuilder message = new StringBuilder();
	private final List<String> resourceFiles = new ArrayList<>();
	private final List<CallTree<StackTraceElement>> callTrees = new ArrayList<>();

	private boolean detected = false;

	/**
	 * 
	 * @return true if the problem (to which that result belongs) has been
	 *         detected, otherwise false
	 */
	public boolean isDetected() {
		return detected;
	}

	/**
	 * Sets whether the the problem (to which that result belongs) has been
	 * detected or not.
	 * 
	 * @param detected
	 *            needs to be true if has been detected
	 */
	public void setDetected(boolean detected) {
		this.detected = detected;
	}

	/**
	 * 
	 * @return the diagnosis message
	 */
	public String getMessage() {
		return message.toString();
	}

	/**
	 * adds a message to the diagnosis text.
	 * 
	 * @param msg
	 *            message to be added
	 */
	public void addMessage(String msg) {
		message.append("   # ");
		message.append(msg);
		message.append(System.getProperty("line.separator"));
	}

	/**
	 * Adds the path to an additional resource.
	 * 
	 * @param pathToFile
	 *            path to file
	 */
	public void addResourceFile(String pathToFile) {
		getResourceFiles().add(pathToFile);
	}

	/**
	 * @return the resourceFiles
	 */
	public List<String> getResourceFiles() {
		return resourceFiles;
	}

	/**
	 * Adds the call tree.
	 * 
	 * @param callTree
	 *            call tree to add
	 */
	public void addStackTrace(CallTree<StackTraceElement> callTree) {
		getCallTrees().add(callTree);
	}

	/**
	 * @return the call trees
	 */
	public List<CallTree<StackTraceElement>> getCallTrees() {
		return callTrees;
	}

}
