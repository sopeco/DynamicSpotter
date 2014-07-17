package org.spotter.core.test.dummies.satellites;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.measurement.AbstractMeasurmentExtension;
import org.spotter.core.measurement.IMeasurementController;

public class DummyMeasurementExtension extends AbstractMeasurmentExtension{

	@Override
	public String getName() {
		return "DummyMeasurement";
	}

	@Override
	public IMeasurementController createExtensionArtifact() {
		return new DummyMeasurement(this);
	}

	@Override
	protected void initializeConfigurationParameters() {
		ConfigParameterDescription parDescription = new ConfigParameterDescription("test.measurement.parameter", LpeSupportedTypes.Integer);
		addConfigParameter(parDescription);
		
	}

	@Override
	public boolean testConnection(String host, String port) {
		return true;
	}

	@Override
	public boolean isRemoteExtension() {
		return true;
	}

}
