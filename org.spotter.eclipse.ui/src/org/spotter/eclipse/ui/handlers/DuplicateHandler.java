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

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ProjectLocationSelectionDialog;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;

/**
 * A duplicate handler for the duplicate command which duplicates the selected project. The handler
 * is only enabled when a single project is selected in the Spotter Project Navigator.
 * 
 * @author Denis Knoepfle
 * 
 */
public class DuplicateHandler extends AbstractHandler {

	/**
	 * The id of the duplicate command.
	 */
	public static final String DUPLICATE_COMMAND_ID = "org.spotter.eclipse.ui.commands.duplicate";

	private static final String DLG_TITLE = "Duplicate Project";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator activator = Activator.getDefault();
		if (activator.getSelectedProjects().size() != 1) {
			return null;
		}
		IProject project = activator.getSelectedProjects().iterator().next();
		Shell shell = Display.getDefault().getActiveShell();
		ProjectLocationSelectionDialog dialog = new ProjectLocationSelectionDialog(shell, project);
		dialog.setTitle(DLG_TITLE);
		if (dialog.open() != Window.OK) {
			return null;
		}
		Object[] result = dialog.getResult();
		String duplicatedProjectName = result[0].toString();
		String destination = result[1].toString() + File.separator + duplicatedProjectName;

		try {
			IProjectDescription description = project.getDescription();
			IPath defaultDefaultLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			if (!defaultDefaultLocation.equals(new Path(result[1].toString()))) {
				description.setLocation(new Path(destination));
			}
			description.setName(duplicatedProjectName);
			project.copy(description, true, null);
			
			// update references in the Spotter configuration file for the new project
			IProject duplicatedProject = project.getWorkspace().getRoot().getProject(duplicatedProjectName);
			SpotterProjectSupport.updateSpotterConfig(duplicatedProject);
		} catch (Exception e) {
			MessageDialog.openError(shell, DLG_TITLE, "Error while copying project '" + project.getName() + "'!");
		}

		return null;
	}

	@Override
	public boolean isEnabled() {
		return Activator.getDefault().getSelectedProjects().size() == 1;
	}

}
