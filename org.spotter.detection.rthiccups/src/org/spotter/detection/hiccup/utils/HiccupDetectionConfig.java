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
package org.spotter.detection.hiccup.utils;

import java.util.HashSet;
import java.util.Set;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;

/**
 * Configuration for hiccup detection.
 * 
 * @author Alexander Wert
 * 
 */
public class HiccupDetectionConfig {
	public static final String OUTLIER_DEVIATION_FACTOR_KEY = "outlierFactor";
	public static final String MIN_DEVIATION_FROM_MEAN_FACTOR_KEY = "minDeviationFromMeanFactor";
	public static final String INTER_HICCUP_TIME_KEY = "interHiccupTime";
	public static final String MOVING_AVERAGE_WINDOW_SIZE_KEY = "mvaWindowSize";

	public static final double OUTLIER_DEVIATION_FACTOR_DEFAULT = 3.0;
	public static final double MIN_DEVIATION_FROM_MEAN_FACTOR_DEFAULT = 0.1;
	public static final long INTER_HICCUP_TIME_DEFAULT = 5000; // [ms]
	public static final int MOVING_AVERAGE_WINDOW_SIZE_DEFAULT = 11;

	private double outlierDeviationFactor;
	private double minDeviationFromMeanFactor;
	private long interHiccupThreshold;
	private int mvaWindowSize; // should be an odd number

	/**
	 * @return the outlierDeviationFactor
	 */
	public double getOutlierDeviationFactor() {
		return outlierDeviationFactor;
	}

	/**
	 * @param outlierDeviationFactor
	 *            the outlierDeviationFactor to set
	 */
	public void setOutlierDeviationFactor(double outlierDeviationFactor) {
		this.outlierDeviationFactor = outlierDeviationFactor;
	}

	/**
	 * @return the minDeviationFromMeanFactor
	 */
	public double getMinDeviationFromMeanFactor() {
		return minDeviationFromMeanFactor;
	}

	/**
	 * @param minDeviationFromMeanFactor
	 *            the minDeviationFromMeanFactor to set
	 */
	public void setMinDeviationFromMeanFactor(double minDeviationFromMeanFactor) {
		this.minDeviationFromMeanFactor = minDeviationFromMeanFactor;
	}

	/**
	 * @return the interHiccupThreshold
	 */
	public long getInterHiccupThreshold() {
		return interHiccupThreshold;
	}

	/**
	 * @param interHiccupThreshold
	 *            the interHiccupThreshold to set
	 */
	public void setInterHiccupThreshold(long interHiccupThreshold) {
		this.interHiccupThreshold = interHiccupThreshold;
	}

	/**
	 * @return the mvaWindowSize
	 */
	public int getMvaWindowSize() {
		return mvaWindowSize;
	}

	/**
	 * @param mvaWindowSize
	 *            the mvaWindowSize to set
	 */
	public void setMvaWindowSize(int mvaWindowSize) {
		this.mvaWindowSize = mvaWindowSize;
	}

	/**
	 * 
	 * @return set of configuration parameters for hiccup detection
	 */
	public static  Set<ConfigParameterDescription> getConfigurationParameters() {
		ConfigParameterDescription outlierDeviationFactor = new ConfigParameterDescription(
				OUTLIER_DEVIATION_FACTOR_KEY, LpeSupportedTypes.Double);
		outlierDeviationFactor.setDescription("This factor describes how much response times need to deviate "
				+ "from the mean value in order to be detected as hiccups. "
				+ "A hiccup is detected if a response time exceeds the following "
				+ "value: mean + standardDeviation * " + OUTLIER_DEVIATION_FACTOR_KEY + ".");
		outlierDeviationFactor.setDefaultValue(String.valueOf(OUTLIER_DEVIATION_FACTOR_DEFAULT));

		ConfigParameterDescription minDeviationFactor = new ConfigParameterDescription(
				MIN_DEVIATION_FROM_MEAN_FACTOR_KEY, LpeSupportedTypes.Double);
		minDeviationFactor
				.setDescription("Specifies the minimal deviation from mean (in percent) in order to be identify a hiccup.");
		minDeviationFactor.setDefaultValue(String.valueOf(MIN_DEVIATION_FROM_MEAN_FACTOR_DEFAULT));

		ConfigParameterDescription interHiccupTimeParameter = new ConfigParameterDescription(INTER_HICCUP_TIME_KEY,
				LpeSupportedTypes.Long);
		interHiccupTimeParameter.setDescription("Defines the threshold for the minimal time between two "
				+ "hiccups to be considered as different hiccups.");
		interHiccupTimeParameter.setDefaultValue(String.valueOf(INTER_HICCUP_TIME_DEFAULT));

		ConfigParameterDescription mvaWindowSizeParameter = new ConfigParameterDescription(
				MOVING_AVERAGE_WINDOW_SIZE_KEY, LpeSupportedTypes.Integer);
		mvaWindowSizeParameter.setDescription("Defines the window size for calculating "
				+ "the moving average on a response time series.");
		mvaWindowSizeParameter.setDefaultValue(String.valueOf(MOVING_AVERAGE_WINDOW_SIZE_DEFAULT));

		Set<ConfigParameterDescription> set = new HashSet<>();
		set.add(outlierDeviationFactor);
		set.add(minDeviationFactor);
		set.add(interHiccupTimeParameter);
		set.add(mvaWindowSizeParameter);
		return set;
	}

}
