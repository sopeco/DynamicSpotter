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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.lpe.common.util.LpeFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.editors.HierarchyEditor;
import org.spotter.eclipse.ui.model.IExtensionItem;
import org.spotter.eclipse.ui.model.IExtensionItemFactory;
import org.spotter.eclipse.ui.model.ImmutableExtensionItemFactory;
import org.spotter.eclipse.ui.navigator.SpotterProjectResults;
import org.spotter.eclipse.ui.navigator.SpotterProjectRunResult;
import org.spotter.eclipse.ui.providers.ResultExtensionsImageProvider;
import org.spotter.eclipse.ui.providers.SpotterExtensionsLabelProvider;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.eclipse.ui.util.SpotterUtils;
import org.spotter.eclipse.ui.util.WidgetUtils;
import org.spotter.eclipse.ui.viewers.ExtensionsGroupViewer;
import org.spotter.eclipse.ui.viewers.ResourceViewer;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;
import org.spotter.shared.result.ResultsLocationConstants;
import org.spotter.shared.result.model.ResultsContainer;
import org.spotter.shared.result.model.SpotterResult;

/**
 * A view to display results of a DynamicSpotter run.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ResultsView extends ViewPart implements ISelectionListener {

	public static final String VIEW_ID = "org.spotter.eclipse.ui.view.resultsView";

	private static final Logger LOGGER = LoggerFactory.getLogger(ResultsView.class);

	private static final String RESULTS_VIEW_TITLE = "Results";
	private static final int RESOURCES_LIST_RATIO = 45;
	private static final int RESOURCES_CANVAS_RATIO = 55;

	private static final String RESULTS_CONTENT_DESC_TEMPLATE = "DynamicSpotter Run '%s' of project '%s'";
	private static final String RESULTS_EMPTY_CONTENT_DESC = "None selected.";
	private static final String EMPTY_RESULTS = "No results selected.";
	private static final String ERR_MSG_PARSE_ERROR = "An error occured while parsing the file '%s'.";
	private static final String ERR_MSG_RES_REFRESH = "Error occured while refreshing resource!";
	private static final String ERR_MSG_MISSING_REPORT = "Either file is missing or report is not set.";
	private static final String ERR_MSG_MISSING_SER_FILE = "Could not find the spotter serialization file related with result '%s'.";

	private static final String LABEL_NONE_SELECTED = "<none selected>";
	private static final String LABEL_DETECTED = "Detected";
	private static final String LABEL_NOT_DETECTED = "Not Detected";
	private static final String LABEL_NO_LOOKUP = "The corresponding result could not be looked up. (erroneous analysis)";
	private static final String LABEL_NO_INFO = "No description available.";

	private static final String TAB_HIERARCHY_NAME = "Hierarchy";
	private static final String TAB_REPORT_NAME = "Report";
	private static final String TAB_ANNOTATION_NAME = "Annotations";

	private final IExtensionItemFactory extensionItemFactory;

	private Group grpDetails;
	private TreeViewer hierarchyTreeViewer;
	private ResultExtensionsImageProvider imageProvider;
	private Text textReport;
	private Text textAnnotation;
	private Button btnSaveAnnotation;
	private Label lblProblemName;
	private Label lblStatus;
	private Label lblDescription;
	private Text textResult;
	private List listResources;
	private ResourceViewer resourceViewer;

	private ServiceClientWrapper client;
	private SpotterProjectRunResult runResultItem;
	private ResultsContainer resultsContainer;
	private XPerformanceProblem currentSelectedProblem;

	/**
	 * The constructor.
	 */
	public ResultsView() {
		this.client = null;
		this.runResultItem = null;
		this.extensionItemFactory = new ImmutableExtensionItemFactory(null);
	}

	@Override
	public void createPartControl(Composite parent) {
		setPartName(RESULTS_VIEW_TITLE);
		setContentDescription(RESULTS_EMPTY_CONTENT_DESC);

		// ensure that the parent's layout is a FillLayout
		if (!(parent.getLayout() instanceof FillLayout)) {
			parent.setLayout(new FillLayout());
		}

		TabFolder folder = new TabFolder(parent, SWT.NONE);
		createHierarchyTab(folder);
		createReportTab(folder);
		createAnnotationTab(folder);

		getViewSite().getPage().addPostSelectionListener(this);
	}

	/**
	 * Sets the run result to show.
	 * 
	 * @param runResultItem
	 *            the run result item to set
	 */
	public void setResult(SpotterProjectRunResult runResultItem) {
		this.runResultItem = runResultItem;
		if (runResultItem != null) {
			String projectName = runResultItem.getProject().getName();
			this.client = Activator.getDefault().getClient(projectName);
		} else {
			this.client = null;
		}
		updateTabs();
	}

	/**
	 * Returns the run result currently shown.
	 * 
	 * @return the run result currently shown or <code>null</code> if none
	 */
	public SpotterProjectRunResult getResult() {
		return runResultItem;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part == this) {
			return;
		}
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof SpotterProjectRunResult) {
				SpotterProjectRunResult runResult = (SpotterProjectRunResult) first;
				if (runResult.isErroneous()) {
					return;
				}
				IFolder newFolder = runResult.getResultFolder();
				if (getResult() == null || !getResult().getResultFolder().equals(newFolder)) {
					setResult(runResult);
				}
			}
		}
	}

	@Override
	public void setFocus() {
		hierarchyTreeViewer.getTree().setFocus();
	}

	@Override
	public void dispose() {
		getViewSite().getPage().removePostSelectionListener(this);
		resourceViewer.dispose();
	}

	/**
	 * Updates the content description string of the results view.
	 */
	public static void updateContentDescription() {
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				ResultsView resultsView = (ResultsView) page.findView(ResultsView.VIEW_ID);
				if (resultsView != null) {
					resultsView.setContentDescription(resultsView.createContentDescription());
				}
			}
		}
	}

	/**
	 * Resets the results view and deletes its contents if the given project
	 * matches the current content's associated project. If <code>project</code>
	 * is <code>null</code>, then the view is reset regardless of its current
	 * content.
	 * 
	 * @param project
	 *            the project to match for the reset or <code>null</code> to
	 *            match any
	 */
	public static void reset(IProject project) {
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				ResultsView resultsView = (ResultsView) page.findView(ResultsView.VIEW_ID);
				if (resultsView != null) {
					SpotterProjectRunResult result = resultsView.getResult();
					if (project == null || result != null && project.equals(result.getProject())) {
						resultsView.setResult(null);
					}
				}
			}
		}
	}

	/**
	 * Resets the results view and deletes its contents if the given project
	 * matches the current content's associated folder. If <code>folder</code>
	 * is <code>null</code>, then the view is reset regardless of its current
	 * content.
	 * 
	 * @param folder
	 *            the folder to match for the reset or <code>null</code> to
	 *            match any
	 */
	public static void reset(IFolder folder) {
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				ResultsView resultsView = (ResultsView) page.findView(ResultsView.VIEW_ID);
				if (resultsView != null) {
					SpotterProjectRunResult result = resultsView.getResult();
					if (folder == null || result != null && folder.equals(result.getResultFolder())) {
						resultsView.setResult(null);
					}
				}
			}
		}
	}

	private void createHierarchyTab(TabFolder folder) {
		TabItem tabItem = new TabItem(folder, SWT.NONE);
		tabItem.setText(TAB_HIERARCHY_NAME);

		Composite parent = new Composite(folder, SWT.NONE);
		parent.setLayout(new FillLayout());
		SashForm container = new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);

		hierarchyTreeViewer = ExtensionsGroupViewer.createTreeViewer(container,
				extensionItemFactory.createExtensionItem(), null, false);
		SpotterExtensionsLabelProvider labelProvider = (SpotterExtensionsLabelProvider) hierarchyTreeViewer
				.getLabelProvider();
		imageProvider = new ResultExtensionsImageProvider();
		labelProvider.setImageProvider(imageProvider);

		grpDetails = new Group(container, SWT.NONE);
		grpDetails.setText("Details");
		grpDetails.setLayout(WidgetUtils.createGridLayout(1, true));

		createHierarchyDetailsUpperPart(grpDetails);
		createHierarchyDetailsLowerPart(grpDetails);

		container.setWeights(new int[] { 1, 2 });

		addSelectionListeners();
		tabItem.setControl(parent);
	}

	private void createHierarchyDetailsUpperPart(Composite parent) {
		Composite compUpperPart = new Composite(parent, SWT.NONE);
		compUpperPart.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		compUpperPart.setLayout(new GridLayout(2, false));

		Label label = new Label(compUpperPart, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		label.setText("Problem Name:");
		lblProblemName = new Label(compUpperPart, SWT.NONE);
		lblProblemName.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		lblProblemName.setText(LABEL_NONE_SELECTED);

		label = new Label(compUpperPart, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		label.setText("Status:");
		lblStatus = new Label(compUpperPart, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		lblStatus.setText("");

		label = new Label(compUpperPart, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		label.setText("Description:");
		lblDescription = new Label(compUpperPart, SWT.WRAP);
		lblDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		lblDescription.setText("");
	}

	private void createHierarchyDetailsLowerPart(Composite parent) {
		Composite compLowerPart = new Composite(parent, SWT.NONE);
		compLowerPart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		compLowerPart.setLayout(new FillLayout(SWT.HORIZONTAL));
		SashForm sashContainer = new SashForm(compLowerPart, SWT.HORIZONTAL | SWT.SMOOTH);

		Group grpResult = new Group(sashContainer, SWT.NONE);
		grpResult.setText("Result Message");
		grpResult.setLayout(new FillLayout());

		textResult = new Text(grpResult, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		textResult.setText("");
		textResult.setEditable(false);

		Group grpResources = new Group(sashContainer, SWT.NONE);
		grpResources.setText("Resources");
		grpResources.setLayout(new FillLayout(SWT.HORIZONTAL));
		SashForm sashResources = new SashForm(grpResources, SWT.HORIZONTAL | SWT.SMOOTH);

		listResources = new List(sashResources, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		resourceViewer = new ResourceViewer(sashResources);

		sashResources.setWeights(new int[] { RESOURCES_LIST_RATIO, RESOURCES_CANVAS_RATIO });
	}

	private void addSelectionListeners() {
		hierarchyTreeViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				currentSelectedProblem = null;
				if (!sel.isEmpty()) {
					IExtensionItem item = (IExtensionItem) sel.getFirstElement();
					Object problem = item.getModelWrapper().getXMLModel();
					if (problem instanceof XPerformanceProblem) {
						currentSelectedProblem = (XPerformanceProblem) problem;
					}
				}
				updateProblemDetails();
			}
		});

		hierarchyTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object selectedNode = selection.getFirstElement();
				hierarchyTreeViewer.setExpandedState(selectedNode, !hierarchyTreeViewer.getExpandedState(selectedNode));
			}
		});

		listResources.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (listResources.getSelectionCount() == 0) {
					resourceViewer.clear();
				} else {
					String selection = listResources.getSelection()[0];
					String prefix = getCurrentResourceFolder();
					String resourceFile = prefix + selection;
					resourceViewer.setResource(resourceFile, runResultItem.getProject().getName());
				}
			}
		});
	}

	private void updateProblemDetails() {
		listResources.removeAll();
		resourceViewer.clear();

		if (currentSelectedProblem == null) {
			lblProblemName.setText(LABEL_NONE_SELECTED);
			lblDescription.setText("");
			lblStatus.setText("");
			textResult.setText("");
		} else {
			String name = currentSelectedProblem.getExtensionName();
			lblProblemName.setText(name);
			String description = client.getExtensionDescription(name);
			lblDescription.setText(description == null ? LABEL_NO_INFO : description);

			String id = currentSelectedProblem.getUniqueId();
			SpotterResult spotterResult = resultsContainer == null ? null : resultsContainer.getResultsMap().get(id);
			if (spotterResult == null) {
				lblStatus.setText(LABEL_NO_LOOKUP);
				textResult.setText(LABEL_NO_LOOKUP);
			} else {
				lblStatus.setText(spotterResult.isDetected() ? LABEL_DETECTED : LABEL_NOT_DETECTED);
				textResult.setText(spotterResult.getMessage());
				populateResourcesList(spotterResult);
			}
		}
		grpDetails.layout();
	}

	private void populateResourcesList(SpotterResult spotterResult) {
		for (String resourceFile : spotterResult.getResourceFiles()) {
			listResources.add(resourceFile);
		}
	}

	private void createReportTab(TabFolder folder) {
		TabItem tabItem = new TabItem(folder, SWT.NONE);
		tabItem.setText(TAB_REPORT_NAME);

		textReport = new Text(folder, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		textReport.setText(EMPTY_RESULTS);
		textReport.setEditable(false);

		tabItem.setControl(textReport);
	}

	private void createAnnotationTab(TabFolder folder) {
		TabItem tabItem = new TabItem(folder, SWT.NONE);
		tabItem.setText(TAB_ANNOTATION_NAME);

		Composite parent = new Composite(folder, SWT.NONE);
		parent.setLayout(WidgetUtils.createGridLayout(1, true));

		btnSaveAnnotation = new Button(parent, SWT.PUSH);
		btnSaveAnnotation.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
		btnSaveAnnotation.setText("Save Changes");
		btnSaveAnnotation.setEnabled(false);
		btnSaveAnnotation.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (runResultItem != null && resultsContainer != null) {
					String oldAnnotation = textAnnotation.getText();
					resultsContainer.setAnnotation(textAnnotation.getText());
					if (SpotterUtils.writeResultsContainer(runResultItem.getResultFolder(), resultsContainer)) {
						btnSaveAnnotation.setEnabled(false);
					} else {
						resultsContainer.setAnnotation(oldAnnotation);
					}
				}
			}
		});

		Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setLayout(new FillLayout());

		textAnnotation = new Text(group, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		textAnnotation.setText("");
		textAnnotation.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (resultsContainer != null && !btnSaveAnnotation.isEnabled()) {
					btnSaveAnnotation.setEnabled(true);
				}
			}
		});

		tabItem.setControl(parent);
	}

	private void updateTabs() {
		setContentDescription(createContentDescription());
		if (runResultItem == null) {
			resetHierarchy();
			resetReport();
			resetAnnotation();
		} else if (updateResultsContainer()) {
			updateHierarchy();
			updateReport();
			updateAnnotation();
		}
	}

	private String createContentDescription() {
		String desc = RESULTS_EMPTY_CONTENT_DESC;
		if (runResultItem != null) {
			String itemText = runResultItem.getText();
			String projectName = runResultItem.getProject().getName();
			desc = String.format(RESULTS_CONTENT_DESC_TEMPLATE, itemText, projectName);
		}
		return desc;
	}

	private void resetHierarchy() {
		hierarchyTreeViewer.setInput(extensionItemFactory.createExtensionItem());
		lblProblemName.setText(LABEL_NONE_SELECTED);
		lblDescription.setText("");
		lblStatus.setText("");
		textResult.setText("");
		listResources.removeAll();
		resourceViewer.clear();
	}

	private void resetReport() {
		textReport.setText(EMPTY_RESULTS);
	}

	private void resetAnnotation() {
		textAnnotation.setText("");
		btnSaveAnnotation.setEnabled(false);
	}

	private boolean updateResultsContainer() {

		IFolder resultFolder = runResultItem.getResultFolder();
		IFile file = resultFolder.getFile(ResultsLocationConstants.RESULTS_SERIALIZATION_FILE_NAME);

		resultsContainer = null;
		String errorMsg = null;
		Exception exception = null;
		try {
			File containerFile = new File(file.getLocation().toString());
			if (!containerFile.exists()) {
				try {
					if (!resultFolder.isSynchronized(IResource.DEPTH_INFINITE)) {
						resultFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
					}

					if (resultFolder.exists()) {
						resultFolder.delete(true, null);
					}
					SpotterProjectResults parent = (SpotterProjectResults) runResultItem.getParent();
					parent.refreshChildren();
				} catch (CoreException e) {
					LOGGER.error("Error while deleting result folder.", e);
				}
			}

			if (!file.isSynchronized(IResource.DEPTH_ZERO)) {
				file.refreshLocal(IResource.DEPTH_ZERO, null);
			}
			resultsContainer = (ResultsContainer) LpeFileUtils.readObject(containerFile);
		} catch (CoreException e) {
			errorMsg = ERR_MSG_RES_REFRESH;
			exception = e;
			LOGGER.error(ERR_MSG_RES_REFRESH, e);
		} catch (FileNotFoundException e) {
			errorMsg = String.format(ERR_MSG_MISSING_SER_FILE, runResultItem.getText());
			exception = e;
			LOGGER.error(DialogUtils.appendCause(errorMsg, e.getMessage()));
		} catch (IOException | ClassNotFoundException e) {
			errorMsg = String.format(ERR_MSG_PARSE_ERROR, file.getLocation());
			exception = e;
			LOGGER.error(errorMsg, e);
		}

		if (errorMsg != null) {
			resultsContainer = null;
			DialogUtils.handleError(errorMsg, exception);
			setResult(null);
		}

		return errorMsg == null;
	}

	private void updateHierarchy() {
		IExtensionItem input = null;

		if (resultsContainer != null) {
			XPerformanceProblem root = resultsContainer.getRootProblem();
			if (root != null) {
				String projectName = runResultItem.getProject().getName();
				input = HierarchyEditor.createPerformanceProblemHierarchy(projectName, extensionItemFactory, root);
			}
		}

		imageProvider.setResultsContainer(resultsContainer);
		if (input == null) {
			input = extensionItemFactory.createExtensionItem();
		}
		hierarchyTreeViewer.setInput(input);
		hierarchyTreeViewer.expandAll();
	}

	private void updateReport() {
		if (resultsContainer != null && resultsContainer.getReport() != null) {
			textReport.setText(resultsContainer.getReport());
		} else {
			textReport.setText(ERR_MSG_MISSING_REPORT);
		}
	}

	private void updateAnnotation() {
		if (resultsContainer != null && resultsContainer.getAnnotation() != null) {
			textAnnotation.setText(resultsContainer.getAnnotation());
		} else {
			textAnnotation.setText("");
		}
		btnSaveAnnotation.setEnabled(false);
	}

	private String getCurrentResourceFolder() {
		IFolder folder = runResultItem.getResultFolder();
		String currentRunFolder = folder.getLocation().toString() + "/";
		String subDirPath = getSubDirPathForProblem(currentSelectedProblem);

		return currentRunFolder + subDirPath;
	}

	private static String getSubDirPathForProblem(XPerformanceProblem problem) {
		// TODO: there must be a way to retrieve this path from the result
		// independent of the extension's implementation!
		String name = problem.getExtensionName();
		String idTag = "" + problem.getUniqueId().hashCode();

		String subDirPath = name + "-" + idTag + "/" + ResultsLocationConstants.RESULT_RESOURCES_SUB_DIR + "/";

		return subDirPath;
	}

}
