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
package org.spotter.runner;

import java.io.File;
import java.util.Properties;

import org.aim.aiminterface.exceptions.InstrumentationException;
import org.aim.aiminterface.exceptions.MeasurementException;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.extension.ExtensionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.core.Spotter;
import org.spotter.exceptions.WorkloadException;

/**
 * Runs Spotter standalone.
 * 
 * @author Alexander Wert
 * 
 */
public final class SpotterRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpotterRunner.class);

	private static final String DEFAULT_PLUGINS_FOLDER = "plugins";

	private static final String SPOTTER_ROOT_DIR_KEY = "rootDir=";

	private static final String HELP_KEY = "-h";

	private static String rootDir = System.getProperty("user.dir");

	private static boolean help = false;

	/**
	 * Private constructor due to utility class.
	 */
	private SpotterRunner() {
	}

	/**
	 * Runs spotter.
	 * 
	 * @param args
	 *            program arguments should contain the path to the configuration
	 *            file!
	 * @throws WorkloadException
	 *             thrown if a problem with workload generation occurs
	 * @throws MeasurementException
	 *             thrown if a problem with measurement data retrieval occurs
	 * @throws InstrumentationException
	 *             thrown if a problem with instrumentation occurs
	 */
	public static void main(String[] args) throws InstrumentationException, MeasurementException, WorkloadException {
		if (args != null) {

			parseArgs(args);

			if (args.length < 1 || help) {
				LOGGER.error("Invalid value for 1st argument! Needs to be a path to the configuration file!");
				printHelp();
			} else {
				File configFile = new File(args[0]);
				if (!configFile.exists()) {
					LOGGER.error("Invalid value for 1st argument! Needs to be a path to the configuration file!");
					printHelp();
				} else {
					Properties coreProperties = new Properties();
					coreProperties.setProperty(ExtensionRegistry.APP_ROOT_DIR_PROPERTY_KEY, rootDir);
					coreProperties.setProperty(ExtensionRegistry.PLUGINS_FOLDER_PROPERTY_KEY, DEFAULT_PLUGINS_FOLDER);
					GlobalConfiguration.initialize(coreProperties);

					Spotter.getInstance().startDiagnosis(configFile.getAbsolutePath());
				}
			}

		} else {
			printHelp();
		}
	}

	private static void printHelp() {
		LOGGER.info("DynamicSpotter Service Launcher requires at least one argument:");
		LOGGER.info("Usage: java -jar <SPOTTER_RUNNER_JAR> PATH_TO_CONFIG_FILE [options]");
		LOGGER.info("the options are:");
		LOGGER.info(HELP_KEY + ": show this help text");
		LOGGER.info(SPOTTER_ROOT_DIR_KEY
				+ "<PATH_TO_SPOTTER_ROOT>: path to the root directory of spotter. "
				+ "Specifies where the location of the plugins folder for DynamicSpotter. Default root is the current directory.");
	}

	/**
	 * Parses the agent arguments.
	 * 
	 * @param agentArgs
	 *            arguments as string
	 */
	private static void parseArgs(String[] agentArgs) {

		for (String arg : agentArgs) {
			if (arg.startsWith(SPOTTER_ROOT_DIR_KEY)) {
				rootDir = arg.substring(SPOTTER_ROOT_DIR_KEY.length());
			}
			if (arg.startsWith(HELP_KEY)) {
				help = true;
			}

		}
	}
}
