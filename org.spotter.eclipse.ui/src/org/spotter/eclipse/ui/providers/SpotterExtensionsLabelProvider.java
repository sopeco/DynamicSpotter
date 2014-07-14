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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.spotter.eclipse.ui.model.ExtensionItem;

/**
 * Label provider for extension items that can be used e.g. in a
 * <code>TableViewer</code>. This label provider expects objects of type
 * {@link ExtensionItem}.
 */
public class SpotterExtensionsLabelProvider extends CellLabelProvider {

	private SpotterExtensionsImageProvider imageProvider;

	public SpotterExtensionsLabelProvider() {
		this.imageProvider = new SpotterExtensionsImageProvider();
	}

	/**
	 * Sets the image provider used by this label provider.
	 * 
	 * @param imageProvider
	 *            The image provider to use. Must not be <code>null</code>.
	 */
	public void setImageProvider(SpotterExtensionsImageProvider imageProvider) {
		if (imageProvider == null) {
			throw new IllegalArgumentException("The image provider must not be null!");
		}
		this.imageProvider = imageProvider;
	}

	@Override
	public String getToolTipText(Object element) {
		String tooltip = null;
		if (element instanceof ExtensionItem) {
			tooltip = ((ExtensionItem) element).getToolTip();
		}
		return tooltip;
	}

	@Override
	public boolean useNativeToolTip(Object object) {
		return true;
	}

	@Override
	public int getToolTipTimeDisplayed(Object object) {
		return 3000;
	}

	@Override
	public int getToolTipDisplayDelayTime(Object object) {
		return 500;
	}

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		if (element != null && element instanceof ExtensionItem) {
			ExtensionItem item = (ExtensionItem) element;
			cell.setText(item.getText());
			cell.setImage(imageProvider.getImage(item));
		}
	}

}
