package org.spotter.core.test.dummies.satellites;

import java.util.Properties;

import org.lpe.common.extension.IExtension;
import org.spotter.core.workload.AbstractWorkloadAdapter;
import org.spotter.exceptions.WorkloadException;

public class DummyWorkload extends AbstractWorkloadAdapter {
	public static int numExperiments = 0;
	public boolean initialized = false;
	public boolean loadStarted = false;
	public boolean warmUpTerminated = false;
	public boolean experimentTerminated = false;

	public DummyWorkload(IExtension<?> provider) {
		super(provider);
	}

	@Override
	public void initialize() throws WorkloadException {
		initialized = true;
		numExperiments = 0;
	}

	@Override
	public void startLoad(Properties config) throws WorkloadException {
		loadStarted = true;
		numExperiments++;
	}

	@Override
	public void waitForWarmupPhaseTermination() throws WorkloadException {
		warmUpTerminated = true;
	}

	@Override
	public void waitForExperimentPhaseTermination() throws WorkloadException {
		experimentTerminated = true;
	}

	@Override
	public void waitForFinishedLoad() throws WorkloadException {
		loadStarted = false;
	}

}
