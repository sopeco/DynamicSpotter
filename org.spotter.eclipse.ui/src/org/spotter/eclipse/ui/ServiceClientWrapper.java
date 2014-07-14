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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.lpe.common.config.ConfigParameterDescription;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.client.SpotterServiceClient;
import org.spotter.eclipse.ui.model.ExtensionMetaobject;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;
import org.spotter.shared.configuration.SpotterExtensionType;
import org.spotter.shared.status.SpotterProgress;

public class ServiceClientWrapper {

	public static final String DEFAULT_SERVICE_HOST = "localhost";
	public static final String DEFAULT_SERVICE_PORT = "8080";
	public static final String KEY_SERVICE_HOST = "spotter.service.host";
	public static final String KEY_SERVICE_PORT = "spotter.service.port";

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceClientWrapper.class);
	private static final String DIALOG_TITLE = "Spotter Service Client";
	private static final String ERR_MSG_CONN = "Connection to '%s' at port %s could not be established!";
	private static final String MSG_FAIL_SAFE = "Error while storing preferences. Please try again.";
	private static final String MSG_NO_CONN = "Action cannot be performed without connection to Spotter Service. Please check connection settings and try again.";
	private static final String MSG_START_DIAGNOSIS = "Could not start diagnosis!";
	private static final String MSG_NO_STATUS = "Could not query status due to missing connection.";
	private static final String MSG_NO_CONFIG_PARAMS = "Could not query configuration parameters due to missing connection.";
	private static final String MSG_NO_EXTENSIONS = "Could not query list of extensions due to missing connection.";
	private static final String MSG_NO_SATTELITE_TEST = "Could not test sattelite connection because cannot reach Spotter Service.";

	private final String projectName;
	private final SpotterServiceClient client;
	private String host;
	private String port;

	// caching
	private Set<ConfigParameterDescription> cachedSpotterConfParameters;
	private Map<String, ConfigParameterDescription> cachedSpotterConfParamsMap;
	private Map<SpotterExtensionType, Set<String>> cachedExtensionNames = new HashMap<>();
	private Map<SpotterExtensionType, ExtensionMetaobject[]> cachedExtensionMetaobjects = new HashMap<>();
	private Map<String, Set<ConfigParameterDescription>> cachedExtensionConfParamters = new HashMap<>();
	private Map<String, String> cachedExtensionDescriptions = new HashMap<>();

	/**
	 * Creates a new instance using the default host and port.
	 */
	public ServiceClientWrapper() {
		this.host = DEFAULT_SERVICE_HOST;
		this.port = DEFAULT_SERVICE_PORT;
		this.projectName = null;
		this.client = new SpotterServiceClient(host, port);
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
		Preferences prefs;
		if (projectName == null) {
			prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		} else {
			prefs = SpotterProjectSupport.getProjectPreferences(projectName);
		}

		this.host = prefs.get(KEY_SERVICE_HOST, DEFAULT_SERVICE_HOST);
		this.port = prefs.get(KEY_SERVICE_PORT, DEFAULT_SERVICE_PORT);
		this.projectName = projectName;
		this.client = new SpotterServiceClient(host, port);
	}

	public String getProjectName() {
		return this.projectName;
	}

	public String getHost() {
		return this.host;
	}

	public String getPort() {
		return this.port;
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
			LOGGER.error("Saving Service Client settings failed. Cause: {}", e);
			// restore old values
			prefs.put(KEY_SERVICE_HOST, oldHost);
			prefs.put(KEY_SERVICE_PORT, oldPort);
			showErrorMessage(MSG_FAIL_SAFE);
		}
		return false;
	}

	public Long startDiagnosis(final String configurationFile) {
		try {
			return client.startDiagnosis(configurationFile);
		} catch (Exception e) {
			LOGGER.error("startDiagnosis request failed: " + e.getMessage());
			showErrorMessage(MSG_START_DIAGNOSIS + (e.getMessage() == null ? "" : " " + e.getMessage()), host, port);
		}
		return null;
	}

	public boolean isRunning() {
		try {
			return client.isRunning();
		} catch (Exception e) {
			showWarningMessage(MSG_NO_STATUS);
		}
		return false;
	}

	/**
	 * @return set of configuration parameter descriptions for Spotter
	 *         configuration.
	 */
	public Set<ConfigParameterDescription> getConfigurationParameters() {
		if (cachedSpotterConfParameters != null) {
			return cachedSpotterConfParameters;
		}
		try {
			cachedSpotterConfParameters = client.getConfigurationParameters();
		} catch (Exception e) {
			LOGGER.error("getConfigurationParameters request failed: " + e.getMessage());
			showErrorMessage(MSG_NO_CONFIG_PARAMS, host, port);
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
		if (cachedSpotterConfParamsMap == null && (cachedSpotterConfParamsMap = initSpotterConfParamsMap()) == null) {
			return null;
		}
		return cachedSpotterConfParamsMap.get(name);
	}

	/**
	 * Returns an array of extension meta objects for the given extension type.
	 * 
	 * @param extType
	 *            extension type of interest
	 * @return array of extension meta objects for the given extension type. In
	 *         the case of an error an empty array is returned.
	 */
	public ExtensionMetaobject[] getAvailableExtensions(SpotterExtensionType extType) {
		ExtensionMetaobject[] metaobjects = cachedExtensionMetaobjects.get(extType);
		if (metaobjects != null) {
			return metaobjects;
		}
		Set<String> extNames = getAvailableExtensionNames(extType);
		if (extNames == null) {
			return new ExtensionMetaobject[0];
		}

		List<ExtensionMetaobject> list = new ArrayList<ExtensionMetaobject>();
		for (String extName : extNames) {
			Set<ConfigParameterDescription> extensionConfParams = client.getExtensionConfigParamters(extName);
			if (extensionConfParams == null) {
				continue;
			}
			list.add(new ExtensionMetaobject(projectName, extName, extensionConfParams));
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
		Set<String> extNames = cachedExtensionNames.get(extType);
		if (extNames != null) {
			return extNames;
		}
		try {
			extNames = client.getAvailableExtensions(extType);
			cachedExtensionNames.put(extType, extNames);
		} catch (Exception e) {
			LOGGER.error("getAvailableExtensions request failed for extType " + extType + ": " + e.getMessage());
			showErrorMessage(MSG_NO_EXTENSIONS, host, port);
		}
		return extNames;
	}

	public Set<ConfigParameterDescription> getExtensionConfigParamters(String extName) {
		Set<ConfigParameterDescription> confParams = cachedExtensionConfParamters.get(extName);
		if (confParams != null) {
			if (!cachedExtensionDescriptions.containsKey(extName)) {
				cachedExtensionDescriptions.put(extName, findExtensionDescription(confParams));
			}
			return confParams;
		}
		try {
			confParams = client.getExtensionConfigParamters(extName);
			cachedExtensionConfParamters.put(extName, confParams);
		} catch (Exception e) {
			LOGGER.error("getExtensionConfigParameters request failed: " + e.getMessage());
			showErrorMessage(MSG_NO_CONFIG_PARAMS, host, port);
		}
		return confParams;
	}

	public String getExtensionDescription(String extName) {
		if (!cachedExtensionConfParamters.containsKey(extName)) {
			// force caching of the extension description
			getExtensionConfigParamters(extName);
		}
		return cachedExtensionDescriptions.get(extName);
	}

	public SpotterProgress getCurrentProgressReport() {
		try {
			return client.getCurrentProgressReport();
		} catch (Exception e) {
			LOGGER.error("getCurrentProgressReport request failed: " + e.getMessage());
			showErrorMessage(MSG_NO_STATUS, host, port);
		}
		return null;
	}

	public Long getCurrentJobId() {
		try {
			return client.getCurrentJobId();
		} catch (Exception e) {
			LOGGER.error("getCurrentJobId request failed: " + e.getMessage());
			showErrorMessage(MSG_NO_STATUS, host, port);
		}
		return null;
	}

	public boolean testConnectionToSattelite(String extName, String host, String port) {
		try {
			return client.testConnectionToSattelite(extName, host, port);
		} catch (Exception e) {
			LOGGER.error("testConnectionToSattelite request failed: " + e.getMessage());
			showErrorMessage(MSG_NO_SATTELITE_TEST, host, port);
		}
		return false;
	}

	public boolean testConnection(boolean showErrorDialog) {
		boolean connection;
		try {
			connection = client.testConnection();
		} catch (Exception e) {
			// LOGGER.debug("testConnection request returned due to no connection to server");
			connection = false;
		}
		if (showErrorDialog && !connection) {
			showErrorMessage(MSG_NO_CONN, host, port);
		}
		return connection;
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
	}

	public static void showErrorMessage(String errorMessage) {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		MessageDialog.openError(shell, DIALOG_TITLE, errorMessage);
	}

	public static void showErrorMessage(String detailedMessage, String host, String port) {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		String msg = String.format(ERR_MSG_CONN, host, port);
		if (detailedMessage != null) {
			msg += "\n\n" + detailedMessage;
		}
		MessageDialog.openError(shell, DIALOG_TITLE, msg);
	}

	public static void showWarningMessage(String warningMessage) {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		MessageDialog.openWarning(shell, DIALOG_TITLE, warningMessage);
	}

	public static void showWarningMessage(String detailedMessage, String host, String port) {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		String msg = String.format(ERR_MSG_CONN, host, port);
		if (detailedMessage != null) {
			msg += "\n\n" + detailedMessage;
		}
		MessageDialog.openWarning(shell, DIALOG_TITLE, msg);
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
