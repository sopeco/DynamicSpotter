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
package org.spotter.eclipse.ui.model;

import java.util.Collection;

import org.eclipse.swt.graphics.Image;
import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.listeners.IItemChangedListener;
import org.spotter.eclipse.ui.listeners.IItemPropertiesChangedListener;
import org.spotter.eclipse.ui.model.xml.IModelWrapper;

/**
 * An abstract decorator for extension items. Subclasses may either override
 * methods and add functionality, or they can make the item applicable for menu
 * commands by implementing the corresponding interface in
 * <code>org.spotter.eclipse.ui.menu</code>.
 * 
 * @author Denis Knoepfle
 * 
 */
public abstract class AbstractExtensionItemDecorator implements IExtensionItem {

	private final IExtensionItem delegate;

	/**
	 * Constructs a new decorator delegating to the given extension item.
	 * 
	 * @param extensionItem
	 *            the item to delegate to
	 */
	public AbstractExtensionItemDecorator(IExtensionItem extensionItem) {
		this.delegate = extensionItem;
	}

	@Override
	public boolean canHandle(String commandId) {
		return delegate.canHandle(commandId);
	}

	@Override
	public Object getHandler(String commandId) {
		return delegate.getHandler(commandId);
	}

	@Override
	public void addHandler(String commandId, Object handler) {
		delegate.addHandler(commandId, handler);
	}

	@Override
	public void removeHandler(String commandId) {
		delegate.removeHandler(commandId);
	}

	@Override
	public String getText() {
		return delegate.getText();
	}

	@Override
	public String getToolTip() {
		return delegate.getToolTip();
	}

	@Override
	public Image getImage() {
		return delegate.getImage();
	}

	@Override
	public boolean isConnectionIgnored() {
		return delegate.isConnectionIgnored();
	}

	@Override
	public void setIgnoreConnection(boolean ignoreConnection) {
		delegate.setIgnoreConnection(ignoreConnection);
	}

	@Override
	public IModelWrapper getModelWrapper() {
		return delegate.getModelWrapper();
	}

	@Override
	public void propertyDirty(Object propertyItem) {
		delegate.propertyDirty(propertyItem);
	}

	@Override
	public void addItemChangedListener(IItemChangedListener listener) {
		delegate.addItemChangedListener(listener);
	}

	@Override
	public void removeItemChangedListener(IItemChangedListener listener) {
		delegate.removeItemChangedListener(listener);
	}

	@Override
	public void addItemPropertiesChangedListener(IItemPropertiesChangedListener listener) {
		delegate.addItemPropertiesChangedListener(listener);
	}

	@Override
	public void removeItemPropertiesChangedListener(IItemPropertiesChangedListener listener) {
		delegate.removeItemPropertiesChangedListener(listener);
	}

	@Override
	public void updateConnectionStatus() {
		delegate.updateConnectionStatus();
	}

	@Override
	public void addConfigParamUsingDescription(ConfigParameterDescription desc) {
		delegate.addConfigParamUsingDescription(desc);
	}

	@Override
	public void removeConfigParam(ConfigParamPropertyItem item) {
		delegate.removeConfigParam(item);
	}

	@Override
	public void removeNonMandatoryConfigParams() {
		delegate.removeNonMandatoryConfigParams();
	}

	@Override
	public void removed() {
		delegate.removed();
	}

	@Override
	public IExtensionItem getItem(int index) {
		return delegate.getItem(index);
	}

	@Override
	public int getItemIndex(IExtensionItem item) {
		return delegate.getItemIndex(item);
	}

	@Override
	public void addItem(ExtensionItem item) {
		delegate.addItem(item);
	}

	@Override
	public void removeItem(int index) {
		delegate.removeItem(index);
	}

	@Override
	public void removeItem(IExtensionItem item) {
		delegate.removeItem(item);
	}

	@Override
	public IExtensionItem[] getItems() {
		return delegate.getItems();
	}

	@Override
	public boolean hasItems() {
		return delegate.hasItems();
	}

	@Override
	public int getItemCount() {
		return delegate.getItemCount();
	}

	@Override
	public IExtensionItem getParent() {
		return delegate.getParent();
	}

	@Override
	public void setError(String errorMessage) {
		delegate.setError(errorMessage);
	}

	@Override
	public void setChildrenError(String errorMessage) {
		delegate.setChildrenError(errorMessage);
	}

	@Override
	public void updateChildrenConnections() {
		delegate.updateChildrenConnections();
	}

	@Override
	public ConfigParameterDescription getExtensionConfigParam(String key) {
		return delegate.getExtensionConfigParam(key);
	}

	@Override
	public Collection<ConfigParameterDescription> getConfigurableExtensionConfigParams() {
		return delegate.getConfigurableExtensionConfigParams();
	}

	@Override
	public boolean hasConfigurableExtensionConfigParams() {
		return delegate.hasConfigurableExtensionConfigParams();
	}

	@Override
	public void fireItemAppearanceChanged() {
		delegate.fireItemAppearanceChanged();
	}

	@Override
	public void fireItemPropertiesChanged() {
		delegate.fireItemPropertiesChanged();
	}

	@Override
	public void fireItemAppearanceChanged(IExtensionItem item) {
		delegate.fireItemAppearanceChanged(item);
	}

	@Override
	public void fireItemChildAdded(IExtensionItem parent, IExtensionItem item) {
		delegate.fireItemChildAdded(parent, item);
	}

	@Override
	public void fireItemChildRemoved(IExtensionItem parent, IExtensionItem item) {
		delegate.fireItemChildRemoved(parent, item);
	}

}
