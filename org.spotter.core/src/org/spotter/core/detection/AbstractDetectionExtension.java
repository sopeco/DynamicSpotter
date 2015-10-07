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

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.extension.ReflectiveAbstractExtension;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.shared.configuration.ConfigKeys;

/**
 * Abstract class for all detection extensions.
 * 
 * @author Alexander Wert
 * 
 */
public abstract class AbstractDetectionExtension extends ReflectiveAbstractExtension implements IDetectionExtension {

	public static final String REUSE_EXPERIMENTS_FROM_PARENT = "reuseExperimentsFromParent";

	/**
	 * Constructor.
	 */
	public AbstractDetectionExtension(final Class<? extends IDetectionController> extensionArtifactClass) {
		super(extensionArtifactClass);
		addConfigParameter(createIsDetectableParameter());
		if (this.createExtensionArtifact() instanceof IExperimentReuser) {
			addConfigParameter(createReuseExperimentsParameter());
		}
		initializeConfigurationParameters();
	}

	/**
	 * This method is called to initialize heuristic specific configuration
	 * parameters.
	 */
	protected abstract void initializeConfigurationParameters();

	private ConfigParameterDescription createReuseExperimentsParameter() {
		final ConfigParameterDescription reuseExperimentsParameter = new ConfigParameterDescription(
				REUSE_EXPERIMENTS_FROM_PARENT, LpeSupportedTypes.Boolean);
		reuseExperimentsParameter.setDescription("Indicates whether the experiments from "
				+ "the parent heuristic should be used for this heuristic.");
		reuseExperimentsParameter.setDefaultValue(String.valueOf(false));
		return reuseExperimentsParameter;
	}

	private ConfigParameterDescription createIsDetectableParameter() {
		final ConfigParameterDescription nameParameter = new ConfigParameterDescription(ConfigKeys.DETECTABLE_KEY,
				LpeSupportedTypes.Boolean);
		nameParameter.setMandatory(true);
		nameParameter.setASet(false);
		nameParameter.setDefaultValue("true");
		nameParameter.setDescription("Specifies if this problem node is detectable!");

		return nameParameter;
	}

	/* (non-Javadoc)
	 * @see org.lpe.common.extension.ReflectiveAbstractExtension#getDisplayLabel()
	 */
	@Override
	public String getDisplayLabel() {
		return super.getDisplayLabel().replace("Detection", "").replace("Controller", "").trim();
	}
	
	
}
