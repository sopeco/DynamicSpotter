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
package org.spotter.service.rest;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.lpe.common.config.ConfigParameterDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.service.SpotterServiceWrapper;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.configuration.JobDescription;
import org.spotter.shared.configuration.SpotterExtensionType;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;
import org.spotter.shared.service.ResponseStatus;
import org.spotter.shared.service.SpotterServiceResponse;
import org.spotter.shared.status.SpotterProgress;

import com.sun.jersey.spi.resource.Singleton;

/**
 * The service interface for Dynamic Spotter.
 * 
 * @author Alexander Wert
 */
@Path(ConfigKeys.SPOTTER_REST_BASE)
@Singleton
public class SpotterService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpotterService.class);

	/**
	 * Starts Dynamic Spotter diagnosis.
	 * 
	 * @param jobDescription
	 *            job description object containing the whole DS setup such as
	 *            config values, environment and hierarchy configuration
	 * @throws IOException
	 *             thrown if experiment fails
	 * @return job id, 0 if already running
	 */
	@POST
	@Path(ConfigKeys.SPOTTER_REST_START_DIAG)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public SpotterServiceResponse<Long> startDiagnosis(JobDescription jobDescription) throws IOException {
		try {
			long jobId = SpotterServiceWrapper.getInstance().startDiagnosis(jobDescription);
			if (jobId == 0) {
				return new SpotterServiceResponse<Long>(jobId, ResponseStatus.INVALID_STATE);
			} else {
				return new SpotterServiceResponse<Long>(jobId, ResponseStatus.OK);
			}
		} catch (Exception e) {
			return createErrorResponse(e);
		}
	}

	/**
	 * Retrieves results from a Dynamic Spotter diagnosis that matches the given
	 * job id.
	 * 
	 * @param jobId
	 *            the job id matching to the diagnosis run to fetch the results
	 *            of
	 * @return the results container for the given id or <code>null</code> if
	 *         for the id no results exist
	 */
	@POST
	@Path(ConfigKeys.SPOTTER_REST_REQU_RESULTS)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("application/zip")
	public StreamingOutput requestResults(String jobId) {
		if (jobId == null) {
			return null;
		}
		try {
			return SpotterServiceWrapper.getInstance().requestResults(jobId);
		} catch (Exception e) {
			LOGGER.error("Server error: " + e);
		}
		return null;
	}

	/**
	 * 
	 * @return true if spotter is currently running diagnosis
	 */
	@GET
	@Path(ConfigKeys.SPOTTER_REST_IS_RUNNING)
	@Produces(MediaType.APPLICATION_JSON)
	public SpotterServiceResponse<Boolean> isRunning() {
		try {
			Boolean isRunning = false;
			switch (SpotterServiceWrapper.getInstance().getState()) {
			case CANCELLED:
			case FINISHED:
				isRunning = false;
				break;
			case RUNNING:
				isRunning = true;
				break;
			default:
				isRunning = false;
				break;
			}

			return new SpotterServiceResponse<Boolean>(isRunning, ResponseStatus.OK);
		} catch (Exception e) {
			return createErrorResponse(e);
		}
	}

	/**
	 * 
	 * @return the exception thrown during the last diagnosis run or
	 *         <code>null</code> if none
	 */
	@GET
	@Path(ConfigKeys.SPOTTER_REST_LAST_EXCEPTION)
	@Produces(MediaType.APPLICATION_JSON)
	public SpotterServiceResponse<Exception> getLastRunException() {
		try {
			Exception lastRunException = null;
			switch (SpotterServiceWrapper.getInstance().getState()) {
			case CANCELLED:
				try {
					SpotterServiceWrapper.getInstance().checkForConcurrentExecutionException();
				} catch (InterruptedException | ExecutionException e) {
					lastRunException = e;
				}
				break;
			default:
				break;
			}
			return new SpotterServiceResponse<>(lastRunException, ResponseStatus.OK);
		} catch (Exception e) {
			return createErrorResponse(e);
		}
	}

	/**
	 * 
	 * @return a set of configuration parameters for Dynamic Spotter.
	 */
	@GET
	@Path(ConfigKeys.SPOTTER_REST_CONFIG_PARAMS)
	@Produces(MediaType.APPLICATION_JSON)
	public SpotterServiceResponse<Set<ConfigParameterDescription>> getConfigurationParameters() {
		try {
			Set<ConfigParameterDescription> set = SpotterServiceWrapper.getInstance().getConfigurationParameters();
			return new SpotterServiceResponse<Set<ConfigParameterDescription>>(set, ResponseStatus.OK);
		} catch (Exception e) {
			return createErrorResponse(e);
		}
	}

	/**
	 * Returns a set of names of available extensions for the given type.
	 * 
	 * @param extType
	 *            extension type
	 * @return a set of extension names
	 */
	@GET
	@Path(ConfigKeys.SPOTTER_REST_EXTENSIONS + "/{extType}")
	@Produces(MediaType.APPLICATION_JSON)
	public SpotterServiceResponse<Set<String>> getAvailableExtensions(@PathParam("extType") String extType) {
		try {
			SpotterExtensionType type = SpotterExtensionType.valueOf(extType);
			Set<String> set = SpotterServiceWrapper.getInstance().getAvailableExtensions(type);
			return new SpotterServiceResponse<Set<String>>(set, ResponseStatus.OK);
		} catch (Exception e) {
			return createErrorResponse(e);
		}
	}

	/**
	 * Returns a set of available configuration parameters for the given
	 * extension.
	 * 
	 * @param extName
	 *            name of the extension of interest
	 * @return list of configuration parameters
	 */
	@GET
	@Path(ConfigKeys.SPOTTER_REST_EXTENSION_PARAMETERS + "/{extName}")
	@Produces(MediaType.APPLICATION_JSON)
	public SpotterServiceResponse<Set<ConfigParameterDescription>> getExtensionConfigParamters(
			@PathParam("extName") String extName) {

		try {
			Set<ConfigParameterDescription> set = SpotterServiceWrapper.getInstance().getExtensionConfigParamters(
					extName);
			return new SpotterServiceResponse<Set<ConfigParameterDescription>>(set, ResponseStatus.OK);
		} catch (Exception e) {
			return createErrorResponse(e);
		}
	}

	/**
	 * Returns the default hierarchy.
	 * 
	 * @return default hierarchy
	 */
	@GET
	@Path(ConfigKeys.SPOTTER_REST_DEFAULT_HIERARCHY)
	@Produces(MediaType.APPLICATION_JSON)
	public SpotterServiceResponse<XPerformanceProblem> getDefaultHierarchy() {
		try {
			XPerformanceProblem root = SpotterServiceWrapper.getInstance().getDefaultHierarchy();
			return new SpotterServiceResponse<XPerformanceProblem>(root, ResponseStatus.OK);
		} catch (Exception e) {
			return createErrorResponse(e);
		}
	}

	/**
	 * Returns a report on the progress of the current job.
	 * 
	 * @return progress report
	 */
	@GET
	@Path(ConfigKeys.SPOTTER_REST_CURRENT_PROGRESS)
	@Produces(MediaType.APPLICATION_JSON)
	public SpotterServiceResponse<SpotterProgress> getCurrentProgressReport() {
		try {
			SpotterProgress progress = SpotterServiceWrapper.getInstance().getCurrentProgressReport();
			return new SpotterServiceResponse<SpotterProgress>(progress, ResponseStatus.OK);
		} catch (Exception e) {
			return createErrorResponse(e);
		}
	}

	/**
	 * Returns the id of the currently running job.
	 * 
	 * @return id
	 */
	@GET
	@Path(ConfigKeys.SPOTTER_REST_CURRENT_JOB)
	@Produces(MediaType.APPLICATION_JSON)
	public SpotterServiceResponse<Long> getCurrentJobId() {
		try {
			Long id = SpotterServiceWrapper.getInstance().getCurrentJobId();
			return new SpotterServiceResponse<Long>(id, ResponseStatus.OK);
		} catch (Exception e) {
			return createErrorResponse(e);
		}
	}

	/**
	 * Returns the root problem of the currently running job.
	 * 
	 * @return the root problem
	 */
	@GET
	@Path(ConfigKeys.SPOTTER_REST_CURRENT_ROOT_PROBLEM)
	@Produces(MediaType.APPLICATION_JSON)
	public SpotterServiceResponse<XPerformanceProblem> getCurrentRootProblem() {
		try {
			XPerformanceProblem rootProblem = SpotterServiceWrapper.getInstance().getCurrentRootProblem();
			return new SpotterServiceResponse<XPerformanceProblem>(rootProblem, ResponseStatus.OK);
		} catch (Exception e) {
			return createErrorResponse(e);
		}
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
	@GET
	@Path(ConfigKeys.SPOTTER_REST_TEST_SATELLITE_CONNECTION + "/" + "{extName}" + "/" + "{host}" + "/" + "{port}")
	@Produces(MediaType.APPLICATION_JSON)
	public SpotterServiceResponse<Boolean> testConnectionToSattelite(@PathParam(value = "extName") String extName,
			@PathParam(value = "host") String host, @PathParam(value = "port") String port) {
		try {
			Boolean connected = SpotterServiceWrapper.getInstance().testConnectionToSattelite(extName, host, port);
			return new SpotterServiceResponse<Boolean>(connected, ResponseStatus.OK);
		} catch (Exception e) {
			return createErrorResponse(e);
		}
	}

	/**
	 * Tests connection to the Spotter service.
	 * 
	 * @return true if connection could have been established, otherwise false
	 */
	@GET
	@Path(ConfigKeys.SPOTTER_REST_TEST_CONNECTION)
	@Produces(MediaType.APPLICATION_JSON)
	public SpotterServiceResponse<Boolean> testConnection() {
		return new SpotterServiceResponse<Boolean>(true, ResponseStatus.OK);
	}

	private <T> SpotterServiceResponse<T> createErrorResponse(Exception e) {
		SpotterServiceResponse<T> response = new SpotterServiceResponse<T>(null, ResponseStatus.SERVER_ERROR);
		LOGGER.error("Server error: " + e);
		if (e.getMessage() == null) {
			response.setErrorMessage(e.getClass().getSimpleName());
		} else {
			response.setErrorMessage(e.getLocalizedMessage());
		}
		return response;
	}

}
