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
package org.spotter.eclipse.ui.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.navigator.IDuplicatable;
import org.spotter.eclipse.ui.util.SpotterUtils;

/**
 * A duplicate handler for the duplicate command which duplicates the selected
 * element. The handler is only enabled when a single, duplicatable element is
 * selected in the DynamicSpotter Project Navigator.
 * 
 * @author Denis Knoepfle
 * 
 */
public class DuplicateHandler extends AbstractHandler {

	/**
	 * The id of the duplicate command.
	 */
	public static final String DUPLICATE_COMMAND_ID = "org.spotter.eclipse.ui.commands.duplicate";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator activator = Activator.getDefault();
		TreeViewer viewer = activator.getNavigatorViewer();
		if (viewer == null) {
			return null;
		}

		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		Iterator<?> iter = selection.iterator();
		while (iter.hasNext()) {
			SpotterUtils.duplicateNavigatorElement(iter.next());
		}

		return null;
	}

	/**
	 * Returns <code>true</code> when exactly one duplicatable is selected.
	 * 
	 * @return <code>true</code> when exactly one duplicatable is selected
	 */
	@Override
	public boolean isEnabled() {
		Activator activator = Activator.getDefault();
		TreeViewer viewer = activator.getNavigatorViewer();
		if (viewer == null) {
			return false;
		}

		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection.size() != 1) {
			return false;
		}

		// check if selected element is duplicatable
		Object element = selection.iterator().next();
		boolean isDuplicatable = (element instanceof IDuplicatable);

		return isDuplicatable;
	}

}
