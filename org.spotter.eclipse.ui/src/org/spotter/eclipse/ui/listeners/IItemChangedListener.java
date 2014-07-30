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
package org.spotter.eclipse.ui.listeners;

import org.spotter.eclipse.ui.model.ExtensionItem;

/**
 * A listener which is notified of structure modification or appearance change
 * events.
 * 
 * @author Denis Knoepfle
 * 
 */
public interface IItemChangedListener {

	/**
	 * Called when a child is added.
	 * 
	 * @param parent
	 *            the parent under which the item was added
	 * @param item
	 *            the added item
	 */
	void childAdded(ExtensionItem parent, ExtensionItem item);

	/**
	 * Called when a child is removed.
	 * 
	 * @param parent
	 *            the parent under which the item was removed
	 * @param item
	 *            the removed item
	 */
	void childRemoved(ExtensionItem parent, ExtensionItem item);

	/**
	 * Called when the appearance of the item changed.
	 * 
	 * @param item
	 *            the changed item
	 */
	void appearanceChanged(ExtensionItem item);

}
