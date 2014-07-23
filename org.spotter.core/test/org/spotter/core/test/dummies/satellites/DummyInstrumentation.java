package org.spotter.core.test.dummies.satellites;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.instrumentation.description.InstrumentationDescription;
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
