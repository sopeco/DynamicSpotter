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

import java.util.List;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.spotter.eclipse.ui.editors.AbstractExtensionsEditor;
import org.spotter.eclipse.ui.model.IExtensionItem;

/**
 * A drag listener for {@link org.spotter.eclipse.ui.model.IExtensionItem}.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ExtensionDragListener implements DragSourceListener {

	public static final String VIEWER_IS_DRAG_SOURCE_PROPERTY = "isDragSource";

	private final StructuredViewer viewer;
	private final AbstractExtensionsEditor editor;
	private IExtensionItem extensionDragSource;
	private IExtensionItem extensionDragSourceParent;

	/**
	 * Creates a new drag listener for the given viewer.
	 * 
	 * @param viewer
	 *            the viewer this listener is attached to
	 * @param editor
	 *            the underlying editor
	 */
	public ExtensionDragListener(StructuredViewer viewer, AbstractExtensionsEditor editor) {
		this.viewer = viewer;
		this.editor = editor;
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		event.doit = !selection.isEmpty() && selection.getFirstElement() instanceof IExtensionItem;
		if (event.doit) {
			extensionDragSource = (IExtensionItem) selection.getFirstElement();
			extensionDragSourceParent = extensionDragSource.getParent();
			LocalSelectionTransfer.getTransfer().setSelection(selection);
			viewer.setData(VIEWER_IS_DRAG_SOURCE_PROPERTY, Boolean.TRUE);
			viewer.setData(ExtensionDropListener.VIEWER_IS_DROP_TARGET_PROPERTY, null);
		}
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		// nothing to do as we use LocalSelectionTransfer
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
		viewer.setData(VIEWER_IS_DRAG_SOURCE_PROPERTY, null);
		boolean isDropTarget = isThisViewerDropTarget();
		if (!event.doit || event.detail == DND.DROP_NONE) {
			return;
		}

		if (event.detail == DND.DROP_MOVE && !isDropTarget) {
			// remove the drag source from the model
			List<?> targetChildren = extensionDragSource.getParent().getModelWrapper().getChildren();
			List<?> sourceChildren = extensionDragSourceParent.getModelWrapper().getChildren();
			// delete extension from source children
			extensionDragSource.getModelWrapper().setXMLModelContainingList(sourceChildren);
			extensionDragSourceParent.removeItem(extensionDragSource, false);
			// set target children as new containing list
			extensionDragSource.getModelWrapper().setXMLModelContainingList(targetChildren);
			if (editor != null) {
				editor.markDirty();
			}
		}
	}

	private boolean isThisViewerDropTarget() {
		Object isDropTargetProperty = viewer.getData(ExtensionDropListener.VIEWER_IS_DROP_TARGET_PROPERTY);
		return isDropTargetProperty instanceof Boolean && (Boolean) isDropTargetProperty;
	}

}
