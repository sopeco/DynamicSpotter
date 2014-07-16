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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.editors.AbstractSpotterEditorInput;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;

/**
 * A delete handler for the delete command which deletes selected projects in
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

	private static final String DLG_TITLE = "Delete Resources";
	private static final String MSG_SINGLE = "Are you sure you want to remove project '%s' from the workspace?\n\nWarning: Contents will be deleted from disk!";
	private static final String MSG_MULTI = "Are you sure you want to remove the following projects from the workspace?\n\n%s\n\nWarning: Contents will be deleted from disk!";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator activator = Activator.getDefault();
		Set<IProject> projects = activator.getSelectedProjects();
		if (projects.isEmpty()) {
			return null;
		}
		Shell shell = Display.getDefault().getActiveShell();
		String prompt;
		if (projects.size() > 1) {
			prompt = createMultiMessage(projects);
		} else {
			prompt = createSingleMessage(projects.iterator().next());
		}

		boolean confirm = MessageDialog.openConfirm(shell, DLG_TITLE, prompt);
		if (!confirm) {
			return null;
		}

		List<IProject> projectsDeletionFailed = new ArrayList<>();
		List<String> deletionErrorMessages = new ArrayList<>();
		while (!projects.isEmpty()) {
			IProject project = projects.iterator().next();
			try {
				deleteProject(shell, project);
			} catch (CoreException e) {
				projectsDeletionFailed.add(project);
				deletionErrorMessages.add(e.getMessage());
			}
			projects.remove(project);
		}
		if (!projectsDeletionFailed.isEmpty()) {
			String errorMessage = createErrorMessage(projectsDeletionFailed, deletionErrorMessages);
			MessageDialog.openError(shell, DLG_TITLE, errorMessage);
		}

		return null;
	}

	private String createErrorMessage(List<IProject> projects, List<String> detailErrorMessages) {
		StringBuilder sb = new StringBuilder();
		sb.append("Error while deleting project(s): ");
		sb.append(concatProjectNames(projects) + "!\n");

		for (String errMsg : detailErrorMessages) {
			sb.append("\nCause: " + errMsg);
		}
		return sb.toString();
	}

	private void deleteProject(Shell shell, IProject project) throws CoreException {
		// close open editors that refer to the project
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				List<IEditorReference> closeEditors = new ArrayList<IEditorReference>();
				for (IEditorReference ref : page.getEditorReferences()) {
					IEditorPart editorPart = ref.getEditor(true);
					if (editorPart != null && editorPart.getEditorInput() instanceof AbstractSpotterEditorInput) {
						AbstractSpotterEditorInput input = (AbstractSpotterEditorInput) editorPart.getEditorInput();
						if (project.equals(input.getProject())) {
							closeEditors.add(ref);
						}
					}
				}
				page.closeEditors(closeEditors.toArray(new IEditorReference[closeEditors.size()]), false);
			}
		}
		// deletes project completely from disk
		// TODO: adjust dialog to allow soft deletion only from workspace
		// without deleting it from disk
		project.delete(true, true, null);
		SpotterProjectSupport.deleteProjectPreferences(project.getName());
	}

	private String concatProjectNames(Collection<IProject> projects) {
		Iterator<IProject> iterator = projects.iterator();
		StringBuilder sb = new StringBuilder();
		sb.append("'" + iterator.next().getName() + "'");
		while (iterator.hasNext()) {
			sb.append(", '" + iterator.next().getName() + "'");
		}
		return sb.toString();
	}

	private String createSingleMessage(IProject project) {
		return String.format(MSG_SINGLE, project.getName());
	}

	private String createMultiMessage(Collection<IProject> projects) {
		return String.format(MSG_MULTI, concatProjectNames(projects));
	}

	@Override
	public boolean isEnabled() {
		return !Activator.getDefault().getSelectedProjects().isEmpty();
	}

}
