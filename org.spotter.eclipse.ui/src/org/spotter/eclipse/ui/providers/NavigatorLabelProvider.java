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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.spotter.eclipse.ui.navigator.ISpotterProjectElement;

/**
 * Label provider for items of DynamicSpotter Project Navigator. This provider
 * is used by the DynamicSpotter Project Navigator.
 * 
 * @author Denis Knoepfle
 * 
 */
public class NavigatorLabelProvider extends LabelProvider {

	@Override
	public Image getImage(Object element) {
		Image image = null;

		if (ISpotterProjectElement.class.isInstance(element)) {
			image = ((ISpotterProjectElement) element).getImage();
		}
		// else ignore the element

		return image;
	}

	@Override
	public String getText(Object element) {
		String text = ""; // $NON-NLS-1$
		if (ISpotterProjectElement.class.isInstance(element)) {
			text = ((ISpotterProjectElement) element).getText();
		}
		// else ignore the element

		return text;
	}

}
