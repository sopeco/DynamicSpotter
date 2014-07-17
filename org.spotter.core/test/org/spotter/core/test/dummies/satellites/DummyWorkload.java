package org.spotter.core.test.dummies.satellites;

import java.util.Properties;

import org.lpe.common.extension.IExtension;
import org.spotter.core.workload.AbstractWorkloadAdapter;
import org.spotter.exceptions.WorkloadException;

public class DummyWorkload extends AbstractWorkloadAdapter{

	public DummyWorkload(IExtension<?> provider) {
		super(provider);
	}

	@Override
	public void initialize() throws WorkloadException {
		
	}

	@Override
	public void startLoad(Properties config) throws WorkloadException {
		
	}

	@Override
	public void waitForWarmupPhaseTermination() throws WorkloadException {
		
	}

	@Override
	public void waitForExperimentPhaseTermination() throws WorkloadException {
		
	}

	@Override
	public void waitForFinishedLoad() throws WorkloadException {
		
	}

}
