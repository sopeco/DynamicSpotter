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
package org.spotter.core.test.dummies.satellites;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.description.InstrumentationDescription;
import org.lpe.common.extension.IExtension;
import org.spotter.core.instrumentation.AbstractSpotterInstrumentation;

public class DummyInstrumentation extends AbstractSpotterInstrumentation {

	public boolean initialized = false;
	public boolean instrumented = false;
	
	public DummyInstrumentation(IExtension<?> provider) {
		super(provider);
	}

	@Override
	public void initialize() throws InstrumentationException {
		initialized = true;

	}

	@Override
	public void instrument(InstrumentationDescription description) throws InstrumentationException {
		instrumented = true;

	}

	@Override
	public void uninstrument() throws InstrumentationException {
		instrumented = false;

	}

}
