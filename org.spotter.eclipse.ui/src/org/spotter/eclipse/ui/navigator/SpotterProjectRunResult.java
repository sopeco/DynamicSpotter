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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.handlers.DeleteHandler;
import org.spotter.eclipse.ui.handlers.OpenHandler;
import org.spotter.eclipse.ui.jobs.JobsContainer;
import org.spotter.eclipse.ui.menu.IDeletable;
import org.spotter.eclipse.ui.menu.IOpenable;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.eclipse.ui.util.SpotterUtils;
import org.spotter.eclipse.ui.view.ResultsView;
import org.spotter.shared.result.ResultsLocationConstants;
import org.spotter.shared.result.model.ResultsContainer;

/**
 * An element that represents a run result node.
 * 
 * @author Denis Knoepfle
 * 
 */
public class SpotterProjectRunResult extends AbstractProjectElement {

	public static final String IMAGE_PATH = "icons/results.gif"; //$NON-NLS-1$
	public static final String ERROR_IMAGE_PATH = "icons/exclamation.png"; //$NON-NLS-1$

	private static final Logger LOGGER = LoggerFactory.getLogger(SpotterProjectRunResult.class);

	private static final String ELEMENT_TYPE_NAME = "Result Item";
	private static final String OPEN_ID = ResultsView.VIEW_ID;
	private static final String TXT_OPEN_ID = EditorsUI.DEFAULT_TEXT_EDITOR_ID;

	private static final String MSG_SINGLE = "Are you sure you want to delete the result '%s'?";
	private static final String MSG_MULTI = "Are you sure you want to delete these %d elements?";

	private final ISpotterProjectElement parent;
	private final IFolder resultFolder;
	private boolean isErroneous;
	private final long jobId;
	private final long timestamp;
	private final String elementName;
	private String elementLabel;

	/**
	 * Creates a new instance of this element.
	 * 
	 * @param parent
	 *            the parent element
	 * @param jobId
	 *            the corresponding job id of this run result
	 * @param timestamp
	 *            the corresponding timestamp of this run result
	 * @param resultFolder
	 *            the result folder that is represented by this node
	 */
	public SpotterProjectRunResult(ISpotterProjectElement parent, long jobId, long timestamp, IFolder resultFolder) {
		super();

		this.parent = parent;
		this.jobId = jobId;
		this.timestamp = timestamp;

		this.resultFolder = resultFolder;
		String errorFilePath = resultFolder.getFile(ResultsLocationConstants.TXT_DIAGNOSIS_ERROR_FILE_NAME)
				.getLocation().toString();
		this.isErroneous = new File(errorFilePath).exists();
		if (isErroneous) {
			setImagePath(ERROR_IMAGE_PATH);
		} else {
			setImagePath(IMAGE_PATH);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd (HH:mm:ss)");
		this.elementName = dateFormat.format(new Date(timestamp));
		readElementLabel();

		addOpenHandler();
		addDeleteHandler();
	}

	/**
	 * @return <code>true</code> if erroneous diagnosis, otherwise
	 *         <code>false</code>
	 */
	public boolean isErroneous() {
		return isErroneous;
	}

	private void addOpenHandler() {
		addHandler(OpenHandler.OPEN_COMMAND_ID, new IOpenable() {
			@Override
			public void open() {
				SpotterProjectRunResult.this.open();
			}

			@Override
			public String getOpenId() {
				return SpotterProjectRunResult.this.getOpenId();
			}

			@Override
			public String getElementName() {
				return SpotterProjectRunResult.this.elementName;
			}
		});
	}

	private void addDeleteHandler() {
		addHandler(DeleteHandler.DELETE_COMMAND_ID, new IDeletable() {
			@Override
			public String getElementTypeName() {
				return ELEMENT_TYPE_NAME;
			}

			@Override
			public void delete() {
				SpotterProjectRunResult.this.delete();
			}

			@Override
			public void delete(Object[] elements) throws CoreException {
				SpotterProjectRunResult.this.delete(elements);
			}

			@Override
			public boolean showConfirmationDialog(Object[] elements) {
				return SpotterProjectRunResult.this.showConfirmationDialog(elements);
			}
		});
	}

	@Override
	public String getText() {
		if (elementLabel != null && !elementLabel.isEmpty()) {
			return elementName + " " + elementLabel;
		} else {
			return elementName;
		}
	}

	/**
	 * @return the corresponding timestamp of this run result
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the result folder this element is linked to
	 */
	public IFolder getResultFolder() {
		return resultFolder;
	}

	@Override
	public Object getParent() {
		return parent;
	}

	@Override
	public IProject getProject() {
		return parent.getProject();
	}

	/**
	 * Updates the label. This also updates the corresponding results container.
	 * 
	 * @param label
	 *            the new label
	 */
	public synchronized void updateElementLabel(String label) {
		ResultsContainer container = SpotterUtils.readResultsContainer(resultFolder);
		if (container != null) {
			String oldLabel = container.getLabel();
			container.setLabel(label);
			if (SpotterUtils.writeResultsContainer(resultFolder, container)) {
				this.elementLabel = label;
			} else {
				container.setLabel(oldLabel);
			}
		}
	}

	/**
	 * @return the label of this element if any
	 */
	public String getElementLabel() {
		return elementLabel;
	}

	/**
	 * Reads the corresponding container and updates the label.
	 */
	private void readElementLabel() {
		ResultsContainer container = SpotterUtils.readResultsContainer(resultFolder);
		String label = null;
		if (container != null) {
			label = container.getLabel();
		}
		this.elementLabel = label;
	}

	private String getOpenId() {
		return isErroneous ? TXT_OPEN_ID : OPEN_ID;
	}

	private void open() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			if (isErroneous) {
				if (!resultFolder.isSynchronized(IResource.DEPTH_INFINITE)) {
					resultFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
				}
				IFile file = resultFolder.getFile(ResultsLocationConstants.TXT_DIAGNOSIS_ERROR_FILE_NAME);
				page.openEditor(new FileEditorInput(file), getOpenId());
			} else {
				ResultsView view = (ResultsView) page.showView(getOpenId());
				view.setResult(this);
			}
		} catch (CoreException e) {
			String message = "Could not open view part " + getOpenId();
			LOGGER.error(message, e);
			throw new RuntimeException(message, e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SpotterProjectRunResult)) {
			return false;
		}
		SpotterProjectRunResult other = (SpotterProjectRunResult) obj;
		boolean equalProject = getProject().equals(other.getProject());
		boolean equalName = elementName.equals(other.elementName);
		return equalProject && equalName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getProject().hashCode();
		result = prime * result + ((elementName == null) ? 0 : elementName.hashCode());
		return result;
	}

	private boolean showConfirmationDialog(Object[] elements) {
		String prompt;
		if (elements.length > 1) {
			prompt = createMultiMessage(elements.length);
		} else {
			prompt = createSingleMessage();
		}

		boolean confirm = DialogUtils.openConfirm(IDeletable.DELETE_DLG_TITLE, prompt);
		return confirm;
	}

	private void delete() {
		try {
			if (!resultFolder.isSynchronized(IResource.DEPTH_INFINITE)) {
				resultFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
			}

			ResultsView.reset(resultFolder);
			resultFolder.delete(true, null);
			// clear job id
			if (!JobsContainer.removeJobId(getProject(), jobId)) {
				DialogUtils
						.openError("There was an error while updating the project's job ids. The results of the corresponding id will be fetched again.");
			}

			updateNavigatorViewer();
		} catch (CoreException e) {
			String message = "Error while deleting result folder '" + resultFolder.getName() + "'!";
			LOGGER.error(message, e);
			DialogUtils.handleError(message, e);
		}
	}

	private void singleDelete(SpotterProjectRunResult runResult, List<Long> jobIds) {
		IFolder resultFolder = runResult.getResultFolder();
		try {
			if (!resultFolder.isSynchronized(IResource.DEPTH_INFINITE)) {
				resultFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
			}

			ResultsView.reset(resultFolder);
			resultFolder.delete(true, null);
			jobIds.add(runResult.jobId);
		} catch (CoreException e) {
			String message = "Error while deleting result folder '" + resultFolder.getName() + "'!";
			LOGGER.error(message, e);
			DialogUtils.handleError(message, e);
		}
	}

	private void delete(Object[] elements) {
		List<Long> jobIds = new ArrayList<>();
		for (Object element : elements) {
			SpotterProjectRunResult runResult = (SpotterProjectRunResult) element;
			singleDelete(runResult, jobIds);
		}

		if (!JobsContainer.removeJobIds(getProject(), jobIds)) {
			DialogUtils
					.openError("There was an error while updating the project's job ids. The results of the corresponding ids will be fetched again.");
		}
		updateNavigatorViewer();
	}

	private void updateNavigatorViewer() {
		SpotterProjectResults parent = (SpotterProjectResults) getParent();
		parent.refreshChildren();
		Activator.getDefault().getNavigatorViewer().refresh(parent);
	}

	private String createSingleMessage() {
		return String.format(MSG_SINGLE, getText());
	}

	private String createMultiMessage(int count) {
		return String.format(MSG_MULTI, count);
	}

}
