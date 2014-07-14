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
package org.spotter.eclipse.ui.providers;

import org.eclipse.swt.graphics.Image;
import org.spotter.eclipse.ui.model.ExtensionItem;

/**
 * Image provider used by {@link SpotterExtensionsLabelProvider} to get images
 * for the extension items.
 */
public class SpotterExtensionsImageProvider {

	/**
	 * Returns the image for the given extension item.
	 * 
	 * @param item
	 *            the item the image is requested for
	 * @return the image for the given extension item
	 */
	public Image getImage(ExtensionItem item) {
		return item.getImage();
	}

}
