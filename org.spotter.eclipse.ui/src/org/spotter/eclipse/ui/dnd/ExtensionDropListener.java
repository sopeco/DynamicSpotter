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
import org.eclipse.swt.dnd.TransferData;
import org.spotter.eclipse.ui.model.IExtensionItem;

/**
 * A drop listener for {@link IExtensionItem}.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ExtensionDropListener extends ViewerDropAdapter {

	private final String acceptableEditorId;

	/**
	 * Creates a new drop listener associated with the given viewer which
	 * accepts elements that suit the specified editor.
	 * 
	 * @param viewer
	 *            the viewer this listener is attached to
	 * @param acceptableEditorId
	 *            the id of the editor which will be accepted. Must not be
	 *            <code>null</code>.
	 */
	public ExtensionDropListener(Viewer viewer, String acceptableEditorId) {
		super(viewer);
		if (acceptableEditorId == null) {
			throw new IllegalArgumentException("acceptableEditorId must not be null");
		}
		this.acceptableEditorId = acceptableEditorId;
	}

	@Override
	public boolean performDrop(Object data) {
		// int location = getCurrentLocation();
		Object target = getCurrentTarget();

		IExtensionItem extension = getExtension(target);
		if (extension != null) {
			System.out.println("Dropped '" + extension + "' to target '" + target + "'");
			return true;
		}

		return false;
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		boolean isSupported = LocalSelectionTransfer.getTransfer().isSupportedType(transferType);
		return isSupported && getExtension(target) != null;
	}

	private IExtensionItem getExtension(Object target) {
		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
		IExtensionItem extension = null;
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IExtensionItem && !element.equals(target)) {
				extension = (IExtensionItem) element;
				if (!acceptableEditorId.equals(extension.getEditorId())) {
					extension = null;
				}
			}
		}

		return extension;
	}
}
