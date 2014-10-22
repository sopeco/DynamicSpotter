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

	@Override
	public IExtensionItem createExtensionItem() {
		IExtensionItem basicItem = new ExtensionItem();
		return createEnhancedExtensionItem(basicItem);
	}

	@Override
	public IExtensionItem createExtensionItem(IModelWrapper modelWrapper) {
		IExtensionItem basicItem = new ExtensionItem(modelWrapper);
		return createEnhancedExtensionItem(basicItem);
	}

	@Override
	public IExtensionItem createExtensionItem(IExtensionItem parent, IModelWrapper modelWrapper) {
		IExtensionItem basicItem = new ExtensionItem(parent, modelWrapper);
		return createEnhancedExtensionItem(basicItem);
	}

	private IExtensionItem createEnhancedExtensionItem(IExtensionItem basicItem) {
		IExtensionItem deletable = new ExtensionItemDeleteDecorator(basicItem);

		return deletable;
	}

}
