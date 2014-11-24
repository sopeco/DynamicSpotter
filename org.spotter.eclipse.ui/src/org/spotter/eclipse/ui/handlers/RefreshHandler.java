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
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.navigator.SpotterProjectResults;

/**
 * A refresh handler for the refresh command which refreshes the elements shown
 * in the navigator.
 * 
 * @author Denis Knoepfle
 * 
 */
public class RefreshHandler extends AbstractHandler implements ISelectionChangedListener {

	/**
	 * The id of the refresh command.
	 */
	public static final String REFRESH_COMMAND_ID = "org.spotter.eclipse.ui.commands.refresh";

	private boolean isEnabled;

	/**
	 * Constructor.
	 */
	public RefreshHandler() {
		super();
		selectionChanged(null);
		Activator.getDefault().addProjectSelectionListener(this);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator activator = Activator.getDefault();
		Map<String, SpotterProjectResults> historyElements = activator.getProjectHistoryElements();

		for (IProject project : activator.getSelectedProjects()) {
			String projectName = project.getName();
			SpotterProjectResults history = historyElements.get(projectName);
			if (history != null) {
				history.refreshChildren();
			}
		}

		activator.getNavigatorViewer().refresh();

		return null;
	}

	/**
	 * Only enable refresh if at least one project is the current selected
	 * project.
	 * 
	 * @return <code>true</code> when at least one project is selected
	 */
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		this.isEnabled = !Activator.getDefault().getSelectedProjects().isEmpty();
	}

}
