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
package org.spotter.dummy;

import java.util.Properties;

import org.lpe.common.extension.IExtension;
import org.spotter.core.workload.AbstractWorkloadAdapter;
import org.spotter.exceptions.WorkloadException;

public class TestLoadDriver extends AbstractWorkloadAdapter {

	public TestLoadDriver(IExtension<?> provider) {
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
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void waitForExperimentPhaseTermination() throws WorkloadException {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void waitForFinishedLoad() throws WorkloadException {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
