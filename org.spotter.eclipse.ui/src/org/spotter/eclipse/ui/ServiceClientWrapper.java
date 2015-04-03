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
package org.spotter.eclipse.ui;

import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.lpe.common.config.ConfigParameterDescription;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.client.SpotterServiceClient;
import org.spotter.eclipse.ui.model.ExtensionMetaobject;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;
import org.spotter.shared.configuration.JobDescription;
import org.spotter.shared.configuration.SpotterExtensionType;
import org.spotter.shared.hierarchy.model.RawHierarchyFactory;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;
import org.spotter.shared.status.SpotterProgress;

import com.sun.jersey.api.client.ClientHandlerException;

/**
 * <p>
 * A wrapper for the Spotter Service Client that delegates to the client and
 * handles arising exceptions by providing meaningful screen messages to the
 * user. Exceptions thrown during a request are stored and can be retrieved
 * afterwards via {@link #getLastException()}.
 * </p>
 * <p>
 * This wrapper class also caches requested information from the server for
 * future use. Whenever the client settings change, the cache will also be
 * cleared automatically or it can be cleared on demand.
 * </p>
 * 
 * @author Denis Knoepfle
 * 
 */
public class ServiceClientWrapper {

	public static final String DEFAULT_SERVICE_HOST = "localhost";
	public static final String DEFAULT_SERVICE_PORT = "8080";
	public static final String KEY_SERVICE_HOST = "spotter.service.host";
	public static final String KEY_SERVICE_PORT = "spotter.service.port";

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceClientWrapper.class);

	private static enum HandlerStyle {
		SILENT, LOG_ONLY, SHOW
	}

	private static final String DIALOG_TITLE = "DynamicSpotter Service Client";
	private static final String ERR_MSG_CONN = "Connection to '%s' at port %s could not be established!";
	private static final String MSG_FAIL_SAFE = "Error while storing preferences. Please try again.";
	private static final String MSG_NO_ACTION = "Action cannot be performed without connection to DynamicSpotter Service. "
			+ "Please check connection settings and try again.";
	private static final String MSG_START_DIAGNOSIS = "Could not start diagnosis!";
	private static final String MSG_REQU_RESULTS = "Could not retrieve diagnosis results!";
	private static final String MSG_NO_STATUS = "Could not retrieve status.";
	private static final String MSG_NO_RUN_EXCEPTION = "Could not retrieve the last run exception.";
	private static final String MSG_NO_CONFIG_PARAMS = "Could not retrieve configuration parameters.";
	private static final String MSG_NO_EXTENSIONS = "Could not retrieve list of extensions.";
	private static final String MSG_NO_DEFAULT_HIER = "Could not retrieve the default hierarchy.";
	private static final String MSG_NO_SATTELITE_TEST = "Could not test satellite connection.";

	private final String projectName;
	private final SpotterServiceClient client;
	private Exception lastClientException;
	private String host;
	private String port;

	// used for caching
	private Set<ConfigParameterDescription> cachedSpotterConfParameters;
	private Map<String, ConfigParameterDescription> cachedSpotterConfParamsMap;
	private Map<SpotterExtensionType, Set<String>> cachedExtensionNames = new HashMap<>();
	private Map<SpotterExtensionType, ExtensionMetaobject[]> cachedExtensionMetaobjects = new HashMap<>();
	private Map<String, Set<ConfigParameterDescription>> cachedExtensionConfParamters = new HashMap<>();
	private Map<String, String> cachedExtensionDescriptions = new HashMap<>();
	private long lastClearTime;

	/**
	 * Creates a new instance using the default host and port.
	 */
	public ServiceClientWrapper() {
		this(null, true);
	}

	/**
	 * Creates a new instance. Service client settings will be stored using the
	 * given project name as identifier. Providing <code>null</code> means that
	 * those settings will be saved in the plugin's root scope instead.
	 * 
	 * @param projectName
	 *            The project the settings should be saved for or
	 *            <code>null</code> to store them at the plugin's root scope
	 */
	public ServiceClientWrapper(String projectName) {
		this(projectName, false);
	}

	private ServiceClientWrapper(String projectName, boolean useDefaults) {
		if (useDefaults) {
			this.host = DEFAULT_SERVICE_HOST;
			this.port = DEFAULT_SERVICE_PORT;
		} else {
			Preferences prefs;
			if (projectName == null) {
				prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
			} else {
				prefs = SpotterProjectSupport.getProjectPreferences(projectName);
			}
			this.host = prefs.get(KEY_SERVICE_HOST, DEFAULT_SERVICE_HOST);
			this.port = prefs.get(KEY_SERVICE_PORT, DEFAULT_SERVICE_PORT);
		}

		this.projectName = projectName;
		this.client = new SpotterServiceClient(host, port);
		this.lastClientException = null;
		this.lastClearTime = System.currentTimeMillis();
	}

	/**
	 * @return The name of the project this client is associated with.
	 */
	public String getProjectName() {
		return this.projectName;
	}

	/**
	 * @return The host of this client.
	 */
	public String getHost() {
		return this.host;
	}

	/**
	 * @return The port of this client.
	 */
	public String getPort() {
		return this.port;
	}

	/**
	 * Returns a value that determines the last time the cache was cleared. If a
	 * previous received value is smaller than the current one it means that the
	 * cache has been cleared in the meantime.
	 * 
	 * @return a number that can be used for comparison to check if cache has
	 *         been cleared meanwhile
	 */
	public long getLastClearTime() {
		return lastClearTime;
	}

	/**
	 * Updates the URL of the wrapped client.
	 * 
	 * @param newHost
	 *            The new host
	 * @param newPort
	 *            The new port
	 */
	public void updateUrl(String newHost, String newPort) {
		if (!host.equals(newHost) || !port.equals(newPort)) {
			client.updateUrl(newHost, newPort);
			host = newHost;
			port = newPort;
			clearCache();
		}
	}

	/**
	 * Saves preferences for the service client for the given project at the
	 * workspace level and updates the plugin's Spotter Service Client.
	 * 
	 * @param newHost
	 *            The new host
	 * @param newPort
	 *            The new port
	 * @return <code>true</code> on success, otherwise <code>false</code>
	 */
	public boolean saveServiceClientSettings(String newHost, String newPort) {
		Preferences prefs;
		if (projectName == null) {
			prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		} else {
			prefs = SpotterProjectSupport.getProjectPreferences(projectName);
		}
		String oldHost = prefs.get(KEY_SERVICE_HOST, DEFAULT_SERVICE_HOST);
		String oldPort = prefs.get(KEY_SERVICE_PORT, DEFAULT_SERVICE_PORT);

		prefs.put(KEY_SERVICE_HOST, newHost);
		prefs.put(KEY_SERVICE_PORT, newPort);

		// force save
		try {
			prefs.flush();
			// update current client
			updateUrl(newHost, newPort);
			return true;
		} catch (BackingStoreException e) {
			LOGGER.error("Saving Service Client settings failed.", e);
			// restore old values
			prefs.put(KEY_SERVICE_HOST, oldHost);
			prefs.put(KEY_SERVICE_PORT, oldPort);

			DialogUtils.handleError(MSG_FAIL_SAFE, e);
		}
		return false;
	}

	/**
	 * Starts the diagnosis using the given job description. Returns the
	 * retrieved job id or <code>null</code> on failure.
	 * 
	 * @param jobDescription
	 *            The job description to use.
	 * @return The retrieved job id or <code>null</code> on failure.
	 */
	public Long startDiagnosis(final JobDescription jobDescription) {
		lastClientException = null;
		try {
			return client.startDiagnosis(jobDescription);
		} catch (Exception e) {
			handleException("startDiagnosis", MSG_START_DIAGNOSIS, e, HandlerStyle.SHOW, false);
		}
		return null;
	}

	/**
	 * Requests the results of a the run with the given job id.
	 * 
	 * @param jobId
	 *            the job id of the diagnosis run
	 * @return input stream containing the zipped run result folder or
	 *         <code>null</code> if none found
	 */
	public InputStream requestResults(final String jobId) {
		lastClientException = null;
		try {
			return client.requestResults(jobId);
		} catch (Exception e) {
			handleException("requestResults", MSG_REQU_RESULTS, e, HandlerStyle.SHOW, false);
		}
		return null;
	}

	/**
	 * Returns <code>true</code> if DynamicSpotter diagnostics is currently
	 * running, otherwise <code>false</code>.
	 * 
	 * @param silent
	 *            <code>true</code> to disable dialog pop-up and logging
	 * @return <code>true</code> if currently running, otherwise
	 *         <code>false</code>
	 */
	public boolean isRunning(boolean silent) {
		lastClientException = null;
		try {
			return client.isRunning();
		} catch (Exception e) {
			HandlerStyle style = silent ? HandlerStyle.SILENT : HandlerStyle.SHOW;
			handleException("isRunning", MSG_NO_STATUS, e, style, true);
		}
		return false;
	}

	/**
	 * Returns the exception thrown during the last diagnosis run or
	 * <code>null</code> if none.
	 * 
	 * @param silent
	 *            <code>true</code> to disable dialog pop-up and logging
	 * @return the exception thrown during the last diagnosis run or
	 *         <code>null</code> if none
	 */
	public Exception getLastRunException(boolean silent) {
		lastClientException = null;
		try {
			return client.getLastRunException();
		} catch (Exception e) {
			HandlerStyle style = silent ? HandlerStyle.SILENT : HandlerStyle.SHOW;
			handleException("getLastRunException", MSG_NO_RUN_EXCEPTION, e, style, true);
		}
		return null;
	}

	/**
	 * @return set of configuration parameter descriptions for DynamicSpotter
	 *         configuration.
	 */
	public Set<ConfigParameterDescription> getConfigurationParameters() {
		lastClientException = null;
		if (cachedSpotterConfParameters != null) {
			return cachedSpotterConfParameters;
		}
		try {
			cachedSpotterConfParameters = client.getConfigurationParameters();
		} catch (Exception e) {
			handleException("getConfigurationParameters", MSG_NO_CONFIG_PARAMS, e, HandlerStyle.SHOW, false);
		}
		return cachedSpotterConfParameters;
	}

	/**
	 * Returns the <code>ConfigParameterDescription</code> that suits the given
	 * name.
	 * 
	 * @param name
	 *            name of the description object to retrieve
	 * @return the matching description object or <code>null</code> if not found
	 */
	public ConfigParameterDescription getSpotterConfigParam(String name) {
		lastClientException = null;
		if (cachedSpotterConfParamsMap == null) {
			cachedSpotterConfParamsMap = initSpotterConfParamsMap();
			if (cachedSpotterConfParamsMap == null) {
				return null;
			}
		}
		return cachedSpotterConfParamsMap.get(name);
	}

	/**
	 * Returns an array of extension meta objects for the given extension type
	 * or <code>null</code> on failure.
	 * 
	 * @param extType
	 *            extension type of interest
	 * @return array of extension meta objects for the given extension type. In
	 *         the case of an error <code>null</code> is returned.
	 */
	public ExtensionMetaobject[] getAvailableExtensions(SpotterExtensionType extType) {
		lastClientException = null;
		ExtensionMetaobject[] metaobjects = cachedExtensionMetaobjects.get(extType);
		if (metaobjects != null) {
			return metaobjects;
		}
		Set<String> extNames = getAvailableExtensionNames(extType);
		if (extNames == null) {
			return null;
		}

		List<ExtensionMetaobject> list = new ArrayList<ExtensionMetaobject>();
		for (String extName : extNames) {
			// force caching and ignore invalid extensions
			if (getExtensionConfigParamters(extName, HandlerStyle.LOG_ONLY) == null) {
				continue;
			}
			list.add(new ExtensionMetaobject(projectName, extName));
		}

		metaobjects = list.toArray(new ExtensionMetaobject[list.size()]);
		cachedExtensionMetaobjects.put(extType, metaobjects);
		return metaobjects;
	}

	/**
	 * Returns a set of extension names for the given extension type.
	 * 
	 * @param extType
	 *            extension type of interest
	 * @return a set of extension names for the given extension type
	 */
	public Set<String> getAvailableExtensionNames(SpotterExtensionType extType) {
		lastClientException = null;
		Set<String> extNames = cachedExtensionNames.get(extType);
		if (extNames != null) {
			return extNames;
		}
		try {
			extNames = client.getAvailableExtensions(extType);
			cachedExtensionNames.put(extType, extNames);
		} catch (Exception e) {
			handleException("getAvailableExtensions", MSG_NO_EXTENSIONS, e, HandlerStyle.SHOW, false);
		}
		return extNames;
	}

	/**
	 * Returns the extension configuration parameters for the given extension or
	 * <code>null</code> if the extension doesn't exist.
	 * 
	 * @param extName
	 *            The name of the extension
	 * @return the extension configuration parameters for the extension or
	 *         <code>null</code>
	 */
	public Set<ConfigParameterDescription> getExtensionConfigParamters(String extName) {
		return getExtensionConfigParamters(extName, HandlerStyle.SHOW);
	}

	private Set<ConfigParameterDescription> getExtensionConfigParamters(String extName, HandlerStyle style) {
		lastClientException = null;
		Set<ConfigParameterDescription> confParams = cachedExtensionConfParamters.get(extName);
		if (confParams != null) {
			if (!cachedExtensionDescriptions.containsKey(extName)) {
				cachedExtensionDescriptions.put(extName, findExtensionDescription(confParams));
			}
			return confParams;
		}
		try {
			confParams = client.getExtensionConfigParamters(extName);
			if (confParams != null) {
				cachedExtensionConfParamters.put(extName, confParams);
				cachedExtensionDescriptions.put(extName, findExtensionDescription(confParams));
			}
		} catch (Exception e) {
			handleException("getExtensionConfigParameters", MSG_NO_CONFIG_PARAMS, e, style, false);
		}
		return confParams;
	}

	/**
	 * Returns the textual description of the given extension.
	 * 
	 * @param extName
	 *            The name of the extension
	 * @return the textual description of the given extension
	 */
	public String getExtensionDescription(String extName) {
		lastClientException = null;
		if (!cachedExtensionConfParamters.containsKey(extName)) {
			// force caching of the extension description
			getExtensionConfigParamters(extName);
		}
		return cachedExtensionDescriptions.get(extName);
	}

	/**
	 * Returns the default hierarchy.
	 * 
	 * @return the default hierarchy
	 */
	public XPerformanceProblem getDefaultHierarchy() {
		lastClientException = null;
		try {
			return client.getDefaultHierarchy();
		} catch (Exception e) {
			handleException("getDefaultHierarchy", MSG_NO_DEFAULT_HIER, e, HandlerStyle.LOG_ONLY, true);
		}
		return RawHierarchyFactory.getInstance().createEmptyHierarchy();
	}

	/**
	 * Returns a report on the progress of the current job.
	 * 
	 * @return a report on the progress of the current job
	 */
	public SpotterProgress getCurrentProgressReport() {
		lastClientException = null;
		try {
			return client.getCurrentProgressReport();
		} catch (Exception e) {
			handleException("getCurrentProgressReport", MSG_NO_STATUS, e, HandlerStyle.SHOW, false);
		}
		return null;
	}

	/**
	 * Returns the id of the currently running job.
	 * 
	 * @return the id of the currently running job
	 */
	public Long getCurrentJobId() {
		lastClientException = null;
		try {
			return client.getCurrentJobId();
		} catch (Exception e) {
			handleException("getCurrentJobId", MSG_NO_STATUS, e, HandlerStyle.SHOW, false);
		}
		return null;
	}

	/**
	 * Returns the root problem of the currently running job.
	 * 
	 * @param silent
	 *            <code>true</code> to disable dialog pop-up and logging
	 * @return the root problem
	 */
	public XPerformanceProblem getCurrentRootProblem(boolean silent) {
		lastClientException = null;
		try {
			return client.getCurrentRootProblem();
		} catch (Exception e) {
			HandlerStyle style = silent ? HandlerStyle.SILENT : HandlerStyle.SHOW;
			handleException("getCurrentRootProblem", MSG_NO_STATUS, e, style, false);
		}
		return null;
	}

	/**
	 * Tests connection to the satellite specified by the given extension name,
	 * host and port. If extension is not a satellite this method returns
	 * <code>false</code>!
	 * 
	 * @param extName
	 *            The name of the extension
	 * @param host
	 *            The host/ip to connect to
	 * @param port
	 *            The port to connect to
	 * @return <code>true</code> if connection could have been established,
	 *         otherwise <code>false</code>
	 */
	public boolean testConnectionToSattelite(String extName, String host, String port) {
		lastClientException = null;
		try {
			return client.testConnectionToSattelite(extName, host, port);
		} catch (Exception e) {
			handleException("testConnectionToSattelite", MSG_NO_SATTELITE_TEST, e, HandlerStyle.SHOW, false);
		}
		return false;
	}

	/**
	 * Tests connection to the DS Service.
	 * 
	 * @param showErrorDialog
	 *            <code>true</code> to show an error dialog on failure
	 * 
	 * @return <code>true</code> if connection could have been established,
	 *         otherwise <code>false</code>
	 */
	public boolean testConnection(boolean showErrorDialog) {
		lastClientException = null;
		boolean connection;
		try {
			connection = client.testConnection();
		} catch (Exception e) {
			connection = false;
			lastClientException = e;
		}
		if (showErrorDialog && !connection) {
			showConnectionProblemMessage(MSG_NO_ACTION, host, port, false);
		}
		return connection;
	}

	/**
	 * Returns the last exception thrown on the client side during a request.
	 * 
	 * @return the last exception thrown
	 */
	public Exception getLastClientException() {
		return lastClientException;
	}

	/**
	 * Returns whether the reason for the last exception was a connection issue.
	 * 
	 * @return <code>true</code> if it was connection issue, <code>false</code>
	 *         otherwise
	 */
	public boolean isConnectionIssue() {
		if (lastClientException instanceof ClientHandlerException) {
			if (lastClientException.getCause() instanceof ConnectException) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Clears the cache deleting all data that was fetched from the server.
	 */
	public void clearCache() {
		// collections as a whole are returned or they are related,
		// so set to null after clear
		if (cachedSpotterConfParameters != null) {
			cachedSpotterConfParameters.clear();
			cachedSpotterConfParameters = null;
		}
		if (cachedSpotterConfParamsMap != null) {
			cachedSpotterConfParamsMap.clear();
			cachedSpotterConfParamsMap = null;
		}
		// only values of these maps are returned, so clear() is enough
		cachedExtensionNames.clear();
		cachedExtensionMetaobjects.clear();
		cachedExtensionConfParamters.clear();
		cachedExtensionDescriptions.clear();

		lastClearTime = System.currentTimeMillis();
	}

	/**
	 * Shows a message on the screen explaining the connection problem. If cause
	 * is not <code>null</code> it will be appended to the message.
	 * 
	 * @param cause
	 *            The cause of the problem
	 * @param host
	 *            The host used for the connection
	 * @param port
	 *            The port used for the connection
	 * @param warning
	 *            <code>true</code> to show a warning only, otherwise an error
	 *            is shown
	 */
	public static void showConnectionProblemMessage(String cause, String host, String port, boolean warning) {
		String msg = String.format(ERR_MSG_CONN, host, port);
		if (cause != null) {
			msg += "\n\n" + cause;
		}
		if (warning) {
			DialogUtils.openWarning(DIALOG_TITLE, msg);
		} else {
			DialogUtils.openError(DIALOG_TITLE, msg);
		}
	}

	/**
	 * Handles an exception and stores the given exception which can then be
	 * retrieved later via {@link #getLastException()}.
	 * 
	 * @param requestName
	 *            The name of the requester. This name is only used for logging.
	 * @param requestErrorMsg
	 *            The error message for the request failure. This message is
	 *            included in the screen message in case of a connection issue.
	 * @param exception
	 *            The exception thrown in the request
	 * @param style
	 *            the style how the exception should be communicated
	 * @param warning
	 *            <code>true</code> for warning, <code>false</code> for error
	 */
	private void handleException(String requestName, String requestErrorMsg, Exception exception, HandlerStyle style,
			boolean warning) {
		lastClientException = exception;

		if (style == HandlerStyle.SILENT) {
			return;
		}

		if (warning) {
			LOGGER.warn("{} request failed! Cause: {}", requestName, exception.toString());
		} else {
			LOGGER.error("{} request failed! Cause: {}", requestName, exception.toString());
		}

		if (style == HandlerStyle.LOG_ONLY) {
			return;
		}

		if (isConnectionIssue()) {
			showConnectionProblemMessage(requestErrorMsg, host, port, warning);
		} else {
			// illegal response state or server error
			String header = "DS Service returned with an error!";
			String message = exception.getMessage();
			if (message == null) {
				message = "Exception (" + exception.getClass().getName() + ") contains no message.";
			}
			String fullMessage = header + "\n\n" + message;
			if (warning) {
				DialogUtils.openWarning(DIALOG_TITLE, fullMessage);
			} else {
				DialogUtils.handleError(header, exception);
			}
		}
	}

	private Map<String, ConfigParameterDescription> initSpotterConfParamsMap() {
		Map<String, ConfigParameterDescription> map = new HashMap<String, ConfigParameterDescription>();
		Set<ConfigParameterDescription> settings = getConfigurationParameters();
		if (settings == null) {
			return null;
		}
		for (ConfigParameterDescription desc : settings) {
			map.put(desc.getName(), desc);
		}
		return map;
	}

	private String findExtensionDescription(Set<ConfigParameterDescription> confParams) {
		for (ConfigParameterDescription desc : confParams) {
			if (desc.getName().equals(ConfigParameterDescription.EXT_DESCRIPTION_KEY)) {
				return desc.getDefaultValue();
			}
		}
		return null;
	}

}
