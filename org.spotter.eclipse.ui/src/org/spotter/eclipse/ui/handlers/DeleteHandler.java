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
import org.spotter.eclipse.ui.navigator.IDeletable;
import org.spotter.eclipse.ui.util.SpotterUtils;

/**
 * A delete handler for the delete command which deletes selected elements in
 * the Spotter Project Navigator.
 * 
 * @author Denis Knoepfle
 * 
 */
public class DeleteHandler extends AbstractHandler {

	/**
	 * The id of the corresponding delete command.
	 */
	public static final String DELETE_COMMAND_ID = "org.spotter.eclipse.ui.commands.delete";

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
			SpotterUtils.deleteNavigatorElement(iter.next());
		}

		return null;
	}

	/**
	 * Only allow deletion if just elements are selected that are deletable.
	 */
	@Override
	public boolean isEnabled() {
		Activator activator = Activator.getDefault();
		TreeViewer viewer = activator.getNavigatorViewer();
		if (viewer == null) {
			return false;
		}

		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection.isEmpty()) {
			return false;
		}
		Iterator<?> iter = selection.iterator();

		while (iter.hasNext()) {
			if (!(iter.next() instanceof IDeletable)) {
				return false;
			}
		}

		return true;
	}

}
