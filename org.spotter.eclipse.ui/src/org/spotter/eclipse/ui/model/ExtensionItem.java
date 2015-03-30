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
import org.spotter.eclipse.ui.handlers.HandlerMediatorHelper;
import org.spotter.eclipse.ui.handlers.IHandlerMediator;
import org.spotter.eclipse.ui.listeners.IItemChangedListener;
import org.spotter.eclipse.ui.listeners.IItemPropertiesChangedListener;
import org.spotter.eclipse.ui.model.xml.IModelWrapper;
import org.spotter.shared.environment.model.XMConfiguration;

/**
 * A basic implementation for an extension item.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ExtensionItem implements IExtensionItem {

	private class ConnectionUpdater implements Runnable {
		private volatile boolean isCancelled = false;

		public void cancel() {
			this.isCancelled = true;
		}

		@Override
		public void run() {
			try {
				Boolean newConnection = ExtensionItem.this.modelWrapper.testConnection();
				onConnectionUpdateComplete(this, newConnection, null);
			} catch (Exception e) {
				onConnectionUpdateComplete(this, null, e);
			}
		}
	}

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
	private final IHandlerMediator handlerMediatorHelper;
	private final List<IExtensionItem> childrenItems;
	private IExtensionItem parentItem;
	private final IModelWrapper modelWrapper;
	private final Map<String, ConfigParameterDescription> remainingDescriptions;

	private final String editorId;
	private ServiceClientWrapper client;
	private long lastCacheClearTime;
	private Map<String, ConfigParameterDescription> paramsMap;
	private volatile Boolean connection;
	private volatile ConnectionUpdater connectionUpdater;
	private boolean ignoreConnection;
	private boolean isPending;
	private String extensionDescription;
	private String errorMsg;

	/**
	 * Creates an extension item with no children and no model.
	 * 
	 * @param editorId
	 *            the id of the editor this extension is assigned to
	 */
	public ExtensionItem(String editorId) {
		this(null, null, editorId);
	}

	/**
	 * Creates an extension item with no parent. This is a convenience
	 * constructor as the item is assigned a parent later anyway when being
	 * added to another extension item as a child.
	 * 
	 * @param modelWrapper
	 *            the model wrapper
	 * @param editorId
	 *            the id of the editor this extension is assigned to
	 */
	public ExtensionItem(IModelWrapper modelWrapper, String editorId) {
		this(null, modelWrapper, editorId);
	}

	/**
	 * Creates an extension item in the hierarchy under the given parent.
	 * 
	 * @param parent
	 *            the parent of this item
	 * @param modelWrapper
	 *            the model wrapper for this item
	 * @param editorId
	 *            the id of the editor this extension is assigned to
	 */
	public ExtensionItem(IExtensionItem parent, IModelWrapper modelWrapper, String editorId) {
		this.itemChangedListeners = new ArrayList<IItemChangedListener>();
		this.propertiesChangedListeners = new ArrayList<IItemPropertiesChangedListener>();
		this.handlerMediatorHelper = new HandlerMediatorHelper();
		this.childrenItems = new ArrayList<IExtensionItem>();
		this.parentItem = parent;

		this.editorId = editorId;
		this.client = null;
		this.lastCacheClearTime = 0;
		this.modelWrapper = modelWrapper;
		if (modelWrapper == null || modelWrapper.getXMLModel() == null) {
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
		setConnection(null);
		setConnectionUpdater(null);
		this.ignoreConnection = false;
		this.isPending = modelWrapper == null ? false : true;
		this.errorMsg = null;
	}

	private synchronized Boolean getConnection() {
		return connection;
	}

	private synchronized void setConnection(Boolean connection) {
		this.connection = connection;
	}

	private synchronized ConnectionUpdater getConnectionUpdater() {
		return connectionUpdater;
	}

	private synchronized void setConnectionUpdater(ConnectionUpdater connectionUpdater) {
		this.connectionUpdater = connectionUpdater;
	}

	private synchronized void onConnectionUpdateComplete(ConnectionUpdater updater, Boolean newConnection,
			Exception exception) {
		if (!updater.isCancelled) {
			if (exception == null) {
				setConnection(newConnection);
			} else {
				errorMsg = exception.getMessage();
			}
			isPending = false;
			fireItemAppearanceChangedOnUIThread();
		}
	}

	@Override
	public String getText() {
		if (modelWrapper == null || modelWrapper.getXMLModel() == null) {
			return "";
		}
		String customName = modelWrapper.getName();
		String extensionName = modelWrapper.getExtensionName();
		if (extensionName == null) {
			extensionName = "unnamed extension";
		}
		if (customName != null && !customName.isEmpty()) {
			return customName + " (" + extensionName + ")";
		} else {
			return extensionName;
		}
	}

	@Override
	public String getToolTip() {
		if (isConnectionIgnored()) {
			return extensionDescription != null ? extensionDescription : "";
		}
		if (isPending) {
			return MSG_CONN_PENDING;
		}
		String tooltip = MSG_CONN_INVALID + (errorMsg == null ? "" : ": " + errorMsg);
		Boolean currentConnection = getConnection();
		if (currentConnection != null) {
			tooltip = currentConnection ? MSG_CONN_AVAILABLE : MSG_CONN_UNAVAILABLE;
		}
		return tooltip;
	}

	@Override
	public Image getImage() {
		if (isConnectionIgnored()) {
			return null;
		}
		if (isPending) {
			return IMG_CONN_PENDING;
		}
		Image image = IMG_CONN_INVALID;
		Boolean currentConnection = getConnection();
		if (currentConnection != null) {
			image = currentConnection ? IMG_CONN_AVAILABLE : IMG_CONN_UNAVAILABLE;
		}
		return image;
	}

	@Override
	public String getEditorId() {
		return editorId;
	}

	@Override
	public String toString() {
		String text = getText();
		return text.isEmpty() ? "ExtensionItem {no model}" : text;
	}

	@Override
	public boolean isConnectionIgnored() {
		return ignoreConnection;
	}

	@Override
	public void setIgnoreConnection(boolean ignoreConnection) {
		this.ignoreConnection = ignoreConnection;
	}

	@Override
	public IModelWrapper getModelWrapper() {
		return modelWrapper;
	}

	@Override
	public void propertyDirty(Object propertyItem) {
		fireItemPropertyChanged(propertyItem);
	}

	@Override
	public void addItemChangedListener(IItemChangedListener listener) {
		itemChangedListeners.add(listener);
	}

	@Override
	public void removeItemChangedListener(IItemChangedListener listener) {
		itemChangedListeners.remove(listener);
	}

	@Override
	public void addItemPropertiesChangedListener(IItemPropertiesChangedListener listener) {
		propertiesChangedListeners.add(listener);
	}

	@Override
	public void removeItemPropertiesChangedListener(IItemPropertiesChangedListener listener) {
		propertiesChangedListeners.remove(listener);
	}

	@Override
	public synchronized void updateConnectionStatus() {
		if (isConnectionIgnored() || modelWrapper == null) {
			return;
		}
		ConnectionUpdater currentUpdater = getConnectionUpdater();
		if (currentUpdater != null) {
			currentUpdater.cancel();
		}
		isPending = true;
		errorMsg = null;
		setConnection(null);

		fireItemAppearanceChanged();
		currentUpdater = new ConnectionUpdater();
		setConnectionUpdater(currentUpdater);
		LpeSystemUtils.submitTask(currentUpdater);
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
	public void removed(boolean propagate) {
		modelWrapper.removed();
		if (propagate) {
			for (IExtensionItem child : childrenItems) {
				child.removed(propagate);
			}
		}
	}

	/**
	 * Copies this item including its children. Any attached handlers or
	 * listeners will not be copied.
	 * 
	 * @return a copy of this item
	 */
	@Override
	public IExtensionItem copyItem() {
		ExtensionItem copy = new ExtensionItem(getParent(), getModelWrapper().copy(), getEditorId());
		copy.setIgnoreConnection(isConnectionIgnored());

		// copy children items as well
		if (hasItems()) {
			for (IExtensionItem child : getItems()) {
				IExtensionItem childCopy = child.copyItem();
				copy.addItem(childCopy);
			}
		}

		return copy;
	}

	@Override
	public IExtensionItem getItem(int index) {
		return childrenItems.get(index);
	}

	@Override
	public int getItemIndex(IExtensionItem item) {
		return childrenItems.lastIndexOf(item);
	}

	@Override
	public void addItem(IExtensionItem item) {
		doAddItem(item);
		fireItemChildAdded(this, item);
	}

	@Override
	public void addItem(int index, IExtensionItem item) {
		doAddItem(item);
		if (!moveItem(item, index)) {
			fireItemChildAdded(this, item);
		}
	}

	private void doAddItem(IExtensionItem item) {
		childrenItems.add(item);
		item.setParent(this);
		item.setIgnoreConnection(isConnectionIgnored());
		if (getModelWrapper() != null) {
			List<?> modelContainingList = getModelWrapper().getChildren();
			item.getModelWrapper().setXMLModelContainingList(modelContainingList);
		}
		item.getModelWrapper().added();
	}

	@Override
	public boolean moveItem(IExtensionItem item, int destinationIndex) {
		boolean success = false;
		int index = getItemIndex(item);
		if (index != -1 && index != destinationIndex && destinationIndex >= 0 && destinationIndex < getItemCount()) {
			childrenItems.remove(index);
			if (destinationIndex < getItemCount()) {
				childrenItems.add(destinationIndex, item);
			} else {
				childrenItems.add(item);
			}
			item.getModelWrapper().moved(destinationIndex);
			fireItemChildAdded(this, item);
			success = true;
		}
		return success;
	}

	@Override
	public void removeItem(int index, boolean propagate) {
		IExtensionItem item = childrenItems.remove(index);
		item.removed(propagate);
		fireItemChildRemoved(this, item);
	}

	@Override
	public void removeItem(IExtensionItem item, boolean propgate) {
		childrenItems.remove(item);
		item.removed(propgate);
		fireItemChildRemoved(this, item);
	}

	@Override
	public IExtensionItem[] getItems() {
		return childrenItems.toArray(new IExtensionItem[childrenItems.size()]);
	}

	@Override
	public boolean hasItems() {
		return !childrenItems.isEmpty();
	}

	@Override
	public int getItemCount() {
		return childrenItems.size();
	}

	@Override
	public void setParent(IExtensionItem parent) {
		this.parentItem = parent;
	}

	@Override
	public IExtensionItem getParent() {
		return parentItem;
	}

	@Override
	public boolean hasParent(IExtensionItem parent) {
		if (getParent() != null) {
			if (getParent().equals(parent)) {
				return true;
			} else {
				return getParent().hasParent(parent);
			}
		}

		return false;
	}

	@Override
	public void setError(String errorMessage) {
		if (errorMessage != null) {
			this.connection = null;
			this.errorMsg = errorMessage;
		}
	}

	@Override
	public void setChildrenError(String errorMessage) {
		for (IExtensionItem item : childrenItems) {
			item.setError(errorMessage);
			item.fireItemAppearanceChanged();
			item.setChildrenError(errorMessage);
		}
	}

	@Override
	public void updateChildrenConnections() {
		for (IExtensionItem item : childrenItems) {
			item.updateConnectionStatus();
			item.updateChildrenConnections();
		}
	}

	@Override
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

	@Override
	public Collection<ConfigParameterDescription> getConfigurableExtensionConfigParams() {
		return remainingDescriptions.values();
	}

	@Override
	public boolean hasConfigurableExtensionConfigParams() {
		return !remainingDescriptions.isEmpty();
	}

	@Override
	public void fireItemAppearanceChanged() {
		fireItemAppearanceChanged(this);
	}

	@Override
	public void fireItemPropertiesChanged() {
		for (IItemPropertiesChangedListener listener : propertiesChangedListeners) {
			listener.propertiesChanged();
		}
	}

	@Override
	public void fireItemAppearanceChanged(IExtensionItem item) {
		for (IItemChangedListener listener : itemChangedListeners) {
			listener.appearanceChanged(item);
		}
		if (parentItem != null) {
			parentItem.fireItemAppearanceChanged(item);
		}
	}

	@Override
	public void fireItemChildAdded(IExtensionItem parent, IExtensionItem item) {
		for (IItemChangedListener listener : itemChangedListeners) {
			listener.childAdded(parent, item);
		}
		if (parentItem != null) {
			parentItem.fireItemChildAdded(parent, item);
		}
	}

	@Override
	public void fireItemChildRemoved(IExtensionItem parent, IExtensionItem item) {
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

	@Override
	public boolean canHandle(String commandId) {
		return handlerMediatorHelper.canHandle(commandId);
	}

	@Override
	public Object getHandler(String commandId) {
		return handlerMediatorHelper.getHandler(commandId);
	}

	@Override
	public void addHandler(String commandId, Object handler) {
		handlerMediatorHelper.addHandler(commandId, handler);
	}

	@Override
	public void removeHandler(String commandId) {
		handlerMediatorHelper.removeHandler(commandId);
	}

}
