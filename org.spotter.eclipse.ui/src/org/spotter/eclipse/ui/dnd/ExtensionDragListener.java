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
package org.spotter.eclipse.ui.dnd;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

/**
 * A drag listener for {@link org.spotter.eclipse.ui.model.IExtensionItem}.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ExtensionDragListener implements DragSourceListener {

	private final StructuredViewer viewer;

	/**
	 * Creates a new drag listener for the given viewer.
	 * 
	 * @param viewer
	 *            the viewer this listener is attached to
	 */
	public ExtensionDragListener(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		event.doit = !viewer.getSelection().isEmpty();
		if (event.doit) {
			LocalSelectionTransfer.getTransfer().setSelection(viewer.getSelection());
		}
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		// nothing to do as we use LocalSelectionTransfer
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
		if (!event.doit || event.detail == DND.DROP_NONE) {
			return;
		}
		
		//if (event.detail == DND.DROP_MOVE) {
			// TODO: remove the drag source from the model
		//}
	}

}
