package org.spotter.core.test.dummies.detection;

import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.detection.IDetectionController;

public class DetectionEExtension extends AbstractDetectionExtension{

	@Override
	public IDetectionController createExtensionArtifact() {
		return new DetectionE(this);
	}

	@Override
	public String getName() {
		return "DetectionE";
	}

	@Override
	protected void initializeConfigurationParameters() {
		
	}

}
