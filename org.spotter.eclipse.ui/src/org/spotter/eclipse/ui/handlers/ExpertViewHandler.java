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

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.navigator.ISpotterProjectElement;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;
import org.spotter.eclipse.ui.util.SpotterUtils;

/**
 * An expert view handler for the expert view command which toggles the expert
 * view for the selected project.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ExpertViewHandler extends AbstractHandler implements IElementUpdater, ISelectionChangedListener {

	/**
	 * The id of the corresponding expert view command.
	 */
	public static final String EXPERT_VIEW_COMMAND_ID = "org.spotter.eclipse.ui.commands.expertView";

	private static final String TOGGLE_STATE_ID = "org.eclipse.ui.commands.toggleState";

	private static final String MSG_CONFIRM_ENABLE = "Are you sure you want to enable the expert view? The expert view enables additional "
			+ "and enhanced features and you should only use this when you know exactly what you are doing.";

	private ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(
			ICommandService.class);
	private boolean isEnabled;

	/**
	 * Constructor.
	 */
	public ExpertViewHandler() {
		super();
		selectionChanged(null);
		Activator.getDefault().addProjectSelectionListener(this);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator activator = Activator.getDefault();
		IStructuredSelection sel = (IStructuredSelection) activator.getNavigatorViewer().getSelection();
		ISpotterProjectElement projectElement = null;
		if (!sel.isEmpty()) {
			projectElement = (ISpotterProjectElement) sel.getFirstElement();
		}

		if (projectElement == null || activator.getSelectedProjects().size() != 1) {
			return null;
		}

		Command command = event.getCommand();
		State state = getCommandState(command);

		boolean oldValue = (boolean) state.getValue();
		String projectName = projectElement.getProject().getName();

		// prompt confirmation only if trying to enable
		boolean confirm = oldValue || DialogUtils.openConfirm(MSG_CONFIRM_ENABLE);

		if (confirm && SpotterProjectSupport.setExpertModeEnabled(projectName, !oldValue)) {
			HandlerUtil.toggleCommandState(command);

			SpotterUtils.refreshProjectParent(projectElement);
		}

		return null;
	}

	/**
	 * Returns <code>true</code> when exactly one project is selected.
	 * 
	 * @return <code>true</code> when exactly one project is selected
	 */
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void updateElement(UIElement element, Map parameters) {
		Activator activator = Activator.getDefault();
		if (activator.getSelectedProjects().size() == 1) {
			IProject project = activator.getSelectedProjects().iterator().next();
			String projectName = project.getName();

			boolean enabled = SpotterProjectSupport.isExpertViewEnabled(projectName);
			State state = getCommandState(null);
			state.setValue(Boolean.valueOf(enabled));

			String label = (enabled ? "Disable" : "Enable") + " Expert View";
			element.setText(label);
		}
	}

	/**
	 * Retrieves the toggle state of the command.
	 * 
	 * @param expertViewCommand
	 *            the command or <code>null</code> to manually try to lookup the
	 *            command
	 * 
	 * @return the toggle state of the command
	 */
	private State getCommandState(Command expertViewCommand) {
		Command command = expertViewCommand;

		if (command == null && commandService != null) {
			command = commandService.getCommand(EXPERT_VIEW_COMMAND_ID);
		}

		if (command == null) {
			throw new RuntimeException("Unable to retrieve command " + EXPERT_VIEW_COMMAND_ID);
		}

		State state = command.getState(TOGGLE_STATE_ID);

		if (state == null) {
			throw new RuntimeException("Unable to retrieve state " + TOGGLE_STATE_ID + " from command "
					+ EXPERT_VIEW_COMMAND_ID);
		}

		return state;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		this.isEnabled = Activator.getDefault().getSelectedProjects().size() == 1;
	}

}
