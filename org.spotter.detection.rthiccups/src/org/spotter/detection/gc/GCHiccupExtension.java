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
package org.spotter.detection.gc;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.detection.IDetectionController;
import org.spotter.detection.hiccup.utils.HiccupDetectionConfig;

/**
 * Garbage Collection Overloading detection extension.
 * 
 * @author Alexander Wert
 * 
 */
public class GCHiccupExtension extends AbstractDetectionExtension {

	protected static final String GC_SAMPLING_DELAY_KEY = "gcStatsSamplingDelay";
	protected static final String GC_CPU_OVERLOAD_THRESHOLD_KEY = "gcOverloadThreshold";
	protected static final String GC_GUILTY_GC_RATIO_THRESHOLD_KEY = "guiltyFullGCRatioThreshold";
	protected static final String HICCUP_GC_CAUSE_PERCENTAGE_THRESHOLD_KEY = "hiccupGCCausePercentageTHreshold";
	protected static final String MIN_HICCUP_INTERVAL_KEY = "minHiccupInterval";
	protected static final String HICCUP_INTERVAL_FACTOR_KEY = "hiccupIntervalFactor";

	protected static final long GC_SAMPLING_DELAY_DEFAULT = 500;
	protected static final double GC_CPU_OVERLOAD_THRESHOLD_DEFAULT = 0.4;
	protected static final double GC_GUILTY_GC_RATIO_THRESHOLD_DEFAULT = 0.7;
	protected static final double HICCUP_GC_CAUSE_PERCENTAGE_THRESHOLD_DEFAULT = 0.7;
	protected static final long MIN_HICCUP_INTERVAL_DEFAULT = 1000; // [ms]
	protected static final double HICCUP_INTERVAL_FACTOR_DEFAULT = 0.5;

	@Override
	public String getName() {
		return "GCHiccups";
	}

	@Override
	public IDetectionController createExtensionArtifact() {
		return new GCHiccupDetectionController(this);
	}

	@Override
	protected void initializeConfigurationParameters() {
		ConfigParameterDescription gcSamplingDelayParameter = new ConfigParameterDescription(GC_SAMPLING_DELAY_KEY,
				LpeSupportedTypes.Long);
		gcSamplingDelayParameter.setDescription("Defines the sampling delay in milliseconds for "
				+ "sampling garbage collections statistics.");
		gcSamplingDelayParameter.setDefaultValue(String.valueOf(GC_SAMPLING_DELAY_DEFAULT));

		addConfigParameter(gcSamplingDelayParameter);

		ConfigParameterDescription gcOverloadThreshold = new ConfigParameterDescription(GC_CPU_OVERLOAD_THRESHOLD_KEY,
				LpeSupportedTypes.Double);
		gcOverloadThreshold.setDescription("Defines the critical threshold for the ratio between "
				+ "the time consumed by garbage collection and the elapsed time.");
		gcOverloadThreshold.setDefaultValue(String.valueOf(GC_CPU_OVERLOAD_THRESHOLD_DEFAULT));

		addConfigParameter(gcOverloadThreshold);

		ConfigParameterDescription guiltyFullGCRatioThreshold = new ConfigParameterDescription(
				GC_GUILTY_GC_RATIO_THRESHOLD_KEY, LpeSupportedTypes.Double);
		guiltyFullGCRatioThreshold.setDescription("Defines the critical threshold for the proportion "
				+ "of full garbage collections causing hiccups.");
		guiltyFullGCRatioThreshold.setDefaultValue(String.valueOf(GC_GUILTY_GC_RATIO_THRESHOLD_DEFAULT));

		addConfigParameter(guiltyFullGCRatioThreshold);

		ConfigParameterDescription hiccupGCCausePercentageTHreshold = new ConfigParameterDescription(
				HICCUP_GC_CAUSE_PERCENTAGE_THRESHOLD_KEY, LpeSupportedTypes.Double);
		hiccupGCCausePercentageTHreshold.setDescription("Defines the critical threshold for proportion all "
				+ "hiccups to be caused by full garbage collections.");
		hiccupGCCausePercentageTHreshold.setDefaultValue(String.valueOf(HICCUP_GC_CAUSE_PERCENTAGE_THRESHOLD_DEFAULT));

		addConfigParameter(hiccupGCCausePercentageTHreshold);

		ConfigParameterDescription minHiccupInterval = new ConfigParameterDescription(MIN_HICCUP_INTERVAL_KEY,
				LpeSupportedTypes.Long);
		minHiccupInterval.setDescription("Defines the minimum time in milliseconds to add to the hiccup interval "
				+ "to decide whether a garbage collection caused the corresponding hiccup.");
		minHiccupInterval.setDefaultValue(String.valueOf(MIN_HICCUP_INTERVAL_DEFAULT));

		addConfigParameter(minHiccupInterval);

		ConfigParameterDescription hiccupIntervalFactor = new ConfigParameterDescription(HICCUP_INTERVAL_FACTOR_KEY,
				LpeSupportedTypes.Double);
		hiccupIntervalFactor
				.setDescription("Defines the factor of the hiccup width to add on both sides of the interval in order "
						+ "to decide whether a garbage collection caused the corresponding hiccup.");
		hiccupIntervalFactor.setDefaultValue(String.valueOf(HICCUP_INTERVAL_FACTOR_DEFAULT));

		addConfigParameter(hiccupIntervalFactor);
		for (ConfigParameterDescription cpd : HiccupDetectionConfig.getConfigurationParameters()) {
			addConfigParameter(cpd);
		}
	}

}
