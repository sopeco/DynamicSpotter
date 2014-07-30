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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.editors.HierarchyEditor;
import org.spotter.eclipse.ui.model.ExtensionItem;
import org.spotter.eclipse.ui.navigator.SpotterProjectParent;
import org.spotter.eclipse.ui.navigator.SpotterProjectRunResult;
import org.spotter.eclipse.ui.providers.ResultExtensionsImageProvider;
import org.spotter.eclipse.ui.providers.SpotterExtensionsLabelProvider;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;
import org.spotter.eclipse.ui.util.WidgetUtils;
import org.spotter.eclipse.ui.viewers.ExtensionsGroupViewer;
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
	private static final String DLG_RESOURCE_TITLE = "Resource '%s' (%s)";
	private static final int DLG_RESOURCE_TOP_MARGIN = 40;

	private static final String RESULTS_CONTENT_DESC_TEMPLATE = "DynamicSpotter Run '%s' of project '%s'";
	private static final String RESULTS_EMPTY_CONTENT_DESC = "None selected.";
	private static final String EMPTY_RESULTS = "No results selected.";
	private static final String ERR_MSG_IO_ERROR = "An I/O error occured while reading the file '%s'.";
	private static final String ERR_MSG_MISSING_REPORT = "Could not find the spotter report file.";
	private static final String ERR_MSG_MISSING_SER_FILE = "Could not find the spotter serialization file.";

	private static final String LABEL_NONE_SELECTED = "<none selected>";
	private static final String LABEL_DETECTED = "Detected";
	private static final String LABEL_NOT_DETECTED = "Not Detected";
	private static final String LABEL_NO_LOOKUP = "The corresponding result could not be looked up. (erroneous analysis)";
	private static final String LABEL_NO_INFO = "No description available.";

	private static final String TAB_HIERARCHY_NAME = "Hierarchy";
	private static final String TAB_REPORT_NAME = "Report";

	private static final String RESOURCE_SEPARATOR_CHAR = "/";

	private Group grpDetails;
	private TreeViewer hierarchyTreeViewer;
	private ResultExtensionsImageProvider imageProvider;
	private Text textReport;
	private Label lblProblemName;
	private Label lblStatus;
	private Label lblDescription;
	private Text textResult;
	private List listResources;
	private Canvas canvasRes;
	private Map<String, Shell> resourceShells;
	private ImageData resourceImageData;
	private Image resourceImage;

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
		this.resourceShells = new HashMap<>();
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

		getViewSite().getPage().addPostSelectionListener(this);
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

		hierarchyTreeViewer = ExtensionsGroupViewer.createTreeViewer(container, new ExtensionItem());
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
		canvasRes = new Canvas(sashResources, SWT.NONE);

		sashResources.setWeights(new int[] { 1, 2 });
		addCanvasListeners();
	}

	private void addCanvasListeners() {
		canvasRes.addMouseTrackListener(new MouseTrackAdapter() {
			private Shell shell;
			private Cursor savedCursor;

			@Override
			public void mouseEnter(MouseEvent e) {
				if (resourceImage != null) {
					shell = canvasRes.getShell();
					if (shell != null) {
						savedCursor = shell.getCursor();
						Cursor zoomCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_HAND);
						shell.setCursor(zoomCursor);
					}
				}
			}

			@Override
			public void mouseExit(MouseEvent e) {
				if (shell != null) {
					if (!shell.isDisposed()) {
						shell.setCursor(savedCursor);
					}
					shell = null;
				}
				savedCursor = null;
			}
		});

		canvasRes.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (resourceImageData != null && listResources.getSelectionCount() > 0) {
					String resourceName = listResources.getSelection()[0];
					String resourceIdentifier = createResourceIdentifier(resourceName);
					if (resourceShells.containsKey(resourceIdentifier)) {
						resourceShells.get(resourceIdentifier).setFocus();
					} else {
						openResourcePopupShell(resourceIdentifier);
					}
				}
			}
		});

		canvasRes.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				updateCanvasImage();
			}
		});
	}

	private void addSelectionListeners() {
		hierarchyTreeViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				currentSelectedProblem = null;
				if (!sel.isEmpty()) {
					ExtensionItem item = (ExtensionItem) sel.getFirstElement();
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
				updateCanvasImage();
			}
		});
	}

	private void updateCanvasImage() {
		if (listResources.getSelectionCount() > 0) {
			String selection = listResources.getSelection()[0];
			String prefix = getCurrentResourceFolder();
			String resourceFile = prefix + selection;
			if (resourceImage != null) {
				resourceImage.dispose();
				resourceImageData = null;
				resourceImage = null;
			}
			File file = new File(resourceFile);
			int canvasWidth = canvasRes.getBounds().width;
			int canvasHeight = canvasRes.getBounds().height;
			if (file.exists() && canvasWidth > 0 && canvasHeight > 0) {
				resourceImageData = new ImageData(resourceFile);
				int width = Math.min(canvasWidth, resourceImageData.width);
				int height = Math.min(canvasHeight, resourceImageData.height);
				resourceImage = new Image(canvasRes.getDisplay(), resourceImageData.scaledTo(width, height));
			} // else {
				// TODO: draw "not available image" picture using GC
			// }
			canvasRes.setBackgroundImage(resourceImage);
		}
	}

	private void openResourcePopupShell(final String resourceIdentifier) {
		final Shell popupShell = new Shell(SWT.ON_TOP | SWT.RESIZE | SWT.CLOSE);
		Label label = new Label(popupShell, SWT.NONE);
		Display display = Display.getDefault();
		Rectangle displayRect = display.getClientArea();

		final Image image = new Image(display, createScaledImageData(displayRect));
		label.setImage(image);

		popupShell.setLayout(new FillLayout());
		popupShell.setImage(Activator.getImage(SpotterProjectParent.IMAGE_PATH));
		String projectName = runResultItem.getProject().getName();
		popupShell.setText(String.format(DLG_RESOURCE_TITLE, resourceIdentifier, projectName));
		popupShell.pack();

		Rectangle splashRect = popupShell.getBounds();
		int x = (displayRect.width + displayRect.x - splashRect.width) / 2;
		int y = (displayRect.height + displayRect.y - splashRect.height) / 2;
		popupShell.setLocation(x, y);

		popupShell.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					popupShell.close();
				}
			}
		});
		popupShell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				image.dispose();
				resourceShells.remove(resourceIdentifier);
			}
		});

		resourceShells.put(resourceIdentifier, popupShell);
		popupShell.open();
	}

	private ImageData createScaledImageData(Rectangle displayRect) {
		int width = resourceImageData.width;
		int height = resourceImageData.height;
		int maxImageWidth = displayRect.width + displayRect.x;
		int maxImageHeight = displayRect.height + displayRect.y - DLG_RESOURCE_TOP_MARGIN;

		int scaledWidth = Math.min(width, maxImageWidth);
		int scaledHeight = Math.min(height, maxImageHeight);
		float wFactor = ((float) scaledWidth) / width;
		float hFactor = ((float) scaledHeight) / height;
		boolean scaleToFitWidth = wFactor <= hFactor;
		if (scaleToFitWidth) {
			width = scaledWidth;
			height *= wFactor;
		} else {
			width *= hFactor;
			height = scaledHeight;
		}

		return resourceImageData.scaledTo(width, height);
	}

	private String createResourceIdentifier(String resourceName) {
		return runResultItem.getText() + RESOURCE_SEPARATOR_CHAR + resourceName;
	}

	private void updateProblemDetails() {
		listResources.removeAll();
		if (resourceImage != null) {
			resourceImage.dispose();
			resourceImageData = null;
			resourceImage = null;
		}
		canvasRes.setBackgroundImage(null);

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

	@Override
	public void dispose() {
		getViewSite().getPage().removePostSelectionListener(this);
		for (Shell shell : resourceShells.values()) {
			shell.close();
		}
	}

	@Override
	public void setFocus() {
		hierarchyTreeViewer.getTree().setFocus();
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
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof SpotterProjectRunResult) {
				IFolder newFolder = ((SpotterProjectRunResult) first).getResultFolder();
				if (getResult() == null || !getResult().getResultFolder().equals(newFolder)) {
					setResult((SpotterProjectRunResult) first);
				}
			}
		}
	}

	private void updateTabs() {
		if (runResultItem == null) {
			setContentDescription(RESULTS_EMPTY_CONTENT_DESC);
			resetHierarchy();
			resetReport();
		} else {
			String contentDescription = String.format(RESULTS_CONTENT_DESC_TEMPLATE, runResultItem.getText(),
					runResultItem.getProject().getName());
			setContentDescription(contentDescription);
			updateHierarchy();
			updateReport();
		}
	}

	private void resetHierarchy() {
		hierarchyTreeViewer.setInput(new ExtensionItem());
		lblProblemName.setText(LABEL_NONE_SELECTED);
		lblDescription.setText("");
		lblStatus.setText("");
		textResult.setText("");
		listResources.removeAll();
		for (Shell shell : resourceShells.values()) {
			shell.close();
		}
		if (resourceImage != null) {
			resourceImage.dispose();
		}
		resourceImage = null;
		resourceImageData = null;
		canvasRes.setBackgroundImage(null);
	}

	private void resetReport() {
		textReport.setText(EMPTY_RESULTS);
	}

	private void updateHierarchy() {
		String filename = SpotterProjectSupport.DEFAULT_RESULTS_DIR_NAME + File.separator + runResultItem.getText()
				+ File.separator + ResultsLocationConstants.RESULTS_SERIALIZATION_FILE_NAME;
		IFile file = runResultItem.getProject().getFile(filename);
		ExtensionItem input = null;
		try {
			if (!file.isSynchronized(IResource.DEPTH_ZERO)) {
				file.refreshLocal(IResource.DEPTH_ZERO, null);
			}
			BufferedInputStream bufferedInStream = new BufferedInputStream(file.getContents());
			ObjectInputStream objectIn = new ObjectInputStream(bufferedInStream);
			resultsContainer = (ResultsContainer) objectIn.readObject();
			objectIn.close();
			bufferedInStream.close();

			input = HierarchyEditor.createPerformanceProblemHierarchy(runResultItem.getProject().getName(),
					resultsContainer.getRootProblem());
		} catch (CoreException e) {
			resultsContainer = null;
			String text = ERR_MSG_MISSING_SER_FILE + " (" + filename + ")";
			LOGGER.error(text + (e.getMessage() != null ? " (" + e.getMessage() + ")" : ""));
			MessageDialog.openWarning(null, RESULTS_VIEW_TITLE, text);
		} catch (IOException | ClassNotFoundException e) {
			resultsContainer = null;
			String text = String.format(ERR_MSG_IO_ERROR, filename);
			LOGGER.error(text + (e.getMessage() != null ? " (" + e.getMessage() + ")" : ""));
			MessageDialog.openWarning(null, RESULTS_VIEW_TITLE, text);
		} finally {
			imageProvider.setResultsContainer(resultsContainer);
			if (input == null) {
				input = new ExtensionItem();
			}
			hierarchyTreeViewer.setInput(input);
			hierarchyTreeViewer.expandAll();
		}
	}

	private void updateReport() {
		String filename = SpotterProjectSupport.DEFAULT_RESULTS_DIR_NAME + File.separator + runResultItem.getText()
				+ File.separator + ResultsLocationConstants.TXT_REPORT_FILE_NAME;
		IFile file = runResultItem.getProject().getFile(filename);
		StringBuilder sb = new StringBuilder();
		try {
			if (!file.isSynchronized(IResource.DEPTH_ZERO)) {
				file.refreshLocal(IResource.DEPTH_ZERO, null);
			}
			BufferedInputStream bufferedInStream = new BufferedInputStream(file.getContents());
			int readByte;
			while ((readByte = bufferedInStream.read()) != -1) {
				sb.append((char) readByte);
			}
			bufferedInStream.close();
			textReport.setText(sb.toString());
		} catch (CoreException e) {
			String text = ERR_MSG_MISSING_REPORT + " (" + filename + ")";
			LOGGER.error(text + (e.getMessage() != null ? " (" + e.getMessage() + ")" : ""));
			textReport.setText(text);
			MessageDialog.openWarning(null, RESULTS_VIEW_TITLE, text);
		} catch (IOException e) {
			String text = String.format(ERR_MSG_IO_ERROR, filename);
			LOGGER.error(text + (e.getMessage() != null ? " (" + e.getMessage() + ")" : ""));
			textReport.setText(text);
			MessageDialog.openWarning(null, RESULTS_VIEW_TITLE, text);
		}
	}

	private String getCurrentResourceFolder() {
		String projectRelativeRunPath = SpotterProjectSupport.DEFAULT_RESULTS_DIR_NAME + File.separator
				+ runResultItem.getText();
		IFolder folder = runResultItem.getProject().getFolder(projectRelativeRunPath);
		String currentRunFolder = folder.getLocation().toString() + "/";
		String subDirPath = currentSelectedProblem.getExtensionName() + "/"
				+ ResultsLocationConstants.RESULT_RESOURCES_SUB_DIR + "/";
		return currentRunFolder + subDirPath;
	}

}
