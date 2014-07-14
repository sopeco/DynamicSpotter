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
package org.spotter.detection.gcoverloading;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.detection.IDetectionController;

/**
 * Garbage Collection Overloading detection extension.
 * 
 * @author Alexander Wert
 * 
 */
public class GCOverloadingExtension extends AbstractDetectionExtension {

	protected static final String GC_SAMPLING_DELAY_KEY = "gcStatsSamplingDelay";

	protected static final long GC_SAMPLING_DELAY_DEFAULT = 500;

	@Override
	public String getName() {
		return "GCOverloading";
	}

	@Override
	public IDetectionController createExtensionArtifact() {
		return new GCOverloadingDetectionController(this);
	}

	@Override
	protected void initializeConfigurationParameters() {
		ConfigParameterDescription gcSamplingDelayParameter = new ConfigParameterDescription(GC_SAMPLING_DELAY_KEY,
				LpeSupportedTypes.Long);
		gcSamplingDelayParameter
				.setDescription("Defines the sampling delay in milliseconds for sampling garbage collections statistics.");
		gcSamplingDelayParameter.setDefaultValue(String.valueOf(GC_SAMPLING_DELAY_DEFAULT));

		addConfigParameter(gcSamplingDelayParameter);

	}

}
