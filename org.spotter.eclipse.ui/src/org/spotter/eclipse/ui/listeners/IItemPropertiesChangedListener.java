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

/**
 * A listener which is notified of property change events.
 * 
 * @author Denis Knoepfle
 * 
 */
public interface IItemPropertiesChangedListener {

	/**
	 * Called when the properties have changed. This can mean that properties might have been
	 * removed or changed or that new ones have been added.
	 */
	public void propertiesChanged();

	/**
	 * Called when a property item has been removed.
	 * 
	 * @param propertyItem
	 *            the property item that has been removed
	 */
	public void itemPropertyRemoved(Object propertyItem);

	/**
	 * Called when a property of an item has changed.
	 * 
	 * @param propertyItem
	 *            the changed property item
	 */
	public void itemPropertyChanged(Object propertyItem);

}
