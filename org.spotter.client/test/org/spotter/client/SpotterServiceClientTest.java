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

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.extension.ExtensionRegistry;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.LpeFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.client.dummy.DummyWorkloadExtension;
import org.spotter.service.ServerLauncher;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.configuration.SpotterExtensionType;
import org.spotter.shared.status.SpotterProgress;

import com.sun.jersey.api.client.ClientHandlerException;

import junit.framework.Assert;

public class SpotterServiceClientTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpotterServiceClientTest.class);
	
	private static final int SHUTDOWN_WAIT_DELAY = 5000;
	
	private static final String host = "localhost";
	private static final String port = "11337";
	
	private SpotterServiceClient ssc;
	
	private static File tempDir;
	
	@BeforeClass
	public static void initialize() throws IOException {
		createTempDir();
		initGlobalConfigs(tempDir.getAbsolutePath());
		
		// make sure the custom port is not used
		try {
			final String[] argsShutdownCustomized = { "shutdown", "port=" + port };
			ServerLauncher.main(argsShutdownCustomized);
			Thread.sleep(SHUTDOWN_WAIT_DELAY);
		} catch (final ClientHandlerException e) {
			LOGGER.debug("shutdown not necessary, no currently running service!");
		} catch (final InterruptedException e) {
			LOGGER.warn("interrupted sleep delay after shutdown!");
		}
		
		startServer();
	}

	@AfterClass
	public static void cleanUp() throws IOException {
		if (tempDir.exists()) {
			LpeFileUtils.removeDir(tempDir.getAbsolutePath());
		}
	}
	
	@Before
	public void initilze() {
		ssc = new SpotterServiceClient(host, port);
	}
	
	private static void startServer() {
		final String[] argsStartCustomized = {"start", "port=" + port };
		ServerLauncher.main(argsStartCustomized);
	}
	
	@Test
	public void testSpotterServiceClient() {
		ssc.updateUrl(host, "");
		ssc.updateUrl(host, port);
	}
	
	@Test
	public void testIsRunningOnline() {
		final boolean status = ssc.isRunning();
		Assert.assertEquals(false, status);
	}

	@Test
	public void testGetLastRunException() {
		final Exception exception = ssc.getLastRunException();
		Assert.assertNull(exception);
	}
	
	@Test
	public void testGetAvailableExtensions() {
		registerExtension();

		final Set<String> set = ssc.getAvailableExtensions(SpotterExtensionType.WORKLOAD_EXTENSION);
		Assert.assertEquals(1, set.size()); // we have one workload extension registered
		
		removeExtension();
	}
	
	@Test
	public void testGetConfigurationParameters() {
		registerExtension();

		final Set<ConfigParameterDescription> cpd = ssc.getConfigurationParameters();
		Assert.assertEquals(true, cpd.size() > 0);
		
		removeExtension();
	}
	
	@Test
	public void testGetExtensionConfigurationParameters() {
		registerExtension();

		final Set<ConfigParameterDescription> cpd = ssc.getExtensionConfigParamters("org.spotter.client.dummy.DummyWorkload");
		Assert.assertEquals(true, cpd.size() > 0);
		
		removeExtension();
	}

	@Test
	public void testGetCurrentJobId() {
		final long currentJobId = ssc.getCurrentJobId();
		// no job is currently running
		Assert.assertEquals(currentJobId, 0);
	}
	
	@Test
	public void testGetCurrentProgressReport() {
		final SpotterProgress sp = ssc.getCurrentProgressReport();
		Assert.assertEquals(0, sp.getProblemProgressMapping().size());
	}
	
	@Test
	public void testConnectionToSattelite() {
		registerExtension();
		
		final boolean status = ssc.testConnectionToSattelite("org.spotter.client.dummy.DummyWorkload", "localhost", "8080");
		Assert.assertEquals(true, status);
		
		removeExtension();
	}
	
	@Test
	public void testConnection() {
		final boolean status = ssc.testConnection();
		Assert.assertEquals(true, status);
	}
	
	private static void createTempDir() throws IOException {
		tempDir = new File("tempJUnit");
		if (tempDir.exists()) {
			LpeFileUtils.removeDir(tempDir.getAbsolutePath());
		}
		LpeFileUtils.createDir(tempDir.getAbsolutePath());
	}
	
	private static void initGlobalConfigs(final String baseDir) {
		final Properties properties = new Properties();
		properties.setProperty(ExtensionRegistry.APP_ROOT_DIR_PROPERTY_KEY, tempDir.getAbsolutePath());
		properties.setProperty(ExtensionRegistry.PLUGINS_FOLDER_PROPERTY_KEY, "plugins");
		properties.setProperty("org.spotter.conf.pluginDirNames", "plugins");
		properties.setProperty(ConfigKeys.RESULT_DIR, baseDir + System.getProperty("file.separator") + "results");
		properties.setProperty(ConfigKeys.EXPERIMENT_DURATION, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL, "1");
		properties.setProperty(ConfigKeys.WORKLOAD_MAXUSERS, "10");
		GlobalConfiguration.initialize(properties);
	}
	
	private void registerExtension() {
		final IExtension ext = new DummyWorkloadExtension();
		ExtensionRegistry.getSingleton().addExtension(ext);
	}
	
	private void removeExtension() {
		final IExtension ext = new DummyWorkloadExtension();
		ExtensionRegistry.getSingleton().removeExtension(ext.getName());
	}
	
}
