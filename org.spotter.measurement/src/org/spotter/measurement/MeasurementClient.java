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
package org.spotter.measurement;

import java.io.OutputStream;

import org.aim.api.exceptions.MeasurementException;
import org.aim.api.measurement.MeasurementData;
import org.aim.artifacts.instrumentation.InstrumentationClient;
import org.lpe.common.extension.IExtension;
import org.spotter.core.measurement.AbstractMeasurementController;

/**
 * Generic REST client for the measurement service.
 * 
 * @author Alexander Wert
 * 
 */
public class MeasurementClient extends AbstractMeasurementController {
	private InstrumentationClient agentClient;

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *            extension provider
	 */
	public MeasurementClient(IExtension<?> provider) {
		super(provider);
	}

	@Override
	public void enableMonitoring() throws MeasurementException {

		agentClient.enableMonitoring();
	}

	@Override
	public void disableMonitoring() throws MeasurementException {

		agentClient.disableMonitoring();
	}

	@Override
	public MeasurementData getMeasurementData() throws MeasurementException {

		return agentClient.getMeasurementData();
	}

	@Override
	public long getCurrentTime() {

		return agentClient.getCurrentTime();
	}

	@Override
	public void initialize() throws MeasurementException {
		if (agentClient == null) {
			agentClient = new InstrumentationClient(getHost(), getPort());
			if (!agentClient.testConnection()) {
				throw new MeasurementException("Connection to measurement satellite could not be established!");
			}
		}

	}

	@Override
	public void pipeToOutputStream(OutputStream oStream) throws MeasurementException {

		agentClient.pipeToOutputStream(oStream);

	}



	@Override
	public void storeReport(String path) throws MeasurementException {
		// nothing to do here.
	}

}
