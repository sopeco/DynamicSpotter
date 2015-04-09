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
package org.spotter.eclipse.ui.actions;

import org.spotter.eclipse.ui.model.IExtensionItem;

/**
 * Class for storing information for the {@link CopyExtensionAction} what and
 * how to copy the data.
 * 
 * @author Denis Knoepfle
 * 
 */
public class CopyExtensionInfo {

	private final IExtensionItem extensionItem;
	private final String acceptableEditorId;

	/**
	 * Creates a new info for the given extension item that is applicable for
	 * the given editor id.
	 * 
	 * @param extensionItem
	 *            the extension item to copy
	 * @param acceptableEditorId
	 *            the id of the editor the extension is valid for
	 */
	public CopyExtensionInfo(IExtensionItem extensionItem, String acceptableEditorId) {
		this.extensionItem = extensionItem;
		this.acceptableEditorId = acceptableEditorId;
	}

	/**
	 * @return the extension item
	 */
	public IExtensionItem getExtensionItem() {
		return extensionItem;
	}

	/**
	 * @return the acceptable editor id
	 */
	public String getAcceptableEditorId() {
		return acceptableEditorId;
	}

}
