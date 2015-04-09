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
import org.eclipse.jface.viewers.StructuredViewer;
import org.spotter.eclipse.ui.model.IExtensionItem;
import org.spotter.eclipse.ui.util.SpotterUtils;

/**
 * Class for cutting a selection of extensions from a view and placing them on
 * the clipboard.
 * 
 * @author Denis Knoepfle
 * 
 */
public class CutExtensionAction extends Action {

	private final StructuredViewer viewer;
	private final CopyExtensionAction copyAction;
	private final DeleteExtensionAction deleteAction;

	/**
	 * Creates a new action for the given viewer and clipboard.
	 * 
	 * @param viewer
	 *            the associated viewer
	 * @param copyAction
	 *            the copy part of the cut action
	 * @param deleteAction
	 *            the delete part of the cut action
	 */
	public CutExtensionAction(StructuredViewer viewer, CopyExtensionAction copyAction,
			DeleteExtensionAction deleteAction) {
		super("Cut");
		this.viewer = viewer;
		this.copyAction = copyAction;
		this.deleteAction = deleteAction;
	}

	@Override
	public void run() {
		IExtensionItem item = SpotterUtils.extractFirstElement(viewer.getSelection(), IExtensionItem.class);
		if (item == null) {
			return;
		}

		copyAction.run();
		deleteAction.run();
	}

}
