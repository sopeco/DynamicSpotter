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

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Font;
import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.model.AbstractPropertyItem;
import org.spotter.eclipse.ui.util.AbstractNameFormatter;
import org.spotter.eclipse.ui.viewers.PropertiesGroupViewer;

/**
 * Label provider for properties table. This label provider expects objects of
 * type {@link AbstractPropertyItem}.
 * 
 * @author Denis Knoepfle
 * 
 */
public class PropertiesLabelProvider extends CellLabelProvider {

	private static final int TOOLTIP_DISPLAY_TIME = 3000;
	private static final int TOOLTIP_DISPLAY_DELAY_TIME = 500;

	private static final FontRegistry FONT_REGISTRY = new FontRegistry();
	private static final Font NORMAL_FONT = FONT_REGISTRY.get(JFaceResources.DEFAULT_FONT);
	private static final Font ITALIC_FONT = FONT_REGISTRY.getItalic(JFaceResources.DEFAULT_FONT);

	private final PropertiesGroupViewer propertiesGroupViewer;

	/**
	 * Create a new label provider for the given properties viewer.
	 * 
	 * @param propertiesGroupViewer
	 *            The properties viewer
	 */
	public PropertiesLabelProvider(PropertiesGroupViewer propertiesGroupViewer) {
		super();
		this.propertiesGroupViewer = propertiesGroupViewer;
	}

	@Override
	public String getToolTipText(Object element) {
		String tooltip = null;
		if (element != null && element instanceof AbstractPropertyItem) {
			tooltip = ((AbstractPropertyItem) element).getToolTip();
		}
		return tooltip;
	}

	@Override
	public boolean useNativeToolTip(Object object) {
		return true;
	}

	@Override
	public int getToolTipTimeDisplayed(Object object) {
		return TOOLTIP_DISPLAY_TIME;
	}

	@Override
	public int getToolTipDisplayDelayTime(Object object) {
		return TOOLTIP_DISPLAY_DELAY_TIME;
	}

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		if (element != null && element instanceof AbstractPropertyItem) {
			AbstractPropertyItem tableItem = (AbstractPropertyItem) element;
			ConfigParameterDescription desc = tableItem.getConfigParameterDescription();
			boolean mandatory = desc == null ? true : desc.isMandatory();
			cell.setFont(mandatory ? NORMAL_FONT : ITALIC_FONT);

			switch (cell.getColumnIndex()) {
			case 0:
				AbstractNameFormatter formatter = propertiesGroupViewer.getNameFormatter();
				String name = tableItem.getName();
				cell.setText(formatter == null ? name : formatter.format(name));
				break;
			case 1:
				cell.setText(tableItem.getValue());
				break;
			default:
				break;
			}
		}
	}

}
