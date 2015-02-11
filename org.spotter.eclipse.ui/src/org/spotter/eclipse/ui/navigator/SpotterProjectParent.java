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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ProjectLocationSelectionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.handlers.DeleteHandler;
import org.spotter.eclipse.ui.handlers.DuplicateHandler;
import org.spotter.eclipse.ui.menu.IDeletable;
import org.spotter.eclipse.ui.menu.IDuplicatable;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;

/**
 * This is the parent element of all other items and represents the project
 * node.
 * 
 * @author Denis Knoepfle
 * 
 */
public class SpotterProjectParent extends AbstractProjectElement {

	public static final String IMAGE_PATH = "icons/ds_16.png"; //$NON-NLS-1$

	private static final Logger LOGGER = LoggerFactory.getLogger(SpotterProjectParent.class);
	private static final String ELEMENT_TYPE_NAME = "Project";
	private static final String DUPLICATE_DLG_TITLE = "Duplicate Project";
	private static final String MSG_SINGLE = "Are you sure you want to remove project '%s' from the workspace?\n\nWarning: Contents will be deleted from disk!";
	private static final String MSG_MULTI = "Are you sure you want to remove the following projects from the workspace?\n\n%s\n\n"
			+ "Warning: Contents will be deleted from disk!";

	private IProject project;

	/**
	 * Creates a new instance of this element.
	 * 
	 * @param project
	 *            the associated project
	 */
	public SpotterProjectParent(IProject project) {
		super(IMAGE_PATH);
		this.project = project;
		addHandler(DeleteHandler.DELETE_COMMAND_ID, new IDeletable() {
			@Override
			public String getElementTypeName() {
				return ELEMENT_TYPE_NAME;
			}

			@Override
			public void delete() throws CoreException {
				SpotterProjectParent.this.delete();
			}

			@Override
			public boolean showConfirmationDialog(Object[] elements) {
				Class<?> clazz = SpotterProjectParent.this.getClass();
				if (elements == null || elements.length == 0 || !elements[0].getClass().equals(clazz)) {
					throw new IllegalArgumentException();
				}
				return SpotterProjectParent.this.showConfirmationDialog(elements);
			}

			@Override
			public void delete(Object[] elements) throws CoreException {
				SpotterProjectParent.this.delete(elements);
			}
		});
		addHandler(DuplicateHandler.DUPLICATE_COMMAND_ID, new IDuplicatable() {
			@Override
			public void duplicate() {
				SpotterProjectParent.this.duplicate();
			}
		});
	}

	@Override
	public String getText() {
		return project.getName();
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
	protected ISpotterProjectElement[] initializeChildren(IProject project) {
		List<ISpotterProjectElement> children = new ArrayList<>();

		children.add(new SpotterProjectConfig(this));

		if (SpotterProjectSupport.isExpertViewEnabled(project.getName())) {
			children.add(new SpotterProjectHierarchy(this));
		}

		children.add(new SpotterProjectResults(this));

		return children.toArray(new ISpotterProjectElement[children.size()]);
	}

	private void duplicate() {
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

			// update references in the DynamicSpotter configuration file for
			// the new project
			IProject duplicatedProject = project.getWorkspace().getRoot().getProject(duplicatedProjectName);
			SpotterProjectSupport.updateSpotterConfig(duplicatedProject);
		} catch (Exception e) {
			String message = "Error while copying project '" + project.getName() + "'!";
			LOGGER.error(message, e);
			DialogUtils.handleError(message, e);
		}
	}

	private boolean showConfirmationDialog(Object[] elements) {
		String prompt;
		if (elements.length > 1) {
			prompt = createMultiMessage(elements);
		} else {
			prompt = createSingleMessage();
		}

		boolean confirm = DialogUtils.openConfirm(IDeletable.DELETE_DLG_TITLE, prompt);
		return confirm;
	}

	private void delete() throws CoreException {
		SpotterProjectSupport.deleteProject(project);
	}

	private void delete(Object[] elements) throws CoreException {
		for (Object element : elements) {
			((SpotterProjectParent) element).delete();
		}
	}

	private String concatProjectNames(Object[] elements) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < elements.length; i++) {
			IProject project = ((SpotterProjectParent) elements[0]).getProject();
			sb.append("'" + project.getName() + "'");
			if (i < elements.length - 1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	private String createSingleMessage() {
		return String.format(MSG_SINGLE, project.getName());
	}

	private String createMultiMessage(Object[] elements) {
		return String.format(MSG_MULTI, concatProjectNames(elements));
	}

}
