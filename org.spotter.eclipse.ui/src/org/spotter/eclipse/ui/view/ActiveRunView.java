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
package org.spotter.eclipse.ui.view;

import java.net.ConnectException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.jobs.JobsContainer;

/**
 * A view to display the progress of the current diagnosis run of
 * DynamicSpotter.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ActiveRunView extends ViewPart implements ISelectionChangedListener {

	public static final String VIEW_ID = "org.spotter.eclipse.ui.view.activeRunView";

	private static final Logger LOGGER = LoggerFactory.getLogger(ActiveRunView.class);

	private static final String ACTIVE_RUN_VIEW_TITLE = "Active Run";
	private static final String ACTIVE_RUN_EMPTY_CONTENT_DESC = "No project selected.";
	private static final String ACTIVE_RUN_CONTENT_DESC_TEMPLATE = "DynamicSpotter diagnosis of project '%s'";
	private static final String ACTIVE_RUN_MULTI_CONTENT_DESC = "Selected multiple projects.";

	private Label label;

	/**
	 * The constructor.
	 */
	public ActiveRunView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		setPartName(ACTIVE_RUN_VIEW_TITLE);
		setContentDescription(ACTIVE_RUN_EMPTY_CONTENT_DESC);

		// ensure that the parent's layout is a FillLayout
		if (!(parent.getLayout() instanceof FillLayout)) {
			parent.setLayout(new FillLayout());
		}

		label = new Label(parent, SWT.NONE);

		Activator.getDefault().addProjectSelectionListener(this);
	}

	@Override
	public void setFocus() {
		// TODO: change later: give focus to main control
		label.setFocus();
	}

	@Override
	public void dispose() {
		Activator.getDefault().removeProjectSelectionListener(this);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		Set<IProject> selected = Activator.getDefault().getSelectedProjects();
		String description;
		if (selected.isEmpty()) {
			description = ACTIVE_RUN_EMPTY_CONTENT_DESC;
			clear();
		} else if (selected.size() == 1) {
			IProject project = selected.iterator().next();
			description = String.format(ACTIVE_RUN_CONTENT_DESC_TEMPLATE, project.getName());
			updateContent(project);
		} else {
			description = ACTIVE_RUN_MULTI_CONTENT_DESC;
			clear();
		}

		setContentDescription(description);
	}

	private void updateContent(IProject project) {
		ServiceClientWrapper client = Activator.getDefault().getClient(project.getName());
		boolean hasClientConnection = client.testConnection(false);
		boolean hasConnectionErrorOccured = false;
		Long jobId = null;

		if (hasClientConnection) {
			try {
				jobId = JobsContainer.readCurrentJob(client, project);
			} catch (ConnectException e) {
				hasConnectionErrorOccured = true;
			}
		}

		if (!hasClientConnection || hasConnectionErrorOccured) {
			label.setText("No connection to DS service. Try again later.");
		} else if (jobId == null) {
			label.setText("Currently no running diagnosis.");
		} else {
			label.setText("Diagnosis in progress!");
		}
	}

	private void clear() {
		label.setText("");
	}

}
