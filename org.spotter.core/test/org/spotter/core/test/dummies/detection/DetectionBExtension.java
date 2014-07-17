package org.spotter.core.test.dummies.detection;

import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.detection.IDetectionController;

public class DetectionBExtension extends AbstractDetectionExtension{

	@Override
	public IDetectionController createExtensionArtifact() {
		return new DetectionB(this);
	}

	@Override
	public String getName() {
		return "DetectionB";
	}

	@Override
	protected void initializeConfigurationParameters() {
		
	}

}
