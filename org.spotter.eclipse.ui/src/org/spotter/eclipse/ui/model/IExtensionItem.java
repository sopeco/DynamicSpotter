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
import org.spotter.eclipse.ui.handlers.IHandlerMediator;
import org.spotter.eclipse.ui.listeners.IItemChangedListener;
import org.spotter.eclipse.ui.listeners.IItemPropertiesChangedListener;
import org.spotter.eclipse.ui.model.xml.IModelWrapper;

/**
 * An interface for items that represent an extension. The items can hold
 * children items as well and thus can be used for hierarchical trees.
 * 
 * @author Denis Knoepfle
 * 
 */
public interface IExtensionItem extends IHandlerMediator {

	/**
	 * @return the text of this table item
	 */
	String getText();

	/**
	 * Returns the tool tip for this item. It contains the connection status of
	 * the extension. When the connection is ignored the extension description
	 * will be returned if available or an empty string otherwise.
	 * 
	 * @return the tool tip for this item
	 */
	String getToolTip();

	/**
	 * @return the image of this table item
	 */
	Image getImage();

	/**
	 * @return the id of the editor this extension is assigned to
	 */
	String getEditorId();

	/**
	 * @return whether the connection of this item is irrelevant and is ignored
	 */
	boolean isConnectionIgnored();

	/**
	 * Set if connection status is ignored. If it is ignored calls to
	 * {@link #updateConnectionStatus()} will have no effect.
	 * 
	 * @param ignoreConnection
	 *            whether to ignore or not
	 */
	void setIgnoreConnection(boolean ignoreConnection);

	/**
	 * @return the model wrapper that is used
	 */
	IModelWrapper getModelWrapper();

	/**
	 * This method should be called whenever one of this item's properties has
	 * been modified from outside this class, so that this item can reflect
	 * changes properly and may notify its listeners if necessary.
	 * 
	 * @param propertyItem
	 *            the affected property item
	 */
	void propertyDirty(Object propertyItem);

	/**
	 * Adds a listener to this item that is notified when this item changes.
	 * 
	 * @param listener
	 *            the listener to register
	 */
	void addItemChangedListener(IItemChangedListener listener);

	/**
	 * Removes the given listener.
	 * 
	 * @param listener
	 *            the listener to deregister
	 */
	void removeItemChangedListener(IItemChangedListener listener);

	/**
	 * Adds a listener to this item that is notified when the properties have
	 * changed.
	 * 
	 * @param listener
	 *            the listener to register
	 */
	void addItemPropertiesChangedListener(IItemPropertiesChangedListener listener);

	/**
	 * Removes the given listener.
	 * 
	 * @param listener
	 *            the listener to deregister
	 */
	void removeItemPropertiesChangedListener(IItemPropertiesChangedListener listener);

	/**
	 * Updates the connection status for this item asynchronously, thus this
	 * method is non-blocking. If <code>setIgnoreConnection()</code> is set to
	 * <code>true</code> the update has no effect.
	 */
	void updateConnectionStatus();

	/**
	 * Creates a new <code>XMConfiguration</code> using the given description
	 * and adds it to the model's config list.
	 * 
	 * @param desc
	 *            the description to retrieve key and initial value from
	 */
	void addConfigParamUsingDescription(ConfigParameterDescription desc);

	/**
	 * Removes the config param contained in the given item from the model's
	 * config list.
	 * 
	 * @param item
	 *            the config item containing the config param to remove
	 */
	void removeConfigParam(ConfigParamPropertyItem item);

	/**
	 * Removes all non-mandatory config parameters from the model's config list.
	 */
	void removeNonMandatoryConfigParams();

	/**
	 * Must be called when this item is removed.
	 * 
	 * @param propagate
	 *            whether to propagate the call to its children
	 */
	void removed(boolean propagate);

	/**
	 * Returns a deep copy of this item including its children. If this item
	 * contained any unique keys, the copy will have new keys assigned instead
	 * to ensure that all keys remain unique.
	 * 
	 * @return a copy of this item
	 */
	IExtensionItem copyItem();

	/**
	 * Returns a specific item determined by its index.
	 * 
	 * @param index
	 *            the index of the item
	 * @return the item at the given position
	 */
	IExtensionItem getItem(int index);

	/**
	 * Returns the index of the given item.
	 * 
	 * @param item
	 *            the child item
	 * @return index of the child item
	 */
	int getItemIndex(IExtensionItem item);

	/**
	 * Adds a new child item. The added item will have this item as parent and
	 * inherits settings like <code>isConnectionIgnored()</code>.
	 * 
	 * @param item
	 *            the item to add
	 */
	void addItem(IExtensionItem item);

	/**
	 * Adds a new child item at the given position. The added item will have
	 * this item as parent and inherits settings like
	 * <code>isConnectionIgnored()</code>. The current item at that position and
	 * consecutive items will be moved back one place.
	 * 
	 * @param index
	 *            the index of the new item
	 * @param item
	 *            the item to add
	 */
	void addItem(int index, IExtensionItem item);

	/**
	 * Moves the given child item to the destination position.
	 * 
	 * @param item
	 *            the item to move
	 * @param destinationIndex
	 *            the index the item should be moved to
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	boolean moveItem(IExtensionItem item, int destinationIndex);

	/**
	 * Removes a child item at the given position.
	 * 
	 * @param index
	 *            the index of the child to remove
	 * @param propagate
	 *            whether to propagate the call to its children
	 */
	void removeItem(int index, boolean propagate);

	/**
	 * Removes the given child item.
	 * 
	 * @param item
	 *            the child item to remove
	 * @param propagate
	 *            whether to propagate the call to its children
	 */
	void removeItem(IExtensionItem item, boolean propagate);

	/**
	 * @return the children items of this item
	 */
	IExtensionItem[] getItems();

	/**
	 * @return <code>true</code> if item has children, <code>false</code>
	 *         otherwise
	 */
	boolean hasItems();

	/**
	 * @return the number of children items
	 */
	int getItemCount();

	/**
	 * Sets the parent of this item.
	 * 
	 * @param parent
	 *            the parent to set
	 */
	void setParent(IExtensionItem parent);

	/**
	 * @return the parent item or <code>null</code> if this item is the root
	 */
	IExtensionItem getParent();

	/**
	 * Returns <code>true</code> if this item has the given item somewhere up
	 * the hierarchy.
	 * 
	 * @param parent
	 *            the parent to look for
	 * @return <code>true</code> if this item has the given item somewhere up
	 *         the hierarchy, <code>false</code> otherwise
	 */
	boolean hasParent(IExtensionItem parent);

	/**
	 * Sets an error for this item with the given message. May not be
	 * <code>null</code>. To reset the error a successful call to
	 * <code>updateStatus</code> is required.
	 * 
	 * @param errorMessage
	 *            the message of the error
	 */
	void setError(String errorMessage);

	/**
	 * Convenience method to update the error message for all children items
	 * recursively. The connection status will be set to erroneous with the
	 * given error message.
	 * 
	 * @param errorMessage
	 *            The error message to set
	 */
	void setChildrenError(String errorMessage);

	/**
	 * Convenience method to update the connection status for all children items
	 * recursively.
	 */
	void updateChildrenConnections();

	/**
	 * @param key
	 *            The key of the corresponding description
	 * @return The description that suits the given key
	 */
	ConfigParameterDescription getExtensionConfigParam(String key);

	/**
	 * @return collection containing all non-used and non-mandatory
	 *         configuration parameter descriptions that are editable
	 */
	Collection<ConfigParameterDescription> getConfigurableExtensionConfigParams();

	/**
	 * A convenience method to check if there are configurable parameters left.
	 * 
	 * @return <code>true</code> if configurable parameters left, otherwise
	 *         <code>false</code>
	 */
	boolean hasConfigurableExtensionConfigParams();

	/**
	 * Notifies <code>IItemChangedListener</code>s that this item's appearance
	 * has changed. Call propagates to parent items recursively, so that their
	 * listeners also get notified.
	 */
	void fireItemAppearanceChanged();

	/**
	 * Notifies <code>IItemChangedListener</code>s that the given item's
	 * appearance has changed. Call propagates to its parent items recursively,
	 * so that their listeners also get notified.
	 * 
	 * @param item
	 *            the item where the propagation starts
	 */
	void fireItemAppearanceChanged(IExtensionItem item);

	/**
	 * Notifies <code>IItemChangedListener</code>s that the given parent item
	 * has added the item specified. Call propagates to further parent items
	 * recursively, so that their listeners also get notified.
	 * 
	 * @param parent
	 *            the item where the propagation starts
	 * @param item
	 *            the item that was added to the given parent
	 */
	void fireItemChildAdded(IExtensionItem parent, IExtensionItem item);

	/**
	 * Notifies <code>IItemChangedListener</code>s that the given parent item
	 * has removed the item specified. Call propagates to further parent items
	 * recursively, so that their listeners also get notified.
	 * 
	 * @param parent
	 *            the item where the propagation starts
	 * @param item
	 *            the item that was removed from the given parent
	 */
	void fireItemChildRemoved(IExtensionItem parent, IExtensionItem item);

	/**
	 * Notifies <code>IItemPropertiesChangedListener</code>s that this item's
	 * properties have changed.
	 */
	void fireItemPropertiesChanged();

}