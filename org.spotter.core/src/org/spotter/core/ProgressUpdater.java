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
package org.spotter.core;

import org.spotter.core.detection.AbstractDetectionController;

/**
 * The ProgressUpdater periodically updates the progress of the detection
 * controller in action.
 * 
 * @author Alexander Wert
 * 
 */
public class ProgressUpdater implements Runnable {

	private static final int SECOND = 1000;
	private volatile boolean run = false;
	private AbstractDetectionController controller;

	@Override
	public void run() {
		run = true;

		while (run) {
			synchronized (this) {
				if (controller != null) {
					controller.updateEstimatedProgress();
				}
			}
			try {
				Thread.sleep(SECOND);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Stops execution of the updater.
	 */
	public synchronized void stop() {
		run = false;
	}

	/**
	 * Sets current detection controller.
	 * 
	 * @param controller
	 *            controller in action
	 */
	public synchronized void setController(AbstractDetectionController controller) {
		this.controller = controller;
	}

}
