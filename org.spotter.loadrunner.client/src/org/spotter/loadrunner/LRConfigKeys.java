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
package org.spotter.loadrunner;

/**
 * LoadRunner configuration keys.
 * @author Alexander Wert
 *
 */
public final class LRConfigKeys {
	/**
	 * Private constructor due to utility class.
	 */
	private LRConfigKeys() {
	}

	/**
	 * Server config for LR service.
	 */
	public static final String HOST = "org.spotter.workload.loadrunner.host";
	public static final String PORT = "org.spotter.workload.loadrunner.port";

	/**
	 * Executables.
	 */
	// (-> loadRunnerPath)
	public static final String CONTROLLER_EXE = "org.spotter.workload.loadrunner.controllerExe";
	// (-> analysisPath)
	public static final String ANALYSIS_EXE = "org.spotter.workload.loadrunner.analysisExe";

	/**
	 * Pre-configured files and names.
	 */
	public static final String SCENARIO_FILE = "org.spotter.workload.loadrunner.scenarioFile";
	public static final String ANALYSIS_TEMPLATE_NAME = "org.spotter.workload.loadrunner.analysis.templateName";
	public static final String ANALYSIS_SESSION_NAME = "org.spotter.workload.loadrunner.analysis.sessionName";
	public static final String RESULT_DIR = "org.spotter.workload.loadrunner.resultDir";

	/**
	 * Experiment configuration.
	 */

	public static final String EXPERIMENT_SCHEDULING_MODE = "org.spotter.workload.loadrunner.experiment.schedulingMode";
	public static final String EXPERIMENT_USER_INIT_MODE = "org.spotter.workload.loadrunner.experiment.vUserInitMode";
	// all times in seconds
	
}
