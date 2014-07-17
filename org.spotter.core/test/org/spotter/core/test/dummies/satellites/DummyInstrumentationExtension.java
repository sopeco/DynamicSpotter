package org.spotter.core.test.dummies.satellites;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.instrumentation.AbstractInstrumentationExtension;
import org.spotter.core.instrumentation.ISpotterInstrumentation;

public class DummyInstrumentationExtension extends AbstractInstrumentationExtension{

	@Override
	public String getName() {
		return "DummyInstrumentation";
	}

	@Override
	public ISpotterInstrumentation createExtensionArtifact() {
		return new DummyInstrumentation(this);
	}

	@Override
	protected void initializeConfigurationParameters() {
		ConfigParameterDescription parDescription = new ConfigParameterDescription("test.instrumentation.parameter", LpeSupportedTypes.Integer);
		addConfigParameter(parDescription);
		
	}

	@Override
	public boolean testConnection(String host, String port) {
		return true;
	}

	@Override
	public boolean isRemoteExtension() {
		return false;
	}

}
