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
package org.spotter.demo.app;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.lpe.common.util.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of the System Monitoring Utility. Starts a grizzly server and
 * initializes the Jersey application.
 * 
 * @author Henning Muszynski
 * 
 */
public final class ServerLauncher {
	private static final int NUM_WORKER_THREADS = 100;
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerLauncher.class);

	/**
	 * Private constructor due to singleton class.
	 */
	private ServerLauncher() {

	}

	/**
	 * Opens up a server on the localhost IP address and the port 8090 of the
	 * underlying system.
	 * 
	 * @param args
	 *            not used.
	 */
	public static void main(String[] args) {
		System.out.println("Starting Demo App at " + args[0]);
		try {
			new Socket(Inet4Address.getLocalHost().getHostAddress(), Integer.parseInt(args[0]));
			LOGGER.warn("Port {} already in use!", args[0]);
			System.exit(0);
		} catch (IOException ignored) {
			List<String> servicePackages = new ArrayList<>();
			servicePackages.add("org.spotter.demo");
			WebServer.getInstance().start(Integer.parseInt(args[0]), "", servicePackages, NUM_WORKER_THREADS / 2,
					NUM_WORKER_THREADS);
			LOGGER.info("Started Demo App!");
			WebServer.getInstance().waitForShutdown();
		}

	}

}
