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

import org.spotter.eclipse.ui.handlers.DeleteHandler;
import org.spotter.eclipse.ui.menu.IDeletable;

/**
 * A decorator that adds the delete functionality to this item.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ExtensionItemDeleteDecorator extends AbstractExtensionItemDecorator {

	private static class Deletable implements IDeletable {

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

	}

	/**
	 * The constructor.
	 * 
	 * @param extensionItem
	 *            the extension item to decorate
	 */
	public ExtensionItemDeleteDecorator(IExtensionItem extensionItem) {
		super(extensionItem);
		extensionItem.addHandler(DeleteHandler.DELETE_COMMAND_ID, new Deletable());
	}

}
