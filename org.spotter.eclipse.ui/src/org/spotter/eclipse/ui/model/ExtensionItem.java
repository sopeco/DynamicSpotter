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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.system.LpeSystemUtils;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.listeners.IItemChangedListener;
import org.spotter.eclipse.ui.listeners.IItemPropertiesChangedListener;
import org.spotter.eclipse.ui.model.xml.IModelWrapper;
import org.spotter.shared.environment.model.XMConfiguration;

/**
 * An item that represents an extension. The item can hold children items as
 * well and thus can be used for hierarchical trees as well.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ExtensionItem {

	private static final String MSG_CONN_PENDING = "Connection test pending...";
	private static final String MSG_CONN_AVAILABLE = "Connection OK";
	private static final String MSG_CONN_UNAVAILABLE = "No connection";
	private static final String MSG_CONN_INVALID = "Invalid state";

	private static final Image IMG_CONN_PENDING = Activator.getImage("icons/pending.gif");
	private static final Image IMG_CONN_AVAILABLE = Activator.getImage("icons/tick.png");
	private static final Image IMG_CONN_UNAVAILABLE = Activator.getImage("icons/cross.png");
	private static final Image IMG_CONN_INVALID = Activator.getImage("icons/exclamation.png");

	private final List<IItemChangedListener> itemChangedListeners;
	private final List<IItemPropertiesChangedListener> propertiesChangedListeners;
	private final List<ExtensionItem> childrenItems;
	private ExtensionItem parentItem;
	private final IModelWrapper modelWrapper;
	private final Map<String, ConfigParameterDescription> remainingDescriptions;

	private ServiceClientWrapper client;
	private long lastCacheClearTime;
	private Map<String, ConfigParameterDescription> paramsMap;
	private Boolean connection;
	private boolean ignoreConnection;
	private boolean isPending;
	private String extensionDescription;
	private String errorMsg;

	/**
	 * Creates an extension item with no children and no model.
	 */
	public ExtensionItem() {
		this(null, null);
	}

	/**
	 * Creates an extension item with no parent. This is a convenience
	 * constructor as the item is assigned a parent later anyway when being
	 * added to another extension item as a child.
	 * 
	 * @param modelWrapper
	 *            the model wrapper
	 */
	public ExtensionItem(IModelWrapper modelWrapper) {
		this(null, modelWrapper);
	}

	/**
	 * Creates an extension item in the hierarchy under the given parent.
	 * 
	 * @param parent
	 *            the parent of this item
	 * @param modelWrapper
	 *            the model wrapper for this item
	 */
	public ExtensionItem(ExtensionItem parent, IModelWrapper modelWrapper) {
		this.itemChangedListeners = new ArrayList<IItemChangedListener>();
		this.propertiesChangedListeners = new ArrayList<IItemPropertiesChangedListener>();
		this.childrenItems = new ArrayList<ExtensionItem>();
		this.parentItem = parent;

		this.client = null;
		this.lastCacheClearTime = 0;
		this.modelWrapper = modelWrapper;
		if (modelWrapper == null) {
			this.remainingDescriptions = new HashMap<String, ConfigParameterDescription>();
		} else {
			String projectName = modelWrapper.getProjectName();
			if (projectName != null) {
				this.client = Activator.getDefault().getClient(projectName);
				this.lastCacheClearTime = client.getLastClearTime();
			}
			this.remainingDescriptions = computeConfigurableExtensionConfigParams();
			ConfigParameterDescription extDesc = getExtensionConfigParam(ConfigParameterDescription.EXT_DESCRIPTION_KEY);
			if (extDesc != null) {
				this.extensionDescription = extDesc.getDefaultValue();
			}
		}
		this.connection = null;
		this.ignoreConnection = false;
		this.isPending = modelWrapper == null ? false : true;
		this.errorMsg = null;
	}

	/**
	 * @return the text of this table item
	 */
	public String getText() {
		if (modelWrapper == null) {
			return "";
		}
		String customName = modelWrapper.getName();
		String extensionName = modelWrapper.getExtensionName();
		if (customName != null && !customName.isEmpty()) {
			return customName + " (" + extensionName + ")";
		} else {
			return extensionName;
		}
	}

	/**
	 * Returns the tool tip for this item. It contains the connection status of
	 * the extension. When the connection is ignored the extension description
	 * will be returned if available or an empty string otherwise.
	 * 
	 * @return the tool tip for this item
	 */
	public String getToolTip() {
		if (ignoreConnection) {
			return extensionDescription != null ? extensionDescription : "";
		}
		if (isPending) {
			return MSG_CONN_PENDING;
		}
		String tooltip = MSG_CONN_INVALID + (errorMsg == null ? "" : ": " + errorMsg);
		if (connection != null) {
			tooltip = connection ? MSG_CONN_AVAILABLE : MSG_CONN_UNAVAILABLE;
		}
		return tooltip;
	}

	/**
	 * @return the image of this table item
	 */
	public Image getImage() {
		if (ignoreConnection) {
			return null;
		}
		if (isPending) {
			return IMG_CONN_PENDING;
		}
		Image image = IMG_CONN_INVALID;
		if (connection != null) {
			image = connection ? IMG_CONN_AVAILABLE : IMG_CONN_UNAVAILABLE;
		}
		return image;
	}

	/**
	 * @return whether the connection of this item is irrelevant and is ignored
	 */
	public boolean isConnectionIgnored() {
		return ignoreConnection;
	}

	/**
	 * Set if connection status is ignored. If it is ignored calls to
	 * {@link #updateConnectionStatus()} will have no effect.
	 * 
	 * @param ignoreConnection
	 *            whether to ignore or not
	 */
	public void setIgnoreConnection(boolean ignoreConnection) {
		this.ignoreConnection = ignoreConnection;
	}

	/**
	 * @return the model wrapper that is used
	 */
	public IModelWrapper getModelWrapper() {
		return modelWrapper;
	}

	/**
	 * This method should be called whenever one of this item's properties has
	 * been modified from outside this class, so that this item can reflect
	 * changes properly and may notify its listeners if necessary.
	 * 
	 * @param propertyItem
	 *            the affected property item
	 */
	public void propertyDirty(Object propertyItem) {
		fireItemPropertyChanged(propertyItem);
	}

	/**
	 * Adds a listener to this item that is notified when this item changes.
	 * 
	 * @param listener
	 *            the listener to register
	 */
	public void addItemChangedListener(IItemChangedListener listener) {
		itemChangedListeners.add(listener);
	}

	/**
	 * Removes the given listener.
	 * 
	 * @param listener
	 *            the listener to deregister
	 */
	public void removeItemChangedListener(IItemChangedListener listener) {
		itemChangedListeners.remove(listener);
	}

	/**
	 * Adds a listener to this item that is notified when the properties have
	 * changed.
	 * 
	 * @param listener
	 *            the listener to register
	 */
	public void addItemPropertiesChangedListener(IItemPropertiesChangedListener listener) {
		propertiesChangedListeners.add(listener);
	}

	/**
	 * Removes the given listener.
	 * 
	 * @param listener
	 *            the listener to deregister
	 */
	public void removeItemPropertiesChangedListener(IItemPropertiesChangedListener listener) {
		propertiesChangedListeners.remove(listener);
	}

	/**
	 * Updates the connection status for this item asynchronously, thus this
	 * method is non-blocking. If <code>setIgnoreConnection()</code> is set to
	 * <code>true</code> the update has no effect.
	 */
	public void updateConnectionStatus() {
		if (isConnectionIgnored() || modelWrapper == null) {
			return;
		}
		isPending = true;
		errorMsg = null;

		fireItemAppearanceChanged();
		LpeSystemUtils.submitTask(new Runnable() {
			@Override
			public void run() {
				try {
					connection = null;
					connection = ExtensionItem.this.modelWrapper.testConnection();
				} catch (Exception e) {
					errorMsg = e.getMessage();
				}
				isPending = false;
				fireItemAppearanceChangedOnUIThread();
			}
		});
	}

	/**
	 * Creates a new <code>XMConfiguration</code> using the given description
	 * and adds it to the model's config list.
	 * 
	 * @param desc
	 *            the description to retrieve key and initial value from
	 */
	public void addConfigParamUsingDescription(ConfigParameterDescription desc) {
		List<XMConfiguration> xmConfigList = modelWrapper.getConfig();
		if (xmConfigList == null) {
			xmConfigList = new ArrayList<XMConfiguration>();
			modelWrapper.setConfig(xmConfigList);
		}
		XMConfiguration xmConfig = new XMConfiguration();
		xmConfig.setKey(desc.getName());
		xmConfig.setValue(desc.getDefaultValue());
		xmConfigList.add(xmConfig);
		if (!desc.isMandatory()) {
			remainingDescriptions.remove(xmConfig.getKey());
		}
		fireItemPropertiesChanged();
		fireItemAppearanceChanged();
	}

	/**
	 * Removes the config param contained in the given item from the model's
	 * config list.
	 * 
	 * @param item
	 *            the config item containing the config param to remove
	 */
	public void removeConfigParam(ConfigParamPropertyItem item) {
		if (modelWrapper.getConfig() != null) {
			XMConfiguration conf = item.getXMConfig();
			if (modelWrapper.getConfig().remove(conf)) {
				ConfigParameterDescription desc = item.getConfigParameterDescription();
				if (!desc.isMandatory()) {
					remainingDescriptions.put(conf.getKey(), desc);
				}
				fireItemPropertyRemoved(item);
			}
		}
	}

	/**
	 * Removes all non-mandatory config parameters from the model's config list.
	 */
	public void removeNonMandatoryConfigParams() {
		List<XMConfiguration> xmConfigList = modelWrapper.getConfig();
		List<XMConfiguration> removeLater = new ArrayList<XMConfiguration>();
		if (xmConfigList != null) {
			for (XMConfiguration conf : xmConfigList) {
				ConfigParameterDescription desc = getExtensionConfigParam(conf.getKey());
				if (!desc.isMandatory()) {
					// TODO: recalculate remainingDescription after cache clear
					removeLater.add(conf);
					remainingDescriptions.put(conf.getKey(), desc);
				}
			}
			xmConfigList.removeAll(removeLater);
			fireItemPropertiesChanged();
		}
	}

	/**
	 * Must be called when this item is removed.
	 */
	public void removed() {
		modelWrapper.removed();
		// when this item is removed all of its children will be gone too
		for (ExtensionItem child : childrenItems) {
			child.removed();
		}
	}

	/**
	 * Returns a specific item determined by its index.
	 * 
	 * @param index
	 *            the index of the item
	 * @return the item at the given position
	 */
	public ExtensionItem getItem(int index) {
		return childrenItems.get(index);
	}

	/**
	 * Returns the index of the given item.
	 * 
	 * @param item
	 *            the child item
	 * @return index of the child item
	 */
	public int getItemIndex(ExtensionItem item) {
		return childrenItems.lastIndexOf(item);
	}

	/**
	 * Adds a new child item. The added item will have this item as parent and
	 * inherits settings like <code>isConnectionIgnored()</code>.
	 * 
	 * @param item
	 *            the item to add
	 */
	public void addItem(ExtensionItem item) {
		childrenItems.add(item);
		item.parentItem = this;
		item.setIgnoreConnection(ignoreConnection);
		fireItemChildAdded(this, item);
	}

	/**
	 * Removes a child item at the given position.
	 * 
	 * @param index
	 *            the index of the child to remove
	 */
	public void removeItem(int index) {
		ExtensionItem item = childrenItems.remove(index);
		item.removed();
		fireItemChildRemoved(this, item);
	}

	/**
	 * Removes the given child item.
	 * 
	 * @param item
	 *            the child item to remove
	 */
	public void removeItem(ExtensionItem item) {
		childrenItems.remove(item);
		item.removed();
		fireItemChildRemoved(this, item);
	}

	/**
	 * @return the children items of this item
	 */
	public ExtensionItem[] getItems() {
		return childrenItems.toArray(new ExtensionItem[childrenItems.size()]);
	}

	/**
	 * @return <code>true</code> if item has children, <code>false</code>
	 *         otherwise
	 */
	public boolean hasItems() {
		return !childrenItems.isEmpty();
	}

	/**
	 * @return the number of children items
	 */
	public int getItemCount() {
		return childrenItems.size();
	}

	/**
	 * @return the parent item or <code>null</code> if this item is the root
	 */
	public ExtensionItem getParent() {
		return parentItem;
	}

	/**
	 * Convenience method to update the error message for all children items
	 * recursively. The connection status will be set to erroneous with the
	 * given error message.
	 * 
	 * @param errorMessage
	 *            The error message to set
	 */
	public void setChildrenError(String errorMessage) {
		for (ExtensionItem item : childrenItems) {
			item.connection = null;
			item.errorMsg = errorMessage;
			item.fireItemAppearanceChanged();
			item.setChildrenError(errorMessage);
		}
	}

	/**
	 * Convenience method to update the connection status for all children items
	 * recursively.
	 */
	public void updateChildrenConnections() {
		for (ExtensionItem item : childrenItems) {
			item.updateConnectionStatus();
			item.updateChildrenConnections();
		}
	}

	/**
	 * @param key
	 *            The key of the corresponding description
	 * @return The description that suits the given key
	 */
	public ConfigParameterDescription getExtensionConfigParam(String key) {
		if (paramsMap == null || hasCacheCleared()) {
			initParamsMap();
			return paramsMap == null ? null : paramsMap.get(key);
		}
		return paramsMap.get(key);
	}

	private boolean hasCacheCleared() {
		if (client == null) {
			return false;
		}

		long clearTime = client.getLastClearTime();
		return lastCacheClearTime < clearTime;
	}

	/**
	 * @return collection containing all non-used and non-mandatory
	 *         configuration parameter descriptions that are editable
	 */
	public Collection<ConfigParameterDescription> getConfigurableExtensionConfigParams() {
		return remainingDescriptions.values();
	}

	/**
	 * A convenience method to check if there are configurable parameters left.
	 * 
	 * @return <code>true</code> if configurable parameters left, otherwise
	 *         <code>false</code>
	 */
	public boolean hasConfigurableExtensionConfigParams() {
		return !remainingDescriptions.isEmpty();
	}

	/**
	 * Notifies <code>IItemChangedListener</code>s that this item's appearance
	 * has changed. Call propagates to parent items recursively, so that their
	 * listeners also get notified.
	 */
	public void fireItemAppearanceChanged() {
		fireItemAppearanceChanged(this);
	}

	/**
	 * Notifies <code>IItemPropertiesChangedListener</code>s that this item's
	 * properties have changed.
	 */
	public void fireItemPropertiesChanged() {
		for (IItemPropertiesChangedListener listener : propertiesChangedListeners) {
			listener.propertiesChanged();
		}
	}

	private void initParamsMap() {
		paramsMap = null;
		Set<ConfigParameterDescription> params = modelWrapper.getExtensionConfigParams();
		if (params == null) {
			// can not initialize the params map
			return;
		}
		paramsMap = new HashMap<String, ConfigParameterDescription>();
		for (ConfigParameterDescription desc : params) {
			paramsMap.put(desc.getName(), desc);
		}
		
		if (client != null) {
			lastCacheClearTime = client.getLastClearTime();
		}
	}

	/**
	 * Creates a set of keys that are used within the model's config list.
	 * 
	 * @return set of used keys within configuration
	 */
	private Set<String> createConfigParamKeysSet() {
		Set<String> keysSet = new HashSet<String>();
		if (modelWrapper.getConfig() != null) {
			for (XMConfiguration conf : modelWrapper.getConfig()) {
				keysSet.add(conf.getKey());
			}
		}
		return keysSet;
	}

	/**
	 * Computes a map containing all non-mandatory configuration parameter
	 * descriptions that are editable and have not been added yet. The
	 * descriptions are accessible by their name as key.
	 * 
	 * @return map containing all non-used and non-mandatory configuration
	 *         parameter descriptions that are editable
	 */
	private Map<String, ConfigParameterDescription> computeConfigurableExtensionConfigParams() {
		Map<String, ConfigParameterDescription> configurable = new HashMap<String, ConfigParameterDescription>();
		Set<String> usedKeysSet = createConfigParamKeysSet();
		Set<ConfigParameterDescription> extensionConfigParams = modelWrapper.getExtensionConfigParams();
		if (extensionConfigParams == null) {
			return configurable;
		}
		for (ConfigParameterDescription desc : extensionConfigParams) {
			if (desc.isEditable() && !desc.isMandatory() && !usedKeysSet.contains(desc.getName())) {
				configurable.put(desc.getName(), desc);
			}
		}
		return configurable;
	}

	// Call propagates to parent items recursively
	private void fireItemAppearanceChanged(ExtensionItem item) {
		for (IItemChangedListener listener : itemChangedListeners) {
			listener.appearanceChanged(item);
		}
		if (parentItem != null) {
			parentItem.fireItemAppearanceChanged(item);
		}
	}

	// Call propagates to parent items recursively.
	private void fireItemChildAdded(ExtensionItem parent, ExtensionItem item) {
		for (IItemChangedListener listener : itemChangedListeners) {
			listener.childAdded(parent, item);
		}
		if (parentItem != null) {
			parentItem.fireItemChildAdded(parent, item);
		}
	}

	// Call propagates to parent items recursively.
	private void fireItemChildRemoved(ExtensionItem parent, ExtensionItem item) {
		for (IItemChangedListener listener : itemChangedListeners) {
			listener.childRemoved(parent, item);
		}
		if (parentItem != null) {
			parentItem.fireItemChildRemoved(parent, item);
		}
	}

	private void fireItemPropertyRemoved(Object propertyItem) {
		for (IItemPropertiesChangedListener listener : propertiesChangedListeners) {
			listener.itemPropertyRemoved(propertyItem);
		}
	}

	private void fireItemPropertyChanged(Object propertyItem) {
		for (IItemPropertiesChangedListener listener : propertiesChangedListeners) {
			listener.itemPropertyChanged(propertyItem);
		}
	}

	/**
	 * Fires an item appearance changed event using the UI-Thread. This helper
	 * method can be used by non-UI-Threads to ensure that the listeners are
	 * executing their code on the UI thread.
	 */
	private void fireItemAppearanceChangedOnUIThread() {
		Display display = Display.getDefault();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				fireItemAppearanceChanged();
			}
		});
	}

}
