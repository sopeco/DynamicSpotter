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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;
import org.spotter.eclipse.ui.editors.AbstractExtensionsEditor;
import org.spotter.eclipse.ui.model.IExtensionItem;

/**
 * A drop listener for {@link IExtensionItem}.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ExtensionDropListener extends ViewerDropAdapter {

	public static final String VIEWER_IS_DROP_TARGET_PROPERTY = "isDropTarget";

	private final AbstractExtensionsEditor editor;
	private final String acceptableEditorId;
	private final boolean supportsHierarchy;

	/**
	 * Creates a new drop listener associated with the given viewer which
	 * accepts elements that suit the specified editor.
	 * 
	 * @param viewer
	 *            the viewer this listener is attached to
	 * @param editor
	 *            the underlying editor. Must not be <code>null</code>.
	 * @param supportsHierarchy
	 *            whether the elements support hierarchy. If this is set to
	 *            <code>true</code> then dropping an item onto a target directly
	 *            adds it as a child rather than a sibling.
	 */
	public ExtensionDropListener(Viewer viewer, AbstractExtensionsEditor editor, boolean supportsHierarchy) {
		super(viewer);
		if (editor == null) {
			throw new IllegalArgumentException("editor must not be null");
		}
		this.editor = editor;
		this.acceptableEditorId = editor.getEditorId();
		this.supportsHierarchy = supportsHierarchy;
	}

	@Override
	public boolean performDrop(Object data) {
		boolean isInternal = isThisViewerDragSource();
		Object target = getCurrentTarget();
		IExtensionItem extension = getExtension(target);
		boolean success = false;

		if (extension != null) {
			if (getCurrentOperation() == DND.DROP_MOVE) {
				success = move(extension, target, getCurrentLocation(), isInternal);
			} else {
				success = copy(extension, target, getCurrentLocation(), isInternal);
			}
		}

		if (success) {
			getViewer().setData(VIEWER_IS_DROP_TARGET_PROPERTY, Boolean.TRUE);
			editor.markDirty();
		}
		return success;
	}

	/**
	 * Moves the extension to the target respecting the location where it was
	 * dropped.
	 * 
	 * @param extension
	 *            The extension to move.
	 * @param target
	 *            The target extension, may be <code>null</code>.
	 * @param location
	 *            The location of the drop.
	 * @param isInternal
	 *            Determines whether the drag 'n drop action was internal of the
	 *            viewer.
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	private boolean move(IExtensionItem extension, Object target, int location, boolean isInternal) {
		if (isInternal) {
			if (target instanceof IExtensionItem) {
				IExtensionItem targetExtension = (IExtensionItem) target;
				IExtensionItem extensionParent = extension.getParent();
				IExtensionItem targetParent = targetExtension.getParent();
				boolean equalParents = extensionParent.equals(targetParent);

				// the index of the extension only matters when parents are
				// equal
				int index = equalParents ? extensionParent.getItemIndex(extension) : -1;
				int targetIndex = targetParent.getItemIndex(targetExtension);
				targetIndex = fixTargetIndex(index, targetIndex, location);

				if (equalParents && location != LOCATION_ON) {
					return extensionParent.moveItem(extension, targetIndex);
				} else if (!targetExtension.equals(extensionParent) || location != LOCATION_ON) {
					extensionParent.removeItem(extension, false);
					if (supportsHierarchy && location == LOCATION_ON) {
						targetExtension.addItem(extension);
					} else {
						targetParent.addItem(targetIndex, extension);
					}
					return true;
				}
			}

		}

		return false;
	}

	/**
	 * Fixes the target index.
	 * 
	 * @param index
	 *            the current index of the item
	 * @param targetIndex
	 *            the target index of the item
	 * @param location
	 *            the location of the drop
	 * @return the fixed target index
	 */
	private int fixTargetIndex(int index, int targetIndex, int location) {
		switch (location) {
		case LOCATION_BEFORE:
			if (index != -1 && index < targetIndex) {
				targetIndex--;
			}
			break;
		case LOCATION_AFTER:
			if (index == -1 || index > targetIndex) {
				targetIndex++;
			}
			break;
		default:
			break;
		}
		return targetIndex;
	}

	/**
	 * Copies the extension to the target respecting the location where it was
	 * dropped.
	 * 
	 * @param extension
	 *            The extension to copy.
	 * @param target
	 *            The target extension, may be <code>null</code>.
	 * @param location
	 *            The location of the drop.
	 * @param isInternal
	 *            Determines whether the drag 'n drop action was internal of the
	 *            viewer.
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	private boolean copy(IExtensionItem extension, Object target, int location, boolean isInternal) {
		return false;
	}

	private boolean isThisViewerDragSource() {
		Object isDragSourceProperty = getViewer().getData(ExtensionDragListener.VIEWER_IS_DRAG_SOURCE_PROPERTY);
		return isDragSourceProperty instanceof Boolean && (Boolean) isDragSourceProperty;
	}

	@Override
	protected int determineLocation(DropTargetEvent event) {
		if (supportsHierarchy) {
			return super.determineLocation(event);
		}

		if (!(event.item instanceof Item)) {
			return LOCATION_NONE;
		}

		Item item = (Item) event.item;
		Point coordinates = new Point(event.x, event.y);
		coordinates = getViewer().getControl().toControl(coordinates);
		Rectangle bounds = getBounds(item);
		if (bounds == null) {
			return LOCATION_NONE;
		}
		if (coordinates.y - bounds.y <= bounds.height / 2) {
			return LOCATION_BEFORE;
		} else {
			return LOCATION_AFTER;
		}
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		boolean isValid = false;

		boolean isSupported = LocalSelectionTransfer.getTransfer().isSupportedType(transferType);
		IExtensionItem extension = isSupported ? getExtension(target) : null;

		if (extension != null && !extension.equals(target) && hasAcceptableEditorId(extension)) {
			if (target instanceof IExtensionItem) {
				IExtensionItem targetExtension = (IExtensionItem) target;
				isValid = !targetExtension.hasParent(extension);
			} else {
				isValid = true;
			}
		}
		return isValid;
	}

	private IExtensionItem getExtension(Object target) {
		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
		IExtensionItem extension = null;
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IExtensionItem) {
				extension = (IExtensionItem) element;
			}
		}

		return extension;
	}

	private boolean hasAcceptableEditorId(IExtensionItem extension) {
		return acceptableEditorId.equals(extension.getEditorId());
	}
}
