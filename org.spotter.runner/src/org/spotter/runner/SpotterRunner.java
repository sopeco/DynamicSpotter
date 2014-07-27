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
package org.spotter.runner;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.spotter.core.Spotter;
import org.spotter.exceptions.WorkloadException;

/**
 * Runs Spotter standalone.
 * 
 * @author Alexander Wert
 * 
 */
public final class SpotterRunner {

	/**
	 * Private constructor due to utility class.
	 */
	private SpotterRunner() {
	}

	/**
	 * Runs spotter.
	 * 
	 * @param args
	 *            program arguments should contain the path to the configuration
	 *            file!
	 * @throws WorkloadException
	 *             thrown if a problem with workload generation occurs
	 * @throws MeasurementException
	 *             thrown if a problem with measurement data retrieval occurs
	 * @throws InstrumentationException
	 *             thrown if a problem with instrumentation occurs
	 */
	public static void main(String[] args) throws InstrumentationException, MeasurementException, WorkloadException {
		if (args.length < 1 || args[0].isEmpty()) {
			throw new IllegalArgumentException("Please provide a configuration file as program argument!");
		}

		String configFile = args[0];
		Spotter.getInstance().startDiagnosis(configFile);
	}
}
