/**
 * Copyright 2014 SAP AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.spotter.jmeter;

/**
 * JMeter configuration keys.
 */
public final class JMeterConfigKeys {

	/**
	 * Private constructor due to utility class.
	 */
	private JMeterConfigKeys() {
	}

	public static final String JMETER_HOME = "org.spotter.workload.jmeter.home";

	public static final String SCENARIO_FILE = "org.spotter.workload.jmeter.scenarioFile";

	public static final String SAMPLING_FILE = "org.spotter.workload.jmeter.samplingFile";

	public static final String LOG_FILE_FLAG = "org.spotter.workload.jmeter.logFileFlag";

	public static final String LOG_FILE_PREFIX = "org.spotter.workload.jmeter.logFilePrefix";

	public static final String THINK_TIME_MIN = "org.spotter.workload.jmeter.thinkTimeMin";

	public static final String THINK_TIME_MAX = "org.spotter.workload.jmeter.thinkTimeMax";

}
