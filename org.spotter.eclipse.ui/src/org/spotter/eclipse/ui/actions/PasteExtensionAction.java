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
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.spotter.eclipse.ui.model.IExtensionItem;
import org.spotter.eclipse.ui.util.SpotterUtils;

/**
 * Class for copying a selection of extensions from a view and placing them on
 * the clipboard.
 * 
 * @author Denis Knoepfle
 * 
 */
public class PasteExtensionAction extends Action {

	private final StructuredViewer viewer;
	private final String acceptableEditorId;

	/**
	 * Creates a new action for the given viewer and clipboard.
	 * 
	 * @param viewer
	 *            the associated viewer
	 * @param acceptableEditorId
	 *            the id of the editor that accepts the copied extensions
	 */
	public PasteExtensionAction(StructuredViewer viewer, String acceptableEditorId) {
		super("Paste");
		this.viewer = viewer;
		this.acceptableEditorId = acceptableEditorId;
	}

	@Override
	public void run() {
		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
		CopyExtensionInfo info = SpotterUtils.extractFirstElement(selection, CopyExtensionInfo.class);
		if (info == null || !info.getAcceptableEditorId().equals(acceptableEditorId)) {
			return;
		}

		IExtensionItem item = info.getExtensionItem();
		IExtensionItem targetExtension = SpotterUtils.extractFirstElement(viewer.getSelection(), IExtensionItem.class);
		IExtensionItem targetParent = targetExtension == null ? null : targetExtension.getParent();

		if (targetParent == null && viewer.getInput() instanceof IExtensionItem) {
			targetParent = (IExtensionItem) viewer.getInput();
		}

		if (targetParent != null) {
			IExtensionItem itemCopy = item.copyItem();
			targetParent.addItem(itemCopy);

			itemCopy.updateConnectionStatus();
			itemCopy.updateChildrenConnections();
		}
	}

}
