package org.spotter.core.test.dummies.detection;


import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.detection.IDetectionController;

public class MockDetectionExtension extends AbstractDetectionExtension {

	@Override
	public IDetectionController createExtensionArtifact() {
		return new MockDetection(this);
	}

	@Override
	public String getName() {
		return "MockDetection";
	}

	@Override
	protected void initializeConfigurationParameters() {

	}

}
