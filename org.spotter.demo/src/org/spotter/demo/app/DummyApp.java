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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.spotter.demo.app.problems.Hiccups;
import org.spotter.demo.app.problems.OLB;

/**
 * Dummy Application.
 * 
 * @author Alexander Wert
 * 
 */
@Path("demo")
public class DummyApp {

	/**
	 * 
	 * @return hello string
	 */
	@GET
	@Path("testOLB")
	@Produces(MediaType.APPLICATION_JSON)
	public String testOLB() {
		OLB.getInstnace().olbMethod();
		return "Hello from OLB Test Method!";

	}

	/**
	 * 
	 * @return hello string
	 */
	@GET
	@Path("testHiccups")
	@Produces(MediaType.APPLICATION_JSON)
	public String testHiccups() {
		Hiccups.getInstnace().garbageHiccupWithNoise();
		return "Hello from Hiccup Test Method!";

	}

}
