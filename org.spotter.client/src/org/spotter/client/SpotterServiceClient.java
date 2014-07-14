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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.web.LpeWebUtils;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.configuration.SpotterExtensionType;
import org.spotter.shared.service.SpotterServiceResponse;
import org.spotter.shared.status.SpotterProgress;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

/**
 * Client for resource monitoring.
 * 
 * @author Alexander Wert
 * 
 */
public class SpotterServiceClient {
	private static final String CHARSET = "UTF8";
	
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
	 * @param configurationFile
	 *            path to the configuration file
	 * @return job id for the started diagnosis task,
	 */
	public long startDiagnosis(String configurationFile) {
		SpotterServiceResponse<Long> response = webResource
				.path(ConfigKeys.SPOTTER_REST_BASE)
				.path(ConfigKeys.SPOTTER_REST_START_DIAG)
				.type(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.post(new GenericType<SpotterServiceResponse<Long>>() {}, configurationFile);

		switch (response.getStatus()) {
		case INVALID_STATE:
			throw new IllegalStateException("Spotter is already running");
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error!: " + response.getErrorMessage());
		default:
			throw new IllegalStateException("Illegal response state!");
		}

	}

	/**
	 * @return true if Spotter Diagnostics is currently running
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
			throw new RuntimeException("Server error!: " + response.getErrorMessage());
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
			throw new RuntimeException("Server error!: " + response.getErrorMessage());
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
			throw new RuntimeException("Server error!: " + response.getErrorMessage());
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
				.path(ConfigKeys.SPOTTER_REST_BASE).path(ConfigKeys.SPOTTER_REST_EXTENSION_PARAMETERS)
				.path(extName.toString()).accept(MediaType.APPLICATION_JSON)
				.get(new GenericType<SpotterServiceResponse<Set<ConfigParameterDescription>>>() {
				});
		switch (response.getStatus()) {
		case OK:
			return response.getPayload();
		case SERVER_ERROR:
			throw new RuntimeException("Server error!: " + response.getErrorMessage());
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
			throw new RuntimeException("Server error!: " + response.getErrorMessage());
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
			throw new RuntimeException("Server error!: " + response.getErrorMessage());
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
			throw new RuntimeException("Server error!: " + response.getErrorMessage());
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
			throw new RuntimeException("Server error!: " + response.getErrorMessage());
		case INVALID_STATE:
		default:
			throw new IllegalStateException("Illegal response state!");
		}
	}

	public static String encodeUrl(String url) {
		try {
			return encodeUrl(url, CHARSET);
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalArgumentException(CHARSET);
		}
	}
	
	public static String encodeUrl(String url, String charsetName) throws UnsupportedEncodingException {
		if (!needsEncoding(url)) {
			return url;
		}

		int length = url.length();

		StringBuffer out = new StringBuffer(length);

		ByteArrayOutputStream buf = new ByteArrayOutputStream(10);

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(buf, charsetName));

		for (int i = 0; i < length; i++) {
			int c = (int) url.charAt(i);
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == ' ') {
				if (c == ' ') {
					c = '+';
				}

				toHex(out, buf.toByteArray());
				buf.reset();

				out.append((char) c);
			} else {
				try {
					writer.write(c);

					if (c >= 0xD800 && c <= 0xDBFF && i < length - 1) {
						int d = (int) url.charAt(i + 1);
						if (d >= 0xDC00 && d <= 0xDFFF) {
							writer.write(d);
							i++;
						}
					}

					writer.flush();
				} catch (IOException ex) {
					throw new IllegalArgumentException(url);
				}
			}
		}
		try {
			writer.close();
		} catch (IOException ioe) {
			// Ignore exceptions on close.
		}

		toHex(out, buf.toByteArray());

		return out.toString();
	}

	private static void toHex(StringBuffer buffer, byte[] b) {
		for (int i = 0; i < b.length; i++) {
			buffer.append('%');

			char ch = Character.forDigit((b[i] >> 4) & 0xF, 16);
			if (Character.isLetter(ch)) {
				ch -= 32;
			}
			buffer.append(ch);

			ch = Character.forDigit(b[i] & 0xF, 16);
			if (Character.isLetter(ch)) {
				ch -= 32;
			}
			buffer.append(ch);
		}
	}

	private static boolean needsEncoding(String s) {
		if (s == null)
			return false;

		for (int i = 0; i < s.length(); i++) {
			int c = (int) s.charAt(i);
			if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9'))
				return true;
			// Otherwise, keep going
		}

		return false;
	}

}
