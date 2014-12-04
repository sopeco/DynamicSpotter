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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.spotter.eclipse.ui.menu.IDeletable;
import org.spotter.eclipse.ui.util.SpotterUtils;

/**
 * A delete handler for the delete command which deletes selected elements in
 * the DynamicSpotter Project Navigator.
 * 
 * @author Denis Knoepfle
 * 
 */
public class DeleteHandler extends AbstractHandler implements IElementUpdater {

	/**
	 * The id of the corresponding delete command.
	 */
	public static final String DELETE_COMMAND_ID = "org.spotter.eclipse.ui.commands.delete";

	/**
	 * The default label used for the command in the popup menu
	 */
	private static final String DEFAULT_LABEL = "Delete";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = SpotterUtils.getActiveWindowSelection();
		
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object[] elements = ((IStructuredSelection) selection).toArray();
			SpotterUtils.deleteElements(elements);
		}

		return null;
	}

	/**
	 * Only allow deletion if the selection contains just deletable elements of
	 * the same type.
	 * 
	 * @return <code>true</code> if only deletable elements of same type are
	 *         selected
	 */
	@Override
	public boolean isEnabled() {
		return !getSelectedDeletables().isEmpty();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void updateElement(UIElement element, Map parameters) {
		List<IDeletable> deletables = getSelectedDeletables();

		String label = getLabelForDeletables(deletables);
		element.setText(label);
	}

	private String getLabelForDeletables(List<IDeletable> deletables) {
		IDeletable deletable = deletables.isEmpty() ? null : deletables.get(0);
		if (deletable == null) {
			return DEFAULT_LABEL;
		}

		String pluralLetter = deletables.size() > 1 ? "s" : "";
		String label = "Delete " + deletable.getElementTypeName() + pluralLetter;

		return label;
	}

	// returns deletable elements of same type that are selected or empty list
	private List<IDeletable> getSelectedDeletables() {
		List<IDeletable> deletables = new ArrayList<>();
		Iterator<?> iter = SpotterUtils.getActiveWindowStructuredSelectionIterator();
		if (iter == null) {
			return deletables;
		}

		Class<?> firstDeletableClazz = null;

		while (iter.hasNext()) {
			Object selectedElement = iter.next();
			Object deletable = SpotterUtils.toConcreteHandler(selectedElement, DELETE_COMMAND_ID);
			if (deletable instanceof IDeletable) {
				Class<?> clazz = selectedElement.getClass();
				if (firstDeletableClazz == null) {
					firstDeletableClazz = clazz;
					deletables.add((IDeletable) deletable);
				} else if (!firstDeletableClazz.equals(clazz)) {
					// only allow same types in one delete
					deletables.clear();
					return deletables;
				} else {
					// just add the element so it can be counted afterwards
					deletables.add((IDeletable) deletable);
				}
			} else {
				// if any element not deletable return empty list
				deletables.clear();
				return deletables;
			}
		}
		return deletables;
	}

}
