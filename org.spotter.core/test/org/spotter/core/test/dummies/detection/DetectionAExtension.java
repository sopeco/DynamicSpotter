package org.spotter.core.test.dummies.detection;

import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.detection.IDetectionController;

public class DetectionAExtension extends AbstractDetectionExtension{

	@Override
	public IDetectionController createExtensionArtifact() {
		return new DetectionA(this);
	}

	@Override
	public String getName() {
		return "DetectionA";
	}

	@Override
	protected void initializeConfigurationParameters() {
		
	}

}
