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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.spotter.eclipse.ui.model.IExtensionItem;
import org.spotter.eclipse.ui.util.SpotterUtils;

/**
 * Class for copying a selection of extensions from a view and placing them on
 * the clipboard.
 * 
 * @author Denis Knoepfle
 * 
 */
public class CopyExtensionAction extends Action {

	private final Clipboard clipboard;
	private final StructuredViewer viewer;
	private final String acceptableEditorId;

	/**
	 * Creates a new action for the given viewer and clipboard.
	 * 
	 * @param viewer
	 *            the associated viewer
	 * @param clipboard
	 *            the clipboard to use
	 * @param acceptableEditorId
	 *            the id of the editor that may accept the copied extensions
	 */
	public CopyExtensionAction(StructuredViewer viewer, Clipboard clipboard, String acceptableEditorId) {
		super("Copy");
		this.viewer = viewer;
		this.clipboard = clipboard;
		this.acceptableEditorId = acceptableEditorId;
	}

	@Override
	public void run() {
		IExtensionItem item = SpotterUtils.extractFirstElement(viewer.getSelection(), IExtensionItem.class);
		if (item == null) {
			return;
		}

		CopyExtensionInfo info = new CopyExtensionInfo(item, acceptableEditorId);
		// use LocalSelectionTransfer for internal copy/paste process
		LocalSelectionTransfer.getTransfer().setSelection(new StructuredSelection(info));
		// copy text to system clipboard for external usage
		clipboard.setContents(new Object[] { item.toString() }, new Transfer[] { TextTransfer.getInstance() });
	}

}
