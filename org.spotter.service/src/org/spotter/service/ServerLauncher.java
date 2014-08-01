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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.extension.ExtensionRegistry;
import org.lpe.common.util.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server launcher for the DynamicSpotter service.
 * 
 * @author Peter Merkert
 */
public final class ServerLauncher {

	private static final int DEFAULT_PORT = 8080;

	private static final String DEFAULT_PLUGINS_FOLDER = "plugins";

	private static final String SPOTTER_ROOT_DIR_KEY = "rootDir=";

	private static final String PORT_KEY = "port=";

	private static final String HELP_KEY = "-h";

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerLauncher.class);

	private static Integer port = DEFAULT_PORT;

	private static String rootDir = System.getProperty("user.dir");

	private static boolean help = false;

	/**
	 * Private constructor due to singleton class.
	 */
	private ServerLauncher() {
	}

	/**
	 * Opens up a server on the localhost IP address and the default port 8080
	 * of the underlying system.
	 * 
	 * @param args
	 *            should contain at least one parameter indicating whether to
	 *            start or stop
	 */
	public static void main(String[] args) {

		if (args != null) {
			parseArgs(args);

			if (args.length < 1 || help) {
				printHelp();
			} else {
				Properties coreProperties = new Properties();
				coreProperties.setProperty(ExtensionRegistry.APP_ROOT_DIR_PROPERTY_KEY, rootDir);
				coreProperties.setProperty(ExtensionRegistry.PLUGINS_FOLDER_PROPERTY_KEY, DEFAULT_PLUGINS_FOLDER);

				if (args[0].equalsIgnoreCase("start")) {
					GlobalConfiguration.initialize(coreProperties);
					List<String> servicePackages = new ArrayList<>();
					servicePackages.add("org.spotter.service.rest");
					WebServer.getInstance().start(port, "", servicePackages);
				} else if (args[0].equalsIgnoreCase("shutdown")) {
					WebServer.triggerServerShutdown(port, "");
				} else {
					LOGGER.error("Invalid value for 1st argument! Valid values are: start / shutdown");
				}

			}

		} else {
			printHelp();
		}

	}

	private static void printHelp() {
		LOGGER.info("DynamicSpotter Service Launcher requires at least one argument:");
		LOGGER.info("Usage: java -jar <SPOTTER_SERVER_JAR> {start | shutdown} [options]");
		LOGGER.info("the options are:");
		LOGGER.info(HELP_KEY + ": show this help text");
		LOGGER.info(PORT_KEY + "=<PORT>: port to bind the server to, default: 8080");
		LOGGER.info(SPOTTER_ROOT_DIR_KEY
				+ "=<PATH_TO_SPOTTER_ROOT>: path to the root directory of spotter. "
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
			if (arg.startsWith(PORT_KEY)) {
				port = Integer.parseInt(arg.substring(PORT_KEY.length()));
			}
			if (arg.startsWith(SPOTTER_ROOT_DIR_KEY)) {
				rootDir = arg.substring(SPOTTER_ROOT_DIR_KEY.length());
			}
			if (arg.startsWith(HELP_KEY)) {
				help = true;
			}

		}
	}

}
