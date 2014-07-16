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
package org.spotter.eclipse.ui.navigator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ProjectLocationSelectionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;

/**
 * This is the parent element of all other items and represents the project
 * node.
 * 
 * @author Denis Knoepfle
 * 
 */
public class SpotterProjectParent implements ISpotterProjectElement, IDeletable, IDuplicatable {

	public static final ISpotterProjectElement[] NO_CHILDREN = new ISpotterProjectElement[0];
	public static final String IMAGE_PATH = "icons/project.gif"; //$NON-NLS-1$

	private static final Logger LOGGER = LoggerFactory.getLogger(SpotterProjectParent.class);
	private static final String DUPLICATE_DLG_TITLE = "Duplicate Project";
	private static final String DELETE_DLG_TITLE = "Delete Resources";
	private static final String MSG_SINGLE = "Are you sure you want to remove project '%s' from the workspace?\n\nWarning: Contents will be deleted from disk!";
	private static final String MSG_MULTI = "Are you sure you want to remove the following projects from the workspace?\n\n%s\n\nWarning: Contents will be deleted from disk!";

	private IProject project;
	private ISpotterProjectElement[] children;
	private Image image;

	/**
	 * Creates a new instance of this element.
	 * 
	 * @param project
	 *            the associated project
	 */
	public SpotterProjectParent(IProject project) {
		this.project = project;
	}

	@Override
	public String getText() {
		return project.getName();
	}

	@Override
	public Image getImage() {
		if (image == null) {
			image = Activator.getImage(IMAGE_PATH);
		}

		return image;
	}

	@Override
	public Object[] getChildren() {
		if (children == null) {
			children = initializeChildren(project);
		}
		// else we have already initialized them

		return children;
	}

	@Override
	public boolean hasChildren() {
		if (children == null) {
			children = initializeChildren(project);
		}
		return children.length > 0;
	}

	@Override
	public Object getParent() {
		return null;
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SpotterProjectParent)) {
			return false;
		}
		SpotterProjectParent other = (SpotterProjectParent) obj;
		return project.equals(other.project);
	}

	@Override
	public int hashCode() {
		return getProject().getName().hashCode();
	}

	private ISpotterProjectElement[] initializeChildren(IProject project) {
		ISpotterProjectElement[] children = { new SpotterProjectConfig(this), new SpotterProjectHierarchy(this),
				new SpotterProjectResults(this) };

		return children;
	}

	@Override
	public void duplicate() {
		IProject project = getProject();
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		ProjectLocationSelectionDialog dialog = new ProjectLocationSelectionDialog(shell, project);
		dialog.setTitle(DUPLICATE_DLG_TITLE);
		if (dialog.open() != Window.OK) {
			return;
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

			// update references in the Spotter configuration file for the new
			// project
			IProject duplicatedProject = project.getWorkspace().getRoot().getProject(duplicatedProjectName);
			SpotterProjectSupport.updateSpotterConfig(duplicatedProject);
		} catch (Exception e) {
			String msg = "Error while copying project '" + project.getName() + "'!";
			LOGGER.error(DialogUtils.appendCause(msg, e.getMessage()));
			DialogUtils.openError(DUPLICATE_DLG_TITLE, msg);
		}
	}

	@Override
	public void delete() {
		Activator activator = Activator.getDefault();
		Set<IProject> projects = activator.getSelectedProjects();
		if (projects.isEmpty()) {
			return;
		}

		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		String prompt;
		if (projects.size() > 1) {
			prompt = createMultiMessage(projects);
		} else {
			prompt = createSingleMessage(projects.iterator().next());
		}

		boolean confirm = MessageDialog.openConfirm(shell, DELETE_DLG_TITLE, prompt);
		if (!confirm) {
			return;
		}

		List<IProject> projectsDeletionFailed = new ArrayList<>();
		List<String> deletionErrorMessages = new ArrayList<>();
		while (!projects.isEmpty()) {
			IProject project = projects.iterator().next();
			try {
				SpotterProjectSupport.deleteProject(project);
			} catch (CoreException e) {
				projectsDeletionFailed.add(project);
				deletionErrorMessages.add(e.getMessage());
			}
			projects.remove(project);
		}
		if (!projectsDeletionFailed.isEmpty()) {
			String errorMessage = createErrorMessage(projectsDeletionFailed, deletionErrorMessages);
			DialogUtils.openError(DELETE_DLG_TITLE, errorMessage);
		}
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

}
