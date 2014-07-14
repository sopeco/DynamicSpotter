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
package org.spotter.demo.load;

import javax.ws.rs.core.MediaType;

import org.lpe.common.util.web.LpeWebUtils;
import org.spotter.workload.simple.ISimpleVUser;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * Simple virtual user script.
 * 
 * @author Alexander Wert
 * 
 */
public class VUser implements ISimpleVUser {

	private static final int THINK_TIME = 100;
	private static final int TIMEOUT = 120 * 1000;
	final WebResource webResource;

	/**
	 * Construcotr.
	 */
	public VUser() {
		Client client = LpeWebUtils.getWebClient();
		client.setConnectTimeout(TIMEOUT);
		client.setReadTimeout(TIMEOUT);
		webResource = client.resource("http://localhost:8089/");
	}

	@Override
	public void executeIteration() {
		try {
			webResource.path("demo").path("testOLB").accept(MediaType.APPLICATION_JSON).get(String.class);

			Thread.sleep(THINK_TIME);
		} catch (Throwable e) {
			// ignoring exception
			e.printStackTrace();
		}
	}

}
