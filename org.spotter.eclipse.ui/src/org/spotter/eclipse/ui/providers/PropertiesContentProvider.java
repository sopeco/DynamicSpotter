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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.listeners.IItemPropertiesChangedListener;
import org.spotter.eclipse.ui.model.AbstractPropertyItem;
import org.spotter.eclipse.ui.model.ConfigParamPropertyItem;
import org.spotter.eclipse.ui.model.ExtensionItem;
import org.spotter.eclipse.ui.model.xml.IModelWrapper;
import org.spotter.shared.environment.model.XMConfiguration;

/**
 * Content provider for property items that can be used e.g. in a
 * <code>TableViewer</code>. This content provider expects input of type
 * {@link ExtensionItem}.
 * 
 * @author Denis Knoepfle
 * 
 */
public class PropertiesContentProvider implements IStructuredContentProvider, IItemPropertiesChangedListener {

	private ExtensionItem inputModel;
	private AbstractTableViewer viewer;

	@Override
	public void dispose() {
		if (inputModel != null) {
			inputModel.removeItemPropertiesChangedListener(this);
		}
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// remember viewer
		this.viewer = (AbstractTableViewer) viewer;
		// deregister listener from old input
		if (inputModel != null) {
			inputModel.removeItemPropertiesChangedListener(this);
			inputModel = null;
		}
		// register listener at new input
		if (newInput instanceof ExtensionItem) {
			inputModel = (ExtensionItem) newInput;
			inputModel.addItemPropertiesChangedListener(this);
		}
		this.viewer.refresh();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (!(inputElement instanceof ExtensionItem)) {
			return new Object[0];
		}
		ExtensionItem tableItem = (ExtensionItem) inputElement;
		IModelWrapper modelWrapper = tableItem.getModelWrapper();
		List<AbstractPropertyItem> mandatoryConfigItems = new ArrayList<AbstractPropertyItem>();
		List<AbstractPropertyItem> nonMandatoryConfigItems = new ArrayList<AbstractPropertyItem>();
		List<XMConfiguration> xmConfigList = modelWrapper.getConfig();

		Collections.sort(xmConfigList);
		
		int size = xmConfigList == null ? 0 : xmConfigList.size();
		Object[] result = new Object[size];

		if (xmConfigList != null) {
			for (XMConfiguration xmConfig : xmConfigList) {
				ConfigParameterDescription desc = tableItem.getExtensionConfigParam(xmConfig.getKey());
				AbstractPropertyItem configParam = new ConfigParamPropertyItem(modelWrapper, desc, xmConfig);
				if (desc.isMandatory()) {
					mandatoryConfigItems.add(configParam);
				} else {
					nonMandatoryConfigItems.add(configParam);
				}
			}

			int index = 0;
			Object[] configItems = mandatoryConfigItems.toArray();
			System.arraycopy(configItems, 0, result, index, configItems.length);
			index += configItems.length;

			configItems = nonMandatoryConfigItems.toArray();
			System.arraycopy(configItems, 0, result, index, configItems.length);
			index += configItems.length;
		}

		return result;
	}

	@Override
	public void propertiesChanged() {
		if (viewer != null) {
			viewer.refresh();
		}
	}

	@Override
	public void itemPropertyRemoved(Object propertyItem) {
		if (viewer != null) {
			viewer.remove(propertyItem);
		}
	}

	@Override
	public void itemPropertyChanged(Object propertyItem) {
		if (viewer != null) {
			viewer.update(propertyItem, null);
		}
	}

}
