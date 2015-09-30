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
package org.spotter.core.test.dummies.satellites;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.extension.IExtensionArtifact;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.workload.AbstractWorkloadExtension;

public class DummyWorkloadExtension extends AbstractWorkloadExtension {

	@Override
	public String getName() {
		return "DummyWorkload";
	}

	@SuppressWarnings("unchecked")
	@Override
	public <EA extends IExtensionArtifact> EA createExtensionArtifact(final String... patterns) {
		return (EA) new DummyWorkload(this);
	}

	@Override
	protected void initializeConfigurationParameters() {
		final ConfigParameterDescription parDescription = new ConfigParameterDescription("test.workload.parameter",
				LpeSupportedTypes.Integer);
		addConfigParameter(parDescription);
	}

	@Override
	public boolean testConnection(final String host, final String port) {
		return true;
	}

	@Override
	public boolean isRemoteExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
