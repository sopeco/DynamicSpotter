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
package org.spotter.instrumentation;

import org.aim.api.instrumentation.description.Restrictions;
import org.aim.artifacts.instrumentation.InstrumentationClient;
import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.instrumentation.AbstractInstrumentationExtension;
import org.spotter.core.instrumentation.ISpotterInstrumentation;

/**
 * Extension for dynamic instrumentation.
 * 
 * @author Alexander Wert
 * 
 */
public class DynamicInstrumentationClientExtension extends AbstractInstrumentationExtension {

	private static final String EXTENSION_DESCRIPTION = "The default instrumentation satellite adapter can be used to "
														+ "connect to every instrumentation satellite which is not "
														+ "covered by other instrumentation satellite adapters.";
	
	@Override
	public String getName() {
		return "instrumentation.satellite.adapter.default";
	}

	private ConfigParameterDescription createPackagesToIncludeParameter() {
		ConfigParameterDescription packagesToIncludeParameter = new ConfigParameterDescription(
				ISpotterInstrumentation.INSTRUMENTATION_INCLUDES, LpeSupportedTypes.String);
		packagesToIncludeParameter.setAset(true);
		packagesToIncludeParameter.setDefaultValue("");
		packagesToIncludeParameter
				.setDescription("This parameter specifies the java packages whose classes should be considered for instrumentation. "
						+ "Class which are not in these packages will not be instrumented.");

		return packagesToIncludeParameter;
	}

	private ConfigParameterDescription createPackagesToExcludeParameter() {
		ConfigParameterDescription packagesToExcludeParameter = new ConfigParameterDescription(
				ISpotterInstrumentation.INSTRUMENTATION_EXCLUDES, LpeSupportedTypes.String);
		packagesToExcludeParameter.setAset(true);
		packagesToExcludeParameter.setDefaultValue(Restrictions.EXCLUDE_JAVA + "," + Restrictions.EXCLUDE_JAVASSIST
				+ "," + Restrictions.EXCLUDE_JAVAX + "," + Restrictions.EXCLUDE_LPE_COMMON);
		packagesToExcludeParameter
				.setDescription("This parameter specifies the java packages whose classes should NOT be considered for instrumentation. "
						+ "Class which are in these packages will not be instrumented.");

		return packagesToExcludeParameter;
	}

	@Override
	protected void initializeConfigurationParameters() {
		addConfigParameter(createPackagesToIncludeParameter());
		addConfigParameter(createPackagesToExcludeParameter());
		addConfigParameter(ConfigParameterDescription.createExtensionDescription(EXTENSION_DESCRIPTION));
	}

	@Override
	public ISpotterInstrumentation createExtensionArtifact() {
		return new DynamicInstrumentationClient(this);
	}

	@Override
	public boolean testConnection(String host, String port) {
		return InstrumentationClient.testConnection(host, port);
	}

	@Override
	public boolean isRemoteExtension() {
		return true;
	}

}
