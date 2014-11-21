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
package org.spotter.client;

import java.io.InputStream;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.web.LpeWebUtils;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.configuration.JobDescription;
import org.spotter.shared.configuration.SpotterExtensionType;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;
import org.spotter.shared.service.SpotterServiceResponse;
import org.spotter.shared.status.SpotterProgress;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

/**
 * Client for resource monitoring.
 * 
 * @author Alexander Wert
 * 
 */
public class SpotterServiceClient {

	private String url;
	private WebResource webResource;
	private Client client;

	/**
	 * Constructor.
	 * 
	 * @param host
	 *            host of the service
	 * @param port
	 *            port where to reach service
	 */
	public SpotterServiceClient(String host, String port) {
		url = "http://" + host + ":" + port;
		client = LpeWebUtils.getWebClient();
		webResource = client.resource(url);
	}

	/**
	 * Updates the URL and creates a new web resource.
	 * 
	 * @param host
	 *            host of the service
	 * @param port
	 *            port where to reach service
	 */
	public void updateUrl(String host, String port) {
		url = "http://" + host + ":" + port;
		webResource = client.resource(url);
	}

	/**
	 * Executes diagnostics process.
	 * 
	 * @param jobDescription
	 *            the job description to use
	 * @return job id for the started diagnosis task
	 */
	public long startDiagnosis(JobDescription jobDescription) {
		SpotterServiceResponse<Long> response = webResource.path(ConfigKeys.SPOTTER_REST_BASE)
				.path(ConfigKeys.SPOTTER_REST_START_DIAG).type(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).post(new GenericType<SpotterServiceResponse<Long>>() {
				}, jobDescription);

		switch (response.getStatus()) {
		case INVALID_STATE:
			throw new IllegalStateException("Spotter is already running");
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error: " + response.getErrorMessage());
		default:
			throw new IllegalStateException("Illegal response state!");
		}

	}

	/**
	 * Requests the results of a the run with the given job id.
	 * 
	 * @param jobId
	 *            the job id of the diagnosis run
	 * @return input stream of the zipped run result folder or <code>null</code>
	 *         if none found
	 */
	public InputStream requestResults(String jobId) {
		ClientResponse response = webResource.path(ConfigKeys.SPOTTER_REST_BASE)
				.path(ConfigKeys.SPOTTER_REST_REQU_RESULTS).type(MediaType.APPLICATION_JSON).accept("application/zip")
				.post(ClientResponse.class, jobId);

		InputStream inputStream = response.getEntityInputStream();
		if (inputStream != null) {
			return inputStream;
		} else {
			throw new RuntimeException("Server error: Received no data");
		}
	}

	/**
	 * Returns whether the Spotter Diagnostics is currently running. If the
	 * method returns <code>false</code> either the last run has finished
	 * successfully or the run had been canceled. For the second case the last
	 * exception thrown during that run can be requested via
	 * {@link #getLastRunException}.
	 * 
	 * @return <code>true</code> if Spotter Diagnostics is currently running,
	 *         <code>false</code> otherwise
	 */
	public synchronized boolean isRunning() {
		SpotterServiceResponse<Boolean> response = webResource.path(ConfigKeys.SPOTTER_REST_BASE)
				.path(ConfigKeys.SPOTTER_REST_IS_RUNNING).accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<SpotterServiceResponse<Boolean>>() {
				});
		switch (response.getStatus()) {
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error: " + response.getErrorMessage());
		case INVALID_STATE:
		default:
			throw new IllegalStateException("Illegal response state!");
		}
	}

	/**
	 * 
	 * @return the exception thrown during the last diagnosis run or
	 *         <code>null</code> if none
	 */
	public synchronized Exception getLastRunException() {
		SpotterServiceResponse<Exception> response = webResource.path(ConfigKeys.SPOTTER_REST_BASE)
				.path(ConfigKeys.SPOTTER_REST_LAST_EXCEPTION).accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<SpotterServiceResponse<Exception>>() {
				});
		switch (response.getStatus()) {
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error: " + response.getErrorMessage());
		case INVALID_STATE:
		default:
			throw new IllegalStateException("Illegal response state!");
		}
	}

	/**
	 * 
	 * @return list of configuration parameter descriptions for Spotter
	 *         configuration.
	 */
	public synchronized Set<ConfigParameterDescription> getConfigurationParameters() {
		SpotterServiceResponse<Set<ConfigParameterDescription>> response = webResource
				.path(ConfigKeys.SPOTTER_REST_BASE).path(ConfigKeys.SPOTTER_REST_CONFIG_PARAMS)
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<SpotterServiceResponse<Set<ConfigParameterDescription>>>() {
				});
		switch (response.getStatus()) {
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error: " + response.getErrorMessage());
		case INVALID_STATE:
		default:
			throw new IllegalStateException("Illegal response state!");
		}
	}

	/**
	 * Returns a list of extension names for the given extension type.
	 * 
	 * @param extType
	 *            extension type of interest
	 * @return list of names
	 */
	public Set<String> getAvailableExtensions(SpotterExtensionType extType) {
		SpotterServiceResponse<Set<String>> response = webResource.path(ConfigKeys.SPOTTER_REST_BASE)
				.path(ConfigKeys.SPOTTER_REST_EXTENSIONS).path(extType.toString()).accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<SpotterServiceResponse<Set<String>>>() {
				});
		switch (response.getStatus()) {
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error: " + response.getErrorMessage());
		case INVALID_STATE:
		default:
			throw new IllegalStateException("Illegal response state!");
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
	public Set<ConfigParameterDescription> getExtensionConfigParamters(String extName) {
		SpotterServiceResponse<Set<ConfigParameterDescription>> response = webResource
				.path(ConfigKeys.SPOTTER_REST_BASE).path(ConfigKeys.SPOTTER_REST_EXTENSION_PARAMETERS).path(extName)
				.accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<SpotterServiceResponse<Set<ConfigParameterDescription>>>() {
				});
		switch (response.getStatus()) {
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error: " + response.getErrorMessage());
		case INVALID_STATE:
		default:
			throw new IllegalStateException("Illegal response state!");
		}
	}

	/**
	 * Returns the default hierarchy.
	 * 
	 * @return the default hierarchy
	 */
	public XPerformanceProblem getDefaultHierarchy() {
		SpotterServiceResponse<XPerformanceProblem> response = webResource.path(ConfigKeys.SPOTTER_REST_BASE)
				.path(ConfigKeys.SPOTTER_REST_DEFAULT_HIERARCHY).accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<SpotterServiceResponse<XPerformanceProblem>>() {
				});
		switch (response.getStatus()) {
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error: " + response.getErrorMessage());
		case INVALID_STATE:
		default:
			throw new IllegalStateException("Illegal response state!");
		}
	}

	/**
	 * Returns a report on the progress of the current job.
	 * 
	 * @return progress report
	 */
	public SpotterProgress getCurrentProgressReport() {
		SpotterServiceResponse<SpotterProgress> response = webResource.path(ConfigKeys.SPOTTER_REST_BASE)
				.path(ConfigKeys.SPOTTER_REST_CURRENT_PROGRESS).accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<SpotterServiceResponse<SpotterProgress>>() {
				});
		switch (response.getStatus()) {
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error: " + response.getErrorMessage());
		case INVALID_STATE:
		default:
			throw new IllegalStateException("Illegal response state!");
		}
	}

	/**
	 * Returns the id of the currently running job.
	 * 
	 * @return id
	 */
	public Long getCurrentJobId() {
		SpotterServiceResponse<Long> response = webResource.path(ConfigKeys.SPOTTER_REST_BASE)
				.path(ConfigKeys.SPOTTER_REST_CURRENT_JOB).accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<SpotterServiceResponse<Long>>() {
				});
		switch (response.getStatus()) {
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error: " + response.getErrorMessage());
		case INVALID_STATE:
		default:
			throw new IllegalStateException("Illegal response state!");
		}
	}

	/**
	 * Returns the root problem of the currently running job.
	 * 
	 * @return the root problem
	 */
	public XPerformanceProblem getCurrentRootProblem() {
		SpotterServiceResponse<XPerformanceProblem> response = webResource.path(ConfigKeys.SPOTTER_REST_BASE)
				.path(ConfigKeys.SPOTTER_REST_CURRENT_ROOT_PROBLEM).accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<SpotterServiceResponse<XPerformanceProblem>>() {
				});
		switch (response.getStatus()) {
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error: " + response.getErrorMessage());
		case INVALID_STATE:
		default:
			throw new IllegalStateException("Illegal response state!");
		}
	}

	@Override
	protected void finalize() throws Throwable {
		client.destroy();
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
		SpotterServiceResponse<Boolean> response = webResource.path(ConfigKeys.SPOTTER_REST_BASE)
				.path(ConfigKeys.SPOTTER_REST_TEST_SATELLITE_CONNECTION).path(extName).path(host).path(port)
				.accept(MediaType.APPLICATION_JSON).get(new GenericType<SpotterServiceResponse<Boolean>>() {
				});
		switch (response.getStatus()) {
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error: " + response.getErrorMessage());
		case INVALID_STATE:
		default:
			throw new IllegalStateException("Illegal response state!");
		}
	}

	/**
	 * Tests connection to the Spotter service.
	 * 
	 * @return true if connection could have been established, otherwise false
	 */
	public boolean testConnection() {
		SpotterServiceResponse<Boolean> response = webResource.path(ConfigKeys.SPOTTER_REST_BASE)
				.path(ConfigKeys.SPOTTER_REST_TEST_CONNECTION).accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<SpotterServiceResponse<Boolean>>() {
				});
		switch (response.getStatus()) {
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error: " + response.getErrorMessage());
		case INVALID_STATE:
		default:
			throw new IllegalStateException("Illegal response state!");
		}
	}

}
