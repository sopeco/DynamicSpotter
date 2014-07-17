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
package org.spotter.detection.godclass;

import org.spotter.core.detection.AbstractDetectionExtension;
import org.spotter.core.detection.IDetectionController;

/**
 * God class anti pattern detection extension.
 * 
 * @author Alexander Wert
 * 
 */
public class GodClassExtension extends AbstractDetectionExtension {

	@Override
	public String getName() {
		return "GodClass";
	}

	@Override
	public IDetectionController createExtensionArtifact() {
		return new GodClassDetectionController(this);
	}

	@Override
	protected void initializeConfigurationParameters() {
		// TODO Auto-generated method stub

	}

}
