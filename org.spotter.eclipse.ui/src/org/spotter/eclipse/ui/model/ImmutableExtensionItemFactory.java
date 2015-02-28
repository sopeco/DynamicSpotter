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
 * Factory to create immutable extension items without any additional
 * functionality.
 * 
 * @author Denis Knöpfle
 * 
 */
public class ImmutableExtensionItemFactory implements IExtensionItemFactory {

	private final String editorId;

	/**
	 * Creates a basic extension item factory for the given editor.
	 * 
	 * @param editorId
	 *            the id of the editor the created extensions will be assigned
	 *            to
	 */
	public ImmutableExtensionItemFactory(String editorId) {
		this.editorId = editorId;
	}

	@Override
	public IExtensionItem createExtensionItem() {
		return new ExtensionItem(editorId);
	}

	@Override
	public IExtensionItem createExtensionItem(IModelWrapper modelWrapper) {
		return new ExtensionItem(modelWrapper, editorId);
	}

	@Override
	public IExtensionItem createExtensionItem(IExtensionItem parent, IModelWrapper modelWrapper) {
		return new ExtensionItem(parent, modelWrapper, editorId);
	}

}
