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

import org.junit.Test;

/**
 * Tests the {@link ServerLauncher} class.
 * 
 * @author Peter Merkert
 */
public class ServerLauncherTest {
	
	/**
	 * Tests everything related to the {@link ServerLauncher} class.
	 */
	@Test
	public void testMain() {
		String[] argsEmpty = {};
		String[] argsNull = null;
		String[] argsStartCustomized = { "start", "port=11337" };
		String[] argsShutdownCustomized = { "shutdown", "port=11337" };
		String[] argsRootDir = { "rootDir=" };
		String[] argsUnordered = { "port=11337", "start" };
		String[] argsHelp = { "-h" };

		ServerLauncher.main(argsNull);
		
		ServerLauncher.main(argsEmpty);
		
		ServerLauncher.main(argsStartCustomized);
		
		ServerLauncher.main(argsShutdownCustomized);

		ServerLauncher.main(argsRootDir);

		ServerLauncher.main(argsUnordered);
		
		// needs to be the last test, because "help" is triggered always to true!
		ServerLauncher.main(argsHelp);
	}

}
