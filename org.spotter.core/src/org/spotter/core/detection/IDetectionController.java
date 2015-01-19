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
package org.spotter.core.detection;

import java.util.Properties;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.lpe.common.extension.IExtensionArtifact;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.SpotterResult;

/**
 * It's the common interface for all detection controllers.
 * 
 * @author Alexander Wert
 * 
 */
public interface IDetectionController extends IExtensionArtifact {
	/**
	 * Analyzes the problem under investigation.
	 * 
	 * @return a {@link SpotterResult} instance indicating whether the problem
	 *         has been detected or not.
	 * @throws InstrumentationException
	 *             if code instrumentation failed
	 * @throws MeasurementException
	 *             if measurement failed
	 * @throws WorkloadException
	 *             if workload cannot be generated
	 */
	SpotterResult analyzeProblem() throws InstrumentationException, MeasurementException, WorkloadException;

	/**
	 * Sets the configuration for the problem detection.
	 * 
	 * @param problemDetectionConfiguration
	 *            configuration properties
	 */
	void setProblemDetectionConfiguration(Properties problemDetectionConfiguration);

	/**
	 * @return the problem detection configuration
	 */
	Properties getProblemDetectionConfiguration();

	/**
	 * 
	 * @param reuser
	 *            a detection heuristic which should reuser the experiments of
	 *            this heuristic
	 */
	void addExperimentReuser(IExperimentReuser reuser);

	/**
	 * Loads properties from hierarchy description.
	 */
	void loadProperties();

	/**
	 * Returns the result manager for this detection controller.
	 * 
	 * @return result manager
	 */
	DetectionResultManager getResultManager();

	/**
	 * @return the problemId
	 */
	String getProblemId();

	/**
	 * @param problemId
	 *            the problemId to set
	 */
	void setProblemId(String problemId);

	/**
	 * Returns the estimated duration of the experiment series conducted for the
	 * corresponding performance problem. unit: [seconds]
	 * 
	 * @return duration of experiments
	 */
	long getExperimentSeriesDuration();

	/**
	 * Triggers heuristic specific experiment execution.
	 * 
	 * @throws InstrumentationException
	 *             if instrumentation fails
	 * @throws MeasurementException
	 *             if measurement data cannot be collected
	 * @throws WorkloadException
	 *             if load cannot be generated properly
	 */
	void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException;
}
