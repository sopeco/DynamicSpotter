/**
 * Copyright 2014 SAP AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spotter.service.rest.dummy;

import org.lpe.common.extension.IExtension;
import org.spotter.core.workload.AbstractWorkloadAdapter;
import org.spotter.core.workload.LoadConfig;
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
	public void startLoad(LoadConfig loadConfig) throws WorkloadException {
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
