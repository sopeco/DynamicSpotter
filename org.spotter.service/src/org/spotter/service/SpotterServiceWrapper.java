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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBException;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.extension.ExtensionRegistry;
import org.lpe.common.extension.Extensions;
import org.lpe.common.extension.IExtension;
import org.lpe.common.extension.IExtensionArtifact;
import org.lpe.common.util.LpeFileUtils;
import org.lpe.common.util.LpeStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.core.AbstractSpotterSatelliteExtension;
import org.spotter.core.Spotter;
import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.instrumentation.AbstractInstrumentationExtension;
import org.spotter.core.measurement.AbstractMeasurmentExtension;
import org.spotter.core.workload.AbstractWorkloadExtension;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.configuration.FileManager;
import org.spotter.shared.configuration.JobDescription;
import org.spotter.shared.configuration.SpotterExtensionType;
import org.spotter.shared.hierarchy.model.RawHierarchyFactory;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;
import org.spotter.shared.result.ResultsLocationConstants;
import org.spotter.shared.status.SpotterProgress;

/**
 * Wraps Spotter execution in a service layer.
 * 
 * @author Alexander Wert, Denis Knoepfle
 * 
 */
public class SpotterServiceWrapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpotterServiceWrapper.class);

	/**
	 * The path to the current working directory.
	 */
	private static final String WORKING_DIR = System.getProperty("user.dir").replace('\\', '/');

	/**
	 * The name of the folder where to put the configuration and results of
	 * diagnosis runs.
	 */
	private static final String RUNTIME_FOLDER = "runtime-diagnosis";

	private static final String ZIP_FILE_EXTENSION = ".zip";

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
	 * @param jobDescription
	 *            job description object containing the whole DS setup such as
	 *            config values, environment and hierarchy configuration
	 * @return job id for the started diagnosis task, 0 if currently a diagnosis
	 *         job is already running
	 */
	public synchronized long startDiagnosis(final JobDescription jobDescription) {
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
					String configurationFile = createDynamicSpotterConfiguration(tempJobId, jobDescription);
					Spotter.getInstance().startDiagnosis(configurationFile, tempJobId);
					currentJobState = JobState.FINISHED;
				} catch (Throwable e) {
					LOGGER.error("Diagnosis failed!", e);
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
	 * Requests the results of a the run with the given job id.
	 * 
	 * @param jobId
	 *            the job id of the diagnosis run
	 * @return output streaming the zipped run result folder or
	 *         <code>null</code> if none found
	 */
	public StreamingOutput requestResults(String jobId) {
		String location = getRuntimeLocation() + "/" + jobId;
		File dir = new File(location);
		if (dir.isDirectory()) {
			final String resultsFolder = location + "/" + FileManager.DEFAULT_RESULTS_DIR_NAME;

			StreamingOutput output = new StreamingOutput() {
				@Override
				public void write(OutputStream os) {
					pipeRunFolderToOutputStream(resultsFolder, os);
				}
			};
			return output;

		}
		return null;
	}

	/**
	 * Returns the current state of the last issued job.
	 * 
	 * @return the current state of the last issued job
	 */
	public synchronized JobState getState() {
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
	 * Returns the root problem of the currently running job.
	 * 
	 * @return the root problem
	 */
	public XPerformanceProblem getCurrentRootProblem() {
		return Spotter.getInstance().getCurrentRootProblem();
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
	 * Returns the default hierarchy.
	 * 
	 * @return default hierarchy
	 */
	public XPerformanceProblem getDefaultHierarchy() {
		return RawHierarchyFactory.getInstance().createProblemHierarchyRoot();
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

	/**
	 * Creates necessary configuration files for the diagnosis from the given
	 * job description.
	 * 
	 * @param jobId
	 *            the job id of the diagnosis
	 * @param jobDescription
	 *            the job description which holds the configuration information
	 * @return the path to the DS configuration file which is required by DS
	 */
	private String createDynamicSpotterConfiguration(long jobId, JobDescription jobDescription) {
		FileManager fileManager = FileManager.getInstance();
		String location = getRuntimeLocation() + "/" + jobId;
		LpeFileUtils.createDir(location);
		String configurationFile = null;

		try {
			fileManager.writeEnvironmentConfig(location, jobDescription.getMeasurementEnvironment());
			fileManager.writeHierarchyConfig(location, jobDescription.getHierarchy());
			configurationFile = fileManager.writeSpotterConfig(location, jobDescription.getDynamicSpotterConfig());
			LOGGER.info("Storing configuration for diagnosis run #" + jobId + " in " + location);
		} catch (IOException | JAXBException e) {
			String message = "Failed to create DS configuration.";
			LOGGER.error(message, e);
			throw new RuntimeException(message, e);
		}

		return configurationFile;
	}

	private static String getRuntimeLocation() {
		return SpotterServiceWrapper.WORKING_DIR + "/" + SpotterServiceWrapper.RUNTIME_FOLDER;
	}

	private void pipeRunFolderToOutputStream(String resultsDirLocation, OutputStream os) {
		String location = resultsDirLocation;
		File resultsDir = new File(location);

		if (resultsDir.isDirectory()) {
			File[] subdirs = resultsDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});
			if (subdirs.length == 1) {
				location += "/" + subdirs[0].getName();

				// pipe zipped run result folder
				FileInputStream fileInputStream = null;
				try {
					fileInputStream = getZippedRunFolder(location);
					LpeStreamUtils.pipe(fileInputStream, os);
				} catch (IOException e) {
					LOGGER.error("Error while streaming results data.", e);
					throw new RuntimeException(e);
				} finally {
					try {
						if (fileInputStream != null) {
							fileInputStream.close();
						}
					} catch (IOException e) {
						LOGGER.warn("Error while closing stream '{}'.", location);
					}
				}

			} else {
				LOGGER.warn("Expected to find only one results folder, but found " + subdirs.length
						+ ", streaming nothing!");
			}
		}
	}

	private FileInputStream getZippedRunFolder(String path) throws FileNotFoundException {
		String zipFileName = path + ZIP_FILE_EXTENSION;
		File zipFile = new File(zipFileName);
		File source = new File(path);

		if (!zipFile.exists()) {
			LOGGER.debug("Packing main results data from '{}' ...", path);

			FileFilter fileFilter = new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					if (pathname.isFile()) {
						return true;
					}

					if (pathname.isDirectory() && !pathname.getName().equals(ResultsLocationConstants.CSV_SUB_DIR)) {
						return true;
					}

					return false;
				}
			};

			LpeFileUtils.zip(source, zipFile, fileFilter);
			LOGGER.debug("Results data packed!");
		}

		FileInputStream fis = new FileInputStream(zipFileName);

		return fis;
	}

}
