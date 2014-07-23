package org.spotter.core.test.dummies.detection;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.lpe.common.extension.IExtension;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.SpotterResult;

public class MockDetection extends AbstractDetectionController{

	public MockDetection(IExtension<IDetectionController> provider) {
		super(provider);
	}

	@Override
	public void loadProperties() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException {
		
		
	}

	@Override
	public int getNumOfExperiments() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected SpotterResult analyze(DatasetCollection data) {
		// TODO Auto-generated method stub
		return null;
	}

}
