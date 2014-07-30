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

import java.io.IOException;
import java.util.Set;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.lpe.common.config.ConfigParameterDescription;
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
	
	/**
	 * Initializes the {@link SpotterService} and fetches the status at startup.
	 */
	@BeforeClass
	public static void initialize() {
		ss = new SpotterService();
		
		testIsRunningBefore();
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
	 */
	@Test
	public void testGetAvailableExtensions() {
		for (SpotterExtensionType set : SpotterExtensionType.values()) {
			SpotterServiceResponse<Set<String>> rss = ss.getAvailableExtensions(set.toString());
			// no extension loaded when testing
			Assert.assertEquals(ResponseStatus.SERVER_ERROR, rss.getStatus());
		}
	}
	
	/**
	 * Tests the {@link SpotterService#getExtensionConfigParamters(String)} method.
	 */
	@Test
	public void testGetExtensionConfigParamters() {
		SpotterServiceResponse<Set<ConfigParameterDescription>> rscpd = ss.getExtensionConfigParamters("");
		// not extension with the name loaded when testing
		Assert.assertEquals(ResponseStatus.SERVER_ERROR, rscpd.getStatus());
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
}
