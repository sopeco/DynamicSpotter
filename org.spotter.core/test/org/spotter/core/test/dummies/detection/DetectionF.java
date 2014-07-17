package org.spotter.core.test.dummies.detection;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.lpe.common.extension.IExtension;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.SpotterResult;

public class DetectionF extends AbstractDetectionController {

	public DetectionF(IExtension<IDetectionController> provider) {
		super(provider);
	}

	@Override
	public void loadProperties() {

	}

	@Override
	protected void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException {
		

	}

	@Override
	protected int getNumOfExperiments() {
		return 1;
	}

	@Override
	protected SpotterResult analyze(DatasetCollection data) {
		SpotterResult result = new SpotterResult();
		result.setDetected(false);
		return result;
	}

}