package org.spotter.core.test.dummies.detection;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.instrumentation.description.InstrumentationDescriptionBuilder;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.lpe.common.extension.IExtension;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;
import org.spotter.core.test.dummies.satellites.DummyMeasurement;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.SpotterResult;

public class MockDetection extends AbstractDetectionController{

	public static final int NUM_EXPERIMENTS = 5;
	
	public MockDetection(IExtension<IDetectionController> provider) {
		super(provider);
	}

	@Override
	public void loadProperties() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException {
		executeDefaultExperimentSeries(this.getClass(), NUM_EXPERIMENTS, new InstrumentationDescriptionBuilder().build());
	}

	@Override
	public int getNumOfExperiments() {
		return NUM_EXPERIMENTS;
	}

	@Override
	protected SpotterResult analyze(DatasetCollection data) {
		SpotterResult result = new SpotterResult();
		result.setDetected(data.getRecords().size()==NUM_EXPERIMENTS*DummyMeasurement.NUM_RECORDS);
		
		return result;
	}

}
