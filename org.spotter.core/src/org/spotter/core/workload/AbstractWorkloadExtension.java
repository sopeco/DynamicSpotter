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
package org.spotter.core.workload;

import java.util.Set;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.extension.IExtension;
import org.lpe.common.extension.IExtensionArtifact;
import org.spotter.core.AbstractSpotterSatelliteExtension;

/**
 * Wrapper abstract class for workload extensions.
 * 
 * @author Alexander Wert
 * 
 */
public abstract class AbstractWorkloadExtension extends AbstractSpotterSatelliteExtension implements
		IExtension {
	
	public AbstractWorkloadExtension(final Class<? extends IExtensionArtifact> extensionArtifactClass) {
		super(extensionArtifactClass);
	}

	@Override
	public final Set<ConfigParameterDescription> getConfigParameters() {
		return configParameters;
	}
}
