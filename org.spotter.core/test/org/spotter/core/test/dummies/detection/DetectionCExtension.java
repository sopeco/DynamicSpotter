package org.spotter.core.test.dummies.detection;

import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.detection.IDetectionController;

public class DetectionCExtension extends AbstractDetectionExtension{

	@Override
	public IDetectionController createExtensionArtifact() {
		return new DetectionC(this);
	}

	@Override
	public String getName() {
		return "DetectionC";
	}

	@Override
	protected void initializeConfigurationParameters() {
		
	}

}
