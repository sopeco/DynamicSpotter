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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.spotter.eclipse.ui.editors.AbstractExtensionsEditor;
import org.spotter.eclipse.ui.model.IExtensionItem;

/**
 * Class for deleting a selection of extensions from a view.
 * 
 * @author Denis Knoepfle
 * 
 */
public class DeleteExtensionAction extends Action {

	private final AbstractExtensionsEditor editor;
	private final StructuredViewer viewer;

	/**
	 * Creates a new action for the given viewer and editor.
	 * 
	 * @param viewer
	 *            the associated viewer
	 * @param editor
	 *            the associated editor
	 */
	public DeleteExtensionAction(StructuredViewer viewer, AbstractExtensionsEditor editor) {
		super("Delete");
		this.viewer = viewer;
		this.editor = editor;
	}

	@Override
	public void run() {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		if (sel.isEmpty()) {
			return;
		}
		IExtensionItem item = (IExtensionItem) sel.getFirstElement();
		IExtensionItem parentItem = item.getParent();
		int index = parentItem.getItemIndex(item);
		if (index != -1) {
			parentItem.removeItem(index, true);
			if (parentItem.hasItems()) {
				// parent still has items left, so select next child
				index = Math.min(index, parentItem.getItemCount() - 1);
				viewer.setSelection(new StructuredSelection(parentItem.getItem(index)));
			} else if (parentItem != viewer.getInput()) {
				// root not reached yet, so select parent item
				viewer.setSelection(new StructuredSelection(parentItem));
			}
			editor.markDirty();
		}
	}

}
