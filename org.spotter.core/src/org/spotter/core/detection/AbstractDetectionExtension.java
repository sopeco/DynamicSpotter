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

import java.util.HashSet;
import java.util.Set;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.shared.configuration.ConfigKeys;

/**
 * Abstract class for all detection extensions.
 * 
 * @author Alexander Wert
 * 
 */
public abstract class AbstractDetectionExtension implements IDetectionExtension {

	public static final String REUSE_EXPERIMENTS_FROM_PARENT = "reuseExperimentsFromParent";

	private final Set<ConfigParameterDescription> configParameters;

	/**
	 * Constructor.
	 */
	public AbstractDetectionExtension() {

		configParameters = new HashSet<ConfigParameterDescription>();
		configParameters.add(createIsDetectableParameter());
		if (this.createExtensionArtifact() instanceof IExperimentReuser) {
			configParameters.add(createReuseExperimentsParameter());
		}
		initializeConfigurationParameters();
	}

	/**
	 * This method is called to initialize heuristic specific configuration
	 * parameters.
	 */
	protected abstract void initializeConfigurationParameters();

	/**
	 * Adds a configuration parameter to the extension.
	 * 
	 * @param parameter
	 *            parameter to add to this extension
	 */
	protected void addConfigParameter(ConfigParameterDescription parameter) {
		configParameters.add(parameter);
	}

	@Override
	public final Set<ConfigParameterDescription> getConfigParameters() {
		return configParameters;
	}

	private ConfigParameterDescription createReuseExperimentsParameter() {
		ConfigParameterDescription reuseExperimentsParameter = new ConfigParameterDescription(
				REUSE_EXPERIMENTS_FROM_PARENT, LpeSupportedTypes.Boolean);
		reuseExperimentsParameter.setDescription("Indicates whether the experiments from "
				+ "the parent heuristic should be used for this heuristic.");
		reuseExperimentsParameter.setDefaultValue(String.valueOf(false));
		return reuseExperimentsParameter;
	}

	private ConfigParameterDescription createIsDetectableParameter() {
		ConfigParameterDescription nameParameter = new ConfigParameterDescription(ConfigKeys.DETECTABLE_KEY,
				LpeSupportedTypes.Boolean);
		nameParameter.setMandatory(true);
		nameParameter.setASet(false);
		nameParameter.setDefaultValue("true");
		nameParameter.setDescription("Specifies if this problem node is detectable!");

		return nameParameter;
	}
}
