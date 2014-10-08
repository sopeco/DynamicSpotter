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
package org.spotter.shared.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.spotter.shared.environment.model.XMeasurementEnvironment;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;
import org.spotter.shared.util.JAXBUtil;

/**
 * A file manager to write configuration files of DS projects.
 * 
 * @author Denis Knoepfle
 * 
 */
public final class FileManager {

	public static final String DEFAULT_RESULTS_DIR_NAME = "results";
	public static final String SPOTTER_CONFIG_FILENAME = "spotter.conf";
	public static final String ENVIRONMENT_FILENAME = "mEnv.xml";
	public static final String HIERARCHY_FILENAME = "hierarchy.xml";

	private static final String KEY_HIERARCHY_FILE_DESC = "path to the XML file describing the problem hierarchy";
	private static final String KEY_ENVIRONMENT_FILE_DESC = "path to the XML file describing all measurement satellites and their configurations";
	private static final String KEY_RESULTS_DIR_DESC = "path to the directory containing the results";

	private static FileManager instance;

	private FileManager() {
	}

	/**
	 * Returns the singleton.
	 * 
	 * @return the singleton instance
	 */
	public static synchronized FileManager getInstance() {
		if (instance == null) {
			instance = new FileManager();
		}
		return instance;
	}

	/**
	 * Writes the DS configuration file.
	 * 
	 * @param location
	 *            the location where to place this file (without the filename)
	 * @param properties
	 *            the project specific properties to write
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void writeSpotterConfig(String location, Properties properties) throws IOException {
		Properties general = createGeneralSpotterProperties(location);

		String file = stripFileSeparator(location) + "/" + FileManager.SPOTTER_CONFIG_FILENAME;
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
			fileWriter.write(createSpotterConfigFileContent(null, general, properties));
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
	}

	/**
	 * Writes the environment configuration.
	 * 
	 * @param location
	 *            the location where to place this file (without the filename)
	 * @param environment
	 *            the environment object to write
	 * @throws JAXBException
	 *             if a problem with the marshalling occurs
	 */
	public void writeEnvironmentConfig(String location, XMeasurementEnvironment environment) throws JAXBException {
		String file = stripFileSeparator(location) + "/" + FileManager.ENVIRONMENT_FILENAME;
		JAXBUtil.writeElementToFile(new File(file), environment);
	}

	/**
	 * Writes the hierarchy configuration.
	 * 
	 * @param location
	 *            the location where to place this file (without the filename)
	 * @param hierarchy
	 *            the hierarchy object to write
	 * @throws JAXBException
	 *             if a problem with the marshalling occurs
	 */
	public void writeHierarchyConfig(String location, XPerformanceProblem hierarchy) throws JAXBException {
		String file = stripFileSeparator(location) + "/" + FileManager.HIERARCHY_FILENAME;
		JAXBUtil.writeElementToFile(new File(file), hierarchy);
	}

	/**
	 * Creates general DynamicSpotter properties for the given project.
	 * 
	 * @param location
	 *            the location where the configuration files are
	 * @return the general DynamicSpotter properties for the given project
	 */
	public Properties createGeneralSpotterProperties(String location) {
		Properties properties = new Properties();
		location = stripFileSeparator(location);

		properties.setProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, location + "/" + HIERARCHY_FILENAME);
		properties.setProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, location + "/" + ENVIRONMENT_FILENAME);
		properties.setProperty(ConfigKeys.RESULT_DIR, location + "/" + DEFAULT_RESULTS_DIR_NAME);

		return properties;
	}

	/**
	 * Creates the content of the DS configuration file.
	 * 
	 * @param descriptionMapping
	 *            an optional mapping of keys to descriptive comments. May be
	 *            <code>null</code>.
	 * @param general
	 *            the general project settings (including paths to the other
	 *            configuration files)
	 * @param properties
	 *            the project specific properties
	 * @return the content of the configuration file as a string
	 */
	public String createSpotterConfigFileContent(Map<String, String> descriptionMapping, Properties general,
			Properties properties) {
		StringBuilder sb = new StringBuilder();
		writeHeading(sb, "GENERAL");
		writeKeyValuePair(sb, general, ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, KEY_HIERARCHY_FILE_DESC);
		writeKeyValuePair(sb, general, ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, KEY_ENVIRONMENT_FILE_DESC);
		writeKeyValuePair(sb, general, ConfigKeys.RESULT_DIR, KEY_RESULTS_DIR_DESC);
		sb.append("\r\n\r\n");
		writeHeading(sb, "SPECIFIED SETTINGS");

		for (String key : properties.stringPropertyNames()) {
			String comment = descriptionMapping == null ? null : descriptionMapping.get(key);
			writeKeyValuePair(sb, properties, key, comment);
		}
		return sb.toString();
	}

	private void writeHeading(StringBuilder sb, String heading) {
		sb.append("####################################\r\n### " + heading
				+ "\r\n####################################\r\n");
	}

	private void writeKeyValuePair(StringBuilder sb, Properties prop, String key, String comment) {
		sb.append("\r\n");
		if (comment != null) {
			sb.append("# " + comment + "\r\n");
		}
		sb.append(key + " = " + prop.getProperty(key) + "\r\n");
	}

	private String stripFileSeparator(String path) {
		if (path.endsWith("/") || path.endsWith("\\")) {
			return path.substring(0, path.length() - 1);
		} else {
			return path;
		}
	}

}
