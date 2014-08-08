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
package org.spotter.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.extension.ExtensionRegistry;
import org.lpe.common.extension.Extensions;
import org.lpe.common.extension.IExtension;
import org.lpe.common.extension.IExtensionArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.core.AbstractSpotterSatelliteExtension;
import org.spotter.core.Spotter;
import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.instrumentation.AbstractInstrumentationExtension;
import org.spotter.core.measurement.AbstractMeasurmentExtension;
import org.spotter.core.workload.AbstractWorkloadExtension;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.configuration.SpotterExtensionType;
import org.spotter.shared.status.SpotterProgress;

/**
 * Wraps Spotter execution in a service layer.
 * 
 * @author Alexander Wert
 * 
 */
public class SpotterServiceWrapper {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpotterServiceWrapper.class);
	private static SpotterServiceWrapper instance;

	/**
	 * 
	 * @return singleton instance
	 */
	public static synchronized SpotterServiceWrapper getInstance() {
		if (instance == null) {
			instance = new SpotterServiceWrapper();
		}

		return instance;
	}

	private ExecutorService executor = Executors.newFixedThreadPool(1);

	private Future<?> futureObject = null;

	private long currentJob;
	private JobState currentJobState = JobState.FINISHED;

	/**
	 * Executes diagnostics process.
	 * 
	 * @param configurationFile
	 *            path to the configuration file
	 * @return job id for the started diagnosis task, 0 if currently a diagnosis
	 *         job is already running
	 */
	public synchronized long startDiagnosis(final String configurationFile) {
		if (getState().equals(JobState.RUNNING)) {
			return 0;
		}
		final long tempJobId = System.currentTimeMillis();
		currentJob = tempJobId;
		currentJobState = JobState.RUNNING;
		futureObject = executor.submit(new Runnable() {

			@Override
			public void run() {
				try {
					Spotter.getInstance().startDiagnosis(configurationFile, tempJobId);
					currentJobState = JobState.FINISHED;
				} catch (Exception e) {
					LOGGER.error("Diagnosis failed: Error: {}", e);
					currentJobState = JobState.CANCELLED;
					throw new RuntimeException(e);
				} finally {
					currentJob = 0;
				}

			}
		});
		return tempJobId;
	}

	/**
	 * Returns the current state of the last issued job.
	 * 
	 * @return the current state of the last issued job
	 */
	public synchronized JobState getState() {
//		if (futureObject != null && futureObject.isCancelled()) {
//			return JobState.CANCELLED;
//		} else if (futureObject == null || futureObject.isDone()) {
//			return JobState.FINISHED;
//		} else {
//			return JobState.RUNNING;
//		}
		return currentJobState;
	}

	/**
	 * Checks whether a concurrent execution exception has been thrown. If this
	 * is the case, this method throws a ExecutionException.
	 * 
	 * @throws InterruptedException
	 *             if job has been interrupted
	 * @throws ExecutionException
	 *             if concurrent exception occured
	 */
	public void checkForConcurrentExecutionException() throws InterruptedException, ExecutionException {
		futureObject.get();
	}

	/**
	 * Returns a report on the progress of the current job.
	 * 
	 * @return progress report
	 */
	public SpotterProgress getCurrentProgressReport() {
		return Spotter.getInstance().getProgress();
	}

	/**
	 * Returns the id of the currently running job.
	 * 
	 * @return id
	 */
	public long getCurrentJobId() {
		return currentJob;
	}

	/**
	 * 
	 * @return list of configuration parameter descriptions for Spotter
	 *         configuration.
	 */
	public synchronized Set<ConfigParameterDescription> getConfigurationParameters() {
		return ConfigKeys.getSpotterConfigParamters();
	}

	/**
	 * Returns a list of extension names for the given extension type.
	 * 
	 * @param extType
	 *            extension type of interest
	 * @return list of names
	 */
	public Set<String> getAvailableExtensions(SpotterExtensionType extType) {
		Class<? extends IExtension<? extends IExtensionArtifact>> extClass = null;
		switch (extType) {
		case DETECTION_EXTENSION:
			extClass = AbstractDetectionExtension.class;
			break;
		case INSTRUMENTATION_EXTENSION:
			extClass = AbstractInstrumentationExtension.class;
			break;
		case MEASUREMENT_EXTENSION:
			extClass = AbstractMeasurmentExtension.class;
			break;
		case WORKLOAD_EXTENSION:
			extClass = AbstractWorkloadExtension.class;
			break;

		default:
			break;
		}
		Extensions<? extends IExtension<? extends IExtensionArtifact>> extensions = ExtensionRegistry.getSingleton()
				.getExtensions(extClass);
		Set<String> extensionNames = new HashSet<>();
		for (IExtension<? extends IExtensionArtifact> ext : extensions.getList()) {
			extensionNames.add(ext.getName());
		}
		return extensionNames;
	}

	/**
	 * Returns a set of available configuration parameters for the given
	 * extension.
	 * 
	 * @param extName
	 *            name of the extension of interest
	 * @return list of configuration parameters
	 */
	public Set<ConfigParameterDescription> getExtensionConfigParamters(String extName) {
		IExtension<? extends IExtensionArtifact> extension = ExtensionRegistry.getSingleton().getExtension(extName);
		if (extension == null) {
			return Collections.emptySet();
		}
		return extension.getConfigParameters();
	}

	/**
	 * Tests connection to the satellite specified by the given extension name,
	 * host and port. If extension is not a satellite this method returns false!
	 * 
	 * @param extName
	 *            name of the extension to connect to
	 * @param host
	 *            host / ip to connect to
	 * @param port
	 *            port to connect to
	 * @return true if connection could have been established, otherwise false
	 */
	public boolean testConnectionToSattelite(String extName, String host, String port) {
		IExtension<? extends IExtensionArtifact> extension = ExtensionRegistry.getSingleton().getExtension(extName);
		if (extension == null) {
			return false;
		}
		if (extension instanceof AbstractSpotterSatelliteExtension) {
			AbstractSpotterSatelliteExtension satellite = (AbstractSpotterSatelliteExtension) extension;
			return satellite.testConnection(host, port);
		}
		return false;
	}

}
