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

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientHandlerException;

/**
 * Tests the {@link ServerLauncher} class.
 * 
 * @author Peter Merkert
 */
public class ServerLauncherTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerLauncherTest.class);
	private static final long SHUTDOWN_WAIT_DELAY = 5000;
	private static final int DEFAULT_PORT = 8080;
	private static final int PORT = 11337;

	/**
	 * Tests everything related to the {@link ServerLauncher} class.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	@Ignore
	// this test would require to shutdown the DS server, however, this then
	// shuts down the JVM... need to find a better solution
	public void testMain() throws IOException, InterruptedException {
		String[] argsEmpty = {};
		String[] argsNull = null;
		String[] argsStartCustomized = { "start", "port=" + PORT };
		String[] argsShutdownCustomized = { "shutdown", "port=" + PORT };
		String[] argsRootDir = { "rootDir=" };
		String[] argsUnordered = { "port=" + PORT, "start" };
		String[] argsHelp = { "-h" };

		// make sure the custom port is not used
		try {
			ServerLauncher.main(argsShutdownCustomized);
			Thread.sleep(SHUTDOWN_WAIT_DELAY);
		} catch (ClientHandlerException e) {
			LOGGER.debug("shutdown not necessary, no currently running service");
		} catch (InterruptedException e) {
			LOGGER.warn("interrupted sleep delay after shutdown!");
		}

		ServerLauncher.main(argsNull);
		Assert.assertTrue(portFree(DEFAULT_PORT));

		ServerLauncher.main(argsEmpty);
		Assert.assertTrue(portFree(DEFAULT_PORT));

		ServerLauncher.main(argsStartCustomized);
		Assert.assertFalse(portFree(PORT));

		ServerLauncher.main(argsShutdownCustomized);
		while (!portFree(PORT)) {
			Thread.sleep(50);
		}

		ServerLauncher.main(argsRootDir);

		ServerLauncher.main(argsUnordered);
		ServerLauncher.main(argsShutdownCustomized);
		while (!portFree(PORT)) {
			Thread.sleep(50);
		}

		// needs to be the last test, "help" always triggered always to true
		ServerLauncher.main(argsHelp);
	}

	private boolean portFree(int port) throws IOException {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			return false;
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
		return true;
	}

}
