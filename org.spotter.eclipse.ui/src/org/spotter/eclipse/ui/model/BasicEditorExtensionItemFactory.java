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

import org.spotter.eclipse.ui.model.xml.IModelWrapper;

/**
 * Factory to create basic extension items with expected standard editor
 * functionality like copy and paste and the like.
 * 
 * @author Denis Knoepfle
 * 
 */
public class BasicEditorExtensionItemFactory implements IExtensionItemFactory {

	private final String editorId;

	/**
	 * Creates a basic extension item factory for the given editor.
	 * 
	 * @param editorId
	 *            the id of the editor the created extensions will be assigned
	 *            to
	 */
	public BasicEditorExtensionItemFactory(String editorId) {
		this.editorId = editorId;
	}

	@Override
	public IExtensionItem createExtensionItem() {
		IExtensionItem basicItem = new ExtensionItem(editorId);
		enhanceExtensionItem(basicItem);
		return basicItem;
	}

	@Override
	public IExtensionItem createExtensionItem(IModelWrapper modelWrapper) {
		IExtensionItem basicItem = new ExtensionItem(modelWrapper, editorId);
		enhanceExtensionItem(basicItem);
		return basicItem;
	}

	@Override
	public IExtensionItem createExtensionItem(IExtensionItem parent, IModelWrapper modelWrapper) {
		IExtensionItem basicItem = new ExtensionItem(parent, modelWrapper, editorId);
		enhanceExtensionItem(basicItem);
		return basicItem;
	}

	private void enhanceExtensionItem(IExtensionItem basicItem) {
		//addDeleteHandler(basicItem);
	}

	/*private void addDeleteHandler(IExtensionItem basicItem) {
		basicItem.addHandler(DeleteHandler.DELETE_COMMAND_ID, new IDeletable() {

			private static final String ELEMENT_TYPE_NAME = "Extension Item";

			@Override
			public void delete() {
				// TODO: implement delete here
			}

			@Override
			public void delete(Object[] elements) {
				// TODO: implement delete here
			}

			@Override
			public String getElementTypeName() {
				return ELEMENT_TYPE_NAME;
			}

			@Override
			public boolean showConfirmationDialog(Object[] elements) {
				// TODO: implement confirmation here
				return false;
			}

		});
	}*/

}
