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
package org.spotter.service.rest;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.extension.ExtensionRegistry;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.LpeFileUtils;
import org.spotter.service.rest.dummy.DummyWorkloadExtension;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.configuration.SpotterExtensionType;
import org.spotter.shared.service.ResponseStatus;
import org.spotter.shared.service.SpotterServiceResponse;
import org.spotter.shared.status.SpotterProgress;

/**
 * Test for the {@link SpotterService} class.
 * 
 * @author Peter Merkert
 */
public class SpotterServiceTest {

	private static SpotterService ss;

	private static File tempDir;
	
	/**
	 * Initializes the {@link SpotterService} and fetches the status at startup.
	 * 
	 * @throws IOException when creating temp dir fails
	 */
	@BeforeClass
	public static void initialize() throws IOException {
		ss = new SpotterService();
		creeateTempDir();
		initGlobalConfigs(tempDir.getAbsolutePath());
		testIsRunningBefore();
	}
	
	/**
	 * Removes the temp dir.
	 * 
	 * @throws IOException removal of temp dir fails
	 */
	@AfterClass
	public static void cleanUp() throws IOException {
		LpeFileUtils.removeDir(tempDir.getAbsolutePath());
	}
	
	/**
	 * DS should not run any experiments now.
	 */
	private static void testIsRunningBefore() {
		SpotterServiceResponse<Boolean> r = ss.isRunning();
		Assert.assertEquals(ResponseStatus.OK, r.getStatus());
		Assert.assertEquals(false, r.getPayload().booleanValue());
	}

	/**
	 * Tests the {@link SpotterService#startDiagnosis(String)} method.
	 * 
	 * @throws IOException execution of {@link SpotterService} failed
	 */
	@Test
	public void testStartDiagnosis() throws IOException {
		SpotterServiceResponse<Long> rl = ss.startDiagnosis("");
		Assert.assertEquals(rl.getStatus(), ResponseStatus.OK);
		
		// we want to force a concurrent job execution failure
		rl = ss.startDiagnosis("");
		Assert.assertEquals(ResponseStatus.INVALID_STATE, rl.getStatus());
		Assert.assertEquals(0L, rl.getPayload().longValue());
	}

	
	/**
	 * Tests the {@link SpotterService#isRunning()} method.
	 */
	@Test
	public void testIsRunning() {
		SpotterServiceResponse<Boolean> rb;
		
		// wait till the last run finished
		while (ss.isRunning().getPayload()) {
			rb = null; // to avoid checkstyle error
		}
		
		rb = ss.isRunning();
		Assert.assertEquals(ResponseStatus.OK, rb.getStatus());
		Assert.assertEquals(false, rb.getPayload().booleanValue());
	}
	
	/**
	 * Tests the {@link SpotterService#getConfigurationParameters()} method.
	 */
	@Test
	public void testGetConfigurationParameters() {
		SpotterServiceResponse<Set<ConfigParameterDescription>> rcpd = ss.getConfigurationParameters();
		Assert.assertEquals(ResponseStatus.OK, rcpd.getStatus());
	}	
	
	/**
	 * Tests the {@link SpotterService#getAvailableExtensions(String)} method.
	 * 
	 * @throws IOException when temporar dir could not be created
	 */
	@Test
	public void testGetAvailableExtensions() throws IOException {
		registerExtension();
		
		SpotterServiceResponse<Set<String>> rss = ss.getAvailableExtensions("myextension");
		Assert.assertEquals(ResponseStatus.SERVER_ERROR, rss.getStatus());
		
		for (SpotterExtensionType set : SpotterExtensionType.values()) {
			rss = ss.getAvailableExtensions(set.toString());
			
			// we have loaded a workload extension
			if (set.equals(SpotterExtensionType.WORKLOAD_EXTENSION)) {
				
				Assert.assertEquals(ResponseStatus.OK, rss.getStatus());
				Assert.assertEquals(1, rss.getPayload().size());
				
			} else {
			
				Assert.assertEquals(ResponseStatus.OK, rss.getStatus());
				Assert.assertEquals(0, rss.getPayload().size());
				
			}
			
		}
	}
	
	private static void creeateTempDir() throws IOException {
		tempDir = new File("tempJUnit");
		if (tempDir.exists()) {
			LpeFileUtils.removeDir(tempDir.getAbsolutePath());
		}
		LpeFileUtils.createDir(tempDir.getAbsolutePath());
	}
	
	private static void initGlobalConfigs(String baseDir) {
		Properties properties = new Properties();
		properties.setProperty("org.lpe.common.extension.appRootDir", "C:\\Users\\D061465\\git\\DynamicSpotter\\org.spotter.service");
		properties.setProperty("org.spotter.conf.pluginDirNames", "plugins");
		properties.setProperty(ConfigKeys.RESULT_DIR, baseDir + System.getProperty("file.separator"));
		properties.setProperty(ConfigKeys.EXPERIMENT_DURATION, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH, "1");
		properties.setProperty(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL, "1");
		properties.setProperty(ConfigKeys.WORKLOAD_MAXUSERS, "10");
		GlobalConfiguration.initialize(properties);
	}
	
	private void registerExtension() {
		IExtension<?> ext = new DummyWorkloadExtension();
		ExtensionRegistry.getSingleton().addExtension(ext);
	}

	/**
	 * Tests the {@link SpotterService#getExtensionConfigParamters(String)} method.
	 */
	@Test
	public void testGetExtensionConfigParamters() {
		SpotterServiceResponse<Set<ConfigParameterDescription>> rscpd = ss.getExtensionConfigParamters("DummyWorkload");
		// not extension with the name loaded when testing
		Assert.assertEquals(ResponseStatus.OK, rscpd.getStatus());
	}
	
	/**
	 * Tests the {@link SpotterService#getCurrentProgressReport()} method.
	 */
	@Test
	public void testGetCurrentProgressReport() {
		SpotterServiceResponse<SpotterProgress> rsp = ss.getCurrentProgressReport();
		Assert.assertEquals(ResponseStatus.OK, rsp.getStatus());
	}
	
	/**
	 * Tests the {@link SpotterService#getCurrentJobId()} method.
	 */
	@Test
	public void testGetCurrentJobId() {
		SpotterServiceResponse<Long> rl = ss.getCurrentJobId();
		Assert.assertEquals(ResponseStatus.OK, rl.getStatus());
	}
	
	/**
	 * Tests the {@link SpotterService#testConnection()} method.
	 */
	@Test
	public void testTestConnection() {
		SpotterServiceResponse<Boolean> rb = ss.testConnection();
		Assert.assertEquals(ResponseStatus.OK, rb.getStatus());
		Assert.assertEquals(true, rb.getPayload().booleanValue());
	}
	
	/**
	 * Tests the {@link SpotterService#testConnectionToSattelite(String, String, String)} method.
	 */
	@Test
	public void testTestConnectionToSattelite() {
		SpotterServiceResponse<Boolean> rb = ss.testConnectionToSattelite("DummyWorkload", null, null);
		Assert.assertEquals(ResponseStatus.OK, rb.getStatus());
		Assert.assertEquals(true, rb.getPayload().booleanValue());
	}
}
