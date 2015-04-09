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
import org.spotter.eclipse.ui.util.SpotterUtils;

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
		IExtensionItem extension = getExtension();
		boolean success = false;

		if (extension != null) {
			int operation = getCurrentOperation();
			switch (operation) {
			case DND.DROP_MOVE:
				success = move(extension, target, getCurrentLocation(), operation, isInternal);
				break;
			case DND.DROP_COPY:
				success = copy(extension, target, getCurrentLocation(), operation, isInternal);
				break;
			default:
				break;
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
	 * @param operation
	 *            The operation that is performed.
	 * @param isInternal
	 *            Determines whether the drag 'n drop action was internal of the
	 *            viewer.
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	private boolean move(IExtensionItem extension, Object target, int location, int operation, boolean isInternal) {
		boolean success = false;
		if (target == null && !isInternal || target instanceof IExtensionItem) {
			IExtensionItem targetExtension = (IExtensionItem) target;
			IExtensionItem extensionParent = extension.getParent();
			IExtensionItem targetParent = target == null ? null : targetExtension.getParent();
			boolean equalParents = extensionParent.equals(targetParent);

			// the index of the extension only matters when parents are equal
			int index = equalParents ? extensionParent.getItemIndex(extension) : -1;
			int targetIndex = target == null ? -1 : targetParent.getItemIndex(targetExtension);
			targetIndex = fixTargetIndex(index, targetIndex, location, operation);

			if (equalParents && location != LOCATION_ON) {
				success = extensionParent.moveItem(extension, targetIndex);
			} else if (!extensionParent.equals(targetExtension) || location != LOCATION_ON) {
				if (isInternal) {
					extensionParent.removeItem(extension, false);
				}
				if (supportsHierarchy && location == LOCATION_ON) {
					targetExtension.addItem(extension);
					success = true;
				} else if (target == null) {
					// only external
					// no specific target, so just drop inside the viewer
					Object input = getViewer().getInput();
					if (input instanceof IExtensionItem) {
						IExtensionItem parent = (IExtensionItem) input;
						parent.addItem(extension);
						success = true;
					}
				} else {
					targetParent.addItem(targetIndex, extension);
					success = true;
				}
			}
		}

		return success;
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
	 * @param operation
	 *            The operation that is performed.
	 * @param isInternal
	 *            Determines whether the drag 'n drop action was internal of the
	 *            viewer.
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	private boolean copy(IExtensionItem extension, Object target, int location, int operation, boolean isInternal) {
		boolean success = false;
		IExtensionItem targetExtension = target instanceof IExtensionItem ? (IExtensionItem) target : null;
		IExtensionItem extensionParent = extension.getParent();
		IExtensionItem targetParent = targetExtension == null ? null : targetExtension.getParent();
		boolean equalParents = extensionParent.equals(targetParent);

		// the index of the extension only matters when parents are equal
		int index = equalParents ? extensionParent.getItemIndex(extension) : -1;
		int targetIndex = targetExtension == null ? -1 : targetParent.getItemIndex(targetExtension);
		targetIndex = fixTargetIndex(index, targetIndex, location, operation);
		IExtensionItem extensionCopy = null;

		if (equalParents && location != LOCATION_ON) {
			extensionCopy = extension.copyItem();
			extensionParent.addItem(targetIndex, extensionCopy);
		} else {
			if (supportsHierarchy && location == LOCATION_ON) {
				extensionCopy = extension.copyItem();
				targetExtension.addItem(extensionCopy);
			} else if (targetExtension == null) {
				// no specific target, so just drop inside the viewer
				Object input = getViewer().getInput();
				if (input instanceof IExtensionItem) {
					IExtensionItem parent = (IExtensionItem) input;
					extensionCopy = extension.copyItem();
					parent.addItem(extensionCopy);
				}
			} else {
				extensionCopy = extension.copyItem();
				targetParent.addItem(targetIndex, extensionCopy);
			}
		}

		if (extensionCopy != null) {
			extensionCopy.updateConnectionStatus();
			extensionCopy.updateChildrenConnections();
			success = true;
		}

		return success;
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
	 * @param operation
	 *            the operation that is performed
	 * @return the fixed target index
	 */
	private int fixTargetIndex(int index, int targetIndex, int location, int operation) {
		if (targetIndex == -1) {
			return targetIndex;
		}
		switch (location) {
		case LOCATION_BEFORE:
			if (index != -1 && operation == DND.DROP_MOVE && index < targetIndex) {
				targetIndex--;
			}
			break;
		case LOCATION_AFTER:
			if (operation == DND.DROP_COPY || operation == DND.DROP_MOVE && (index == -1 || index > targetIndex)) {
				targetIndex++;
			}
			break;
		default:
			break;
		}
		return targetIndex;
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
		IExtensionItem extension = isSupported ? getExtension() : null;

		boolean isCopyOp = operation == DND.DROP_COPY;
		if (extension != null && (isCopyOp || !extension.equals(target)) && hasAcceptableEditorId(extension)) {
			if (target instanceof IExtensionItem) {
				IExtensionItem targetExtension = (IExtensionItem) target;
				isValid = isCopyOp || !targetExtension.hasParent(extension);
			} else {
				isValid = target == null;
			}
		}
		return isValid;
	}

	private IExtensionItem getExtension() {
		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
		return SpotterUtils.extractFirstElement(selection, IExtensionItem.class);
	}

	private boolean hasAcceptableEditorId(IExtensionItem extension) {
		return acceptableEditorId.equals(extension.getEditorId());
	}
}
