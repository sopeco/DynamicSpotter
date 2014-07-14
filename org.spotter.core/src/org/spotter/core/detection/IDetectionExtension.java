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

import org.lpe.common.extension.IExtension;

/**
 * Extension interface for all extensions.
 * @author Alexander Wert
 *
 */
public interface IDetectionExtension extends IExtension<IDetectionController> {
	/**
	 * Creates a new detection strategy provided by the extension.
	 * 
	 * @return returns an exploration strategy
	 */
	IDetectionController createExtensionArtifact();
}
