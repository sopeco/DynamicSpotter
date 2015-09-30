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
package org.spotter.core.workload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.lpe.common.extension.IExtension;
import org.spotter.exceptions.WorkloadException;

/**
 * Brokers all workload adapter to use for Spotter.
 * 
 * @author Alexander Wert
 * 
 */
public final class WorkloadAdapterBroker implements IWorkloadAdapter {

	private static WorkloadAdapterBroker instance;

	/**
	 * 
	 * @return singleton instance
	 */
	public static WorkloadAdapterBroker getInstance() {
		if (instance == null) {
			instance = new WorkloadAdapterBroker();
		}
		return instance;
	}

	private final List<IWorkloadAdapter> wlAdapters;

	/**
	 * Constructor.
	 * 
	 * @param instrumentationControllers
	 *            instrumentation controllers to manage
	 */
	private WorkloadAdapterBroker() {
		this.wlAdapters = new ArrayList<IWorkloadAdapter>();

	}

	/**
	 * Sets instrumentation controllers.
	 * 
	 * @param instrumentationControllers
	 *            instrumentation controllers
	 */
	public void setControllers(final Collection<IWorkloadAdapter> instrumentationControllers) {
		wlAdapters.clear();
		wlAdapters.addAll(instrumentationControllers);
	}

	@Override
	public void initialize() throws WorkloadException {
		for (final IWorkloadAdapter wlAdapter : wlAdapters) {
			wlAdapter.initialize();
		}

	}

	@Override
	public void startLoad(final LoadConfig loadConfig) throws WorkloadException {
		for (final IWorkloadAdapter wlAdapter : wlAdapters) {
			wlAdapter.startLoad(loadConfig);
		}

	}

	@Override
	public void waitForFinishedLoad() throws WorkloadException {
		for (final IWorkloadAdapter wlAdapter : wlAdapters) {
			wlAdapter.waitForFinishedLoad();
		}

	}

	@Override
	public Properties getProperties() {
		final Properties props = new Properties();
		for (final IWorkloadAdapter wlAdapter : wlAdapters) {
			props.putAll(wlAdapter.getProperties());
		}
		return props;
	}

	@Override
	public void waitForWarmupPhaseTermination() throws WorkloadException {
		for (final IWorkloadAdapter wlAdapter : wlAdapters) {
			wlAdapter.waitForWarmupPhaseTermination();
		}

	}

	@Override
	public void waitForExperimentPhaseTermination() throws WorkloadException {
		for (final IWorkloadAdapter wlAdapter : wlAdapters) {
			wlAdapter.waitForExperimentPhaseTermination();
		}

	}

	@Override
	public IExtension getProvider() {
		return null;
	}

	@Override
	public String getName() {
		return "Workload Adapter Broker";
	}

	@Override
	public String getPort() {
		return "NA";
	}

	@Override
	public String getHost() {
		return "localhost";
	}

	@Override
	public void setProperties(final Properties properties) {
		// nothing to do
	}

}
