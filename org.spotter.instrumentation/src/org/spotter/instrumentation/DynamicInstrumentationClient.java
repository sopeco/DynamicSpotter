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
package org.spotter.instrumentation;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.instrumentation.description.InstrumentationDescription;
import org.aim.artifacts.instrumentation.InstrumentationClient;
import org.lpe.common.extension.IExtension;
import org.spotter.core.instrumentation.AbstractSpotterInstrumentation;

/**
 * Client for usage of dynamic instrumentation agent.
 * 
 * @author Alexander Wert
 * 
 */
public class DynamicInstrumentationClient extends AbstractSpotterInstrumentation {

	private InstrumentationClient client;

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *            extension provider
	 */
	public DynamicInstrumentationClient(IExtension<?> provider) {
		super(provider);
	}

	@Override
	public void initialize() throws InstrumentationException {
		if (client == null) {
			client = new InstrumentationClient(getHost(), getPort());
			if (!client.testConnection()) {
				throw new InstrumentationException("Connection to instrumentation could not be established!");
			}
		}
	}

	@Override
	public void instrument(InstrumentationDescription description) throws InstrumentationException {
		if (client == null) {
			initialize();
		}
		client.instrument(description);

	}

	@Override
	public void uninstrument() throws InstrumentationException {

		if (client == null) {
			initialize();
		}
		client.uninstrument();

	}







}
