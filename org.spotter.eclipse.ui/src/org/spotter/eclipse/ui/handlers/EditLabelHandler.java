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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.navigator.SpotterProjectRunResult;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.eclipse.ui.util.SpotterUtils;
import org.spotter.eclipse.ui.view.ResultsView;

/**
 * A handler for the edit label command which edits the label of the element.
 * 
 * @author Denis Knoepfle
 * 
 */
public class EditLabelHandler extends AbstractHandler {

	/**
	 * The id of the corresponding edit label command.
	 */
	public static final String EDIT_LABEL_COMMAND_ID = "org.spotter.eclipse.ui.commands.editLabel";

	private static final String DLG_MESSAGE = "Enter a label for this element:";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object element = SpotterUtils.getActiveWindowStructuredSelectionIterator().next();
		SpotterProjectRunResult runResult = (SpotterProjectRunResult) element;
		String label = runResult.getElementLabel();

		InputDialog dialog = new InputDialog(null, DialogUtils.DEFAULT_DLG_TITLE, DLG_MESSAGE, label, null);
		if (dialog.open() == Window.OK) {
			runResult.updateElementLabel(dialog.getValue());
			Activator.getDefault().getNavigatorViewer().refresh(runResult);
			ResultsView.updateContentDescription();
		}

		return null;
	}

	@Override
	public boolean isEnabled() {
		boolean enabled = false;
		ISelection selection = SpotterUtils.getActiveWindowSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.size() == 1
					&& structuredSelection.getFirstElement() instanceof SpotterProjectRunResult) {
				enabled = true;
			}
		}

		return enabled;
	}

}
