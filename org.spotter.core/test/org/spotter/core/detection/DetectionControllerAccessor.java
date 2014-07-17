package org.spotter.core.detection;

import org.spotter.shared.result.model.SpotterResult;

public class DetectionControllerAccessor {
	public static SpotterResult analyzeProblem(IDetectionController detectionController) {
		return ((AbstractDetectionController)detectionController).analyze(null);
	}
}
