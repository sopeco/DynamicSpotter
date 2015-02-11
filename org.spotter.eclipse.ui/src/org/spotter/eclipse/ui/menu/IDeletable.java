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
package org.spotter.eclipse.ui.menu;

import org.eclipse.core.runtime.CoreException;

/**
 * An interface for elements that can be deleted.
 * 
 * @author Denis Knoepfle
 * 
 */
public interface IDeletable {

	/**
	 * Title for delete dialogs.
	 */
	String DELETE_DLG_TITLE = "Delete";

	/**
	 * Deletes this element.
	 * 
	 * @throws CoreException
	 *             if error occurs during deletion
	 */
	void delete() throws CoreException;

	/**
	 * Deletes all elements which are expected to be of the same class as the
	 * implementing class. Clients should prefer this method over single
	 * deletions due to performance reasons.
	 * 
	 * @param elements
	 *            the elements to delete
	 * @throws CoreException
	 *             if error occurs during deletion
	 */
	void delete(Object[] elements) throws CoreException;

	/**
	 * Returns the name of this element type that should be used within the
	 * label for the delete command.
	 * 
	 * @return The name of this element type
	 */
	String getElementTypeName();

	/**
	 * Opens a confirmation dialog to delete the given elements which are
	 * assumed to be of the same type as this element.
	 * 
	 * @param elements
	 *            the elements the confirmation is done for
	 * @return <code>true</code> to proceed with deletion, <code>false</code>
	 *         otherwise
	 */
	boolean showConfirmationDialog(Object[] elements);

}
