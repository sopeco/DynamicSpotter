package org.spotter.core.test.dummies.detection;

import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.detection.IDetectionController;

public class DetectionDExtension extends AbstractDetectionExtension{

	@Override
	public IDetectionController createExtensionArtifact() {
		return new DetectionD(this);
	}

	@Override
	public String getName() {
		return "DetectionD";
	}

	@Override
	protected void initializeConfigurationParameters() {
		
	}

}
