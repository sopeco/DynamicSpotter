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
import org.eclipse.swt.dnd.Clipboard;

/**
 * Class for cutting a selection of extensions from a view and placing them on
 * the clipboard.
 * 
 * @author Denis Knoepfle
 * 
 */
public class CutExtensionAction extends Action {

	//private final Clipboard clipboard;
	//private final StructuredViewer viewer;

	/**
	 * Creates a new action for the given viewer and clipboard.
	 * 
	 * @param viewer
	 *            the associated viewer
	 * @param clipboard
	 *            the clipboard to use
	 */
	public CutExtensionAction(StructuredViewer viewer, Clipboard clipboard) {
		super("Cut");
		//this.viewer = viewer;
		//this.clipboard = clipboard;
	}

	@Override
	public void run() {
		// TODO: implement cut action
	}

}
