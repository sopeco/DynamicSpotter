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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.lpe.common.util.system.LpeSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.jobs.JobsContainer;
import org.spotter.eclipse.ui.model.IExtensionItem;
import org.spotter.eclipse.ui.model.IExtensionItemFactory;
import org.spotter.eclipse.ui.model.ImmutableExtensionItemFactory;
import org.spotter.eclipse.ui.providers.RunExtensionsImageProvider;
import org.spotter.eclipse.ui.providers.SpotterExtensionsLabelProvider;
import org.spotter.eclipse.ui.viewers.ExtensionsGroupViewer;
import org.spotter.shared.status.DiagnosisProgress;
import org.spotter.shared.status.SpotterProgress;

/**
 * A view to display the progress of the current diagnosis run of
 * DynamicSpotter.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ActiveRunView extends ViewPart implements ISelectionChangedListener {

	public static final String VIEW_ID = "org.spotter.eclipse.ui.view.activeRunView";

	private static final long SLEEP_TIME_MILLIS = 1000;
	private static final Logger LOGGER = LoggerFactory.getLogger(ActiveRunView.class);

	private static final String ACTIVE_RUN_VIEW_TITLE = "Active Run";
	private static final String ACTIVE_RUN_EMPTY_CONTENT_DESC = "No project selected.";
	private static final String ACTIVE_RUN_CONTENT_DESC_TEMPLATE = "DynamicSpotter diagnosis of project '%s'";
	private static final String ACTIVE_RUN_MULTI_CONTENT_DESC = "Selected multiple projects.";

	private final IExtensionItemFactory extensionItemFactory;
	private boolean isDisposed;
	private Label label;
	private TreeViewer treeViewer;
	private SpotterProgress spotterProgress;

	private class ViewUpdater implements Runnable {

		@Override
		public void run() {
			while (!isDisposed) {
				try {
					Thread.sleep(SLEEP_TIME_MILLIS);
				} catch (InterruptedException e) {
					LOGGER.warn("View Updater was interrupted");
				}
				try {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							updateView();
						}
					});
				} catch (SWTException e) {
					LOGGER.debug("Stop view updater as view is already disposed (" + e.getMessage() + ")");
					break;
				}
			}
		}

	}

	/**
	 * The constructor.
	 */
	public ActiveRunView() {
		this.extensionItemFactory = new ImmutableExtensionItemFactory();
		this.isDisposed = false;
		this.spotterProgress = null;
	}

	@Override
	public void createPartControl(Composite parent) {
		setPartName(ACTIVE_RUN_VIEW_TITLE);
		setContentDescription(ACTIVE_RUN_EMPTY_CONTENT_DESC);

		// ensure that the parent's layout is a FillLayout
		if (!(parent.getLayout() instanceof FillLayout)) {
			parent.setLayout(new FillLayout());
		}

		label = new Label(parent, SWT.WRAP);
		createTreeViewer(parent);

		Activator.getDefault().addProjectSelectionListener(this);
		LpeSystemUtils.submitTask(new ViewUpdater());
	}

	private void createTreeViewer(Composite parent) {
		treeViewer = ExtensionsGroupViewer.createTreeViewer(parent, extensionItemFactory.createExtensionItem());

		SpotterExtensionsLabelProvider labelProvider = new SpotterExtensionsLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				super.update(cell);
				Object element = cell.getElement();
				if (element instanceof IExtensionItem) {
					cell.setText(cell.getText() + " some status 97%%");
				}
			}
		};

		labelProvider.setImageProvider(new RunExtensionsImageProvider());
		treeViewer.setLabelProvider(labelProvider);
	}

	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		Activator.getDefault().removeProjectSelectionListener(this);
		this.isDisposed = true;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		updateView();
	}

	private void updateView() {
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
			clear();
			label.setText("Currently no running diagnosis.");
		} else {
			label.setText("Diagnosis with job id '" + jobId + "' is in progress!");
			spotterProgress = client.getCurrentProgressReport();
			String problemId = spotterProgress.getCurrentProblem();

			if (problemId != null) {
				DiagnosisProgress progress = spotterProgress.getProgress(problemId);
				if (progress != null) {
					final float hundred = 100.0f;
					String estimation = String.format("%.1f", hundred * progress.getEstimatedProgress());
					String problemName = progress.getName();
					label.setText(label.getText() + "\n" + problemName + " (" + estimation + " %): "
							+ progress.getStatus());
				}
			}
		}
	}

	private void clear() {
		label.setText("");
	}

}
