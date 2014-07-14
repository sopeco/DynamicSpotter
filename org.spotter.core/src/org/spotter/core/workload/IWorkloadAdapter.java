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

import java.util.Properties;

import org.lpe.common.extension.IExtensionArtifact;
import org.spotter.exceptions.WorkloadException;

/**
 * {@link IWorkloadAdapter} specifies the interface for a PPD workload adapter.
 * 
 * @author Alexander Wert
 * 
 */
public interface IWorkloadAdapter extends IExtensionArtifact {
	/**
	 * Number of users key.
	 */
	String NUMBER_CURRENT_USERS = "org.ppd.workload.numberOfUsers";

	/**
	 * Initializes the workload adapter.
	 * 
	 * @throws WorkloadException
	 *             if initialization fails
	 */
	void initialize() throws WorkloadException;

	/**
	 * Starts the workload generation.
	 * 
	 * @param config
	 *            contains configuration properties for the specific workload
	 *            generator. The workload characteristics are described within
	 *            this configuration as well.
	 * @throws WorkloadException
	 *             if starting load fails
	 */
	void startLoad(Properties config) throws WorkloadException;

	/**
	 * Blocks until warmup phase is finished.
	 * 
	 * @throws WorkloadException
	 *             if waiting is interrupted
	 */
	void waitForWarmupPhaseTermination() throws WorkloadException;

	/**
	 * Blocks until stable experiment phase is finished.
	 * 
	 * @throws WorkloadException
	 *             if waiting is interrupted
	 */
	void waitForExperimentPhaseTermination() throws WorkloadException;

	/**
	 * Waits until workload generation has finished.
	 * 
	 * @throws WorkloadException
	 *             if status cannot be retrieved
	 */
	void waitForFinishedLoad() throws WorkloadException;

	/**
	 * @return the name
	 */
	String getName();

	

	/**
	 * @return the port
	 */
	String getPort();

	

	/**
	 * @return the host
	 */
	String getHost();

	

	/**
	 * @return the properties
	 */
	Properties getProperties();

	/**
	 * @param properties
	 *            the properties to set
	 */
	void setProperties(Properties properties);
	
}
