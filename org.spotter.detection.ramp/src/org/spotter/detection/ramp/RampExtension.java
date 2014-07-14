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
package org.spotter.detection.ramp;

import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.detection.IDetectionController;

/**
 * The ramp antipattern detection extension.
 * 
 * @author Alexander Wert
 */
public class RampExtension extends AbstractDetectionExtension {

	protected static final String KEY_WARMUP_PHASE_DURATION = "warmupPhaseDuration";
	protected static final String KEY_REQUIRED_SIGNIFICANT_STEPS = "numRequiredSignificantSteps";
	protected static final String KEY_REQUIRED_SIGNIFICANCE_LEVEL = "requiredSignificanceLevel";
	protected static final String KEY_CPU_UTILIZATION_THRESHOLD = "maxCpuUtilization";
	protected static final String KEY_EXPERIMENT_STEPS = "numExperiments";
	
	protected static final int STIMULATION_PHASE_DURATION_DEFAULT = 30; //[Sec]
	protected static final int EXPERIMENT_STEPS_DEFAULT = 3;
	protected static final double REQUIRED_SIGNIFICANCE_LEVEL_DEFAULT = 0.05; //[0-1] (percentage)
	protected static final int REQUIRED_SIGNIFICANT_STEPS_DEFAULT = 2;
	protected static final double MAX_CPU_UTILIZATION_DEFAULT = 0.9; //[0-1] (percentage)
	
	@Override
	public String getName() {
		return "Ramp";
	}

	@Override
	public IDetectionController createExtensionArtifact() {
		return new RampDetectionController(this);
	}

	@Override
	protected void initializeConfigurationParameters() {
		// TODO Auto-generated method stub

	}
}
