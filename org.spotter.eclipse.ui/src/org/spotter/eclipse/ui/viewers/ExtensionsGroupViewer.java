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
package org.spotter.eclipse.ui.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.actions.CopyExtensionAction;
import org.spotter.eclipse.ui.actions.CutExtensionAction;
import org.spotter.eclipse.ui.actions.DeleteExtensionAction;
import org.spotter.eclipse.ui.actions.PasteExtensionAction;
import org.spotter.eclipse.ui.dialogs.AddExtensionDialog;
import org.spotter.eclipse.ui.dnd.ExtensionDragListener;
import org.spotter.eclipse.ui.dnd.ExtensionDropListener;
import org.spotter.eclipse.ui.editors.AbstractExtensionsEditor;
import org.spotter.eclipse.ui.listeners.IItemChangedListener;
import org.spotter.eclipse.ui.model.BasicEditorExtensionItemFactory;
import org.spotter.eclipse.ui.model.ExtensionMetaobject;
import org.spotter.eclipse.ui.model.IExtensionItem;
import org.spotter.eclipse.ui.model.IExtensionItemFactory;
import org.spotter.eclipse.ui.model.xml.IModelWrapper;
import org.spotter.eclipse.ui.providers.SpotterExtensionsContentProvider;
import org.spotter.eclipse.ui.providers.SpotterExtensionsLabelProvider;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.eclipse.ui.util.WidgetUtils;
import org.spotter.shared.environment.model.XMConfiguration;

/**
 * This viewer is ready-to-use and views a extensions group containing a table
 * or tree (depending on the <code>hierarchical</code> flag) and some controls
 * to edit the configured components.
 * <p>
 * The viewer looks best if placed within a composite with a
 * <code>FillLayout</code> or similar in order to use all the available space.
 * The viewer's content provider expects input of type {@link IExtensionItem}
 * and interprets it as root element. Depending on the input's
 * <code>isConnectionIgnored()</code> return value there will be a refresh
 * button to update the connection status of all elements. When a properties
 * group viewer is set, it will be updated when the selection of the extension
 * changes.
 * </p>
 * 
 * @author Denis Knoepfle
 * 
 */
public class ExtensionsGroupViewer implements IItemChangedListener {

	private static final int VIEWER_CONTROL_STYLE = SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL
			| SWT.V_SCROLL;

	private static final String NO_SERVICE_CONNECTION = "No connection to Spotter Service";
	private static final String NO_EXTENSIONS = "No extension in the category was found. Ensure that "
			+ "you have placed the extension jar in the correct directory PATH/TO/PLUGINS.";

	private final AbstractExtensionsEditor editor;
	private final boolean isHierarchical;
	private final boolean ignoreConnection;
	private final IExtensionItem extensionsInput;
	private final IExtensionItemFactory extensionItemFactory;
	private IExtensionItem currentSelectedExtension;
	private PropertiesGroupViewer propertiesGroupViewer;

	private Control viewerControl;
	private TableViewer extensionsTblViewer;
	private TreeViewer extensionsTreeViewer;
	private StructuredViewer extensionsViewer;
	private Clipboard clipboard;
	private DeleteExtensionAction deleteExtensionAction;
	private Button btnAddExtension, btnAppendExtension, btnRemoveExtension, btnRefreshExtensions;

	/**
	 * Creates an extensions group viewer under the given parent which is
	 * associated with the provided editor.
	 * 
	 * @param parent
	 *            the parent of this viewer
	 * @param editor
	 *            the associated editor, must not be <code>null</code>
	 * @param hierarchical
	 *            determines whether extension items can have children
	 * @param editSupport
	 *            determines whether edit support like copy, cut, paste, remove
	 *            and drag 'n drop is enabled
	 */
	public ExtensionsGroupViewer(Composite parent, AbstractExtensionsEditor editor, boolean hierarchical,
			boolean editSupport) {
		if (editor == null) {
			throw new IllegalArgumentException("editor must not be null");
		}
		this.editor = editor;
		this.isHierarchical = hierarchical;
		this.extensionsInput = editor.getInitialExtensionsInput();
		this.extensionItemFactory = new BasicEditorExtensionItemFactory(editor.getEditorId());
		this.ignoreConnection = extensionsInput == null ? true : extensionsInput.isConnectionIgnored();

		createExtensionsGroup(parent, editSupport);
		addButtonListeners();
		addSelectionListeners();

		if (extensionsInput != null) {
			extensionsInput.addItemChangedListener(this);
			extensionsInput.updateChildrenConnections();
		}
	}

	/**
	 * Sets the <code>PropertiesGroupViewer</code>. It will be updated via
	 * {@link PropertiesGroupViewer#updateProperties(IExtensionItem)
	 * updateProperties(ExtensionItem)} whenever a extension item is selected.
	 * 
	 * @param viewer
	 *            the viewer
	 */
	public void setPropertiesGroupViewer(PropertiesGroupViewer viewer) {
		this.propertiesGroupViewer = viewer;
	}

	/**
	 * Asks this viewer to take focus.
	 */
	public void setFocus() {
		viewerControl.setFocus();
	}

	/**
	 * Disposes of this viewer.
	 */
	public void dispose() {
		if (clipboard != null) {
			clipboard.dispose();
		}
		if (extensionsInput != null) {
			extensionsInput.removeItemChangedListener(this);
		}
	}

	/**
	 * @return the underlying viewer
	 */
	public StructuredViewer getViewer() {
		return extensionsViewer;
	}

	/**
	 * Create a table viewer under the given parent. Initializes the viewer with
	 * the given input. Uses SpotterExtensionsContentProvider as content
	 * provider and SpotterExtensionsLabelProvider as label provider.
	 * 
	 * @param parent
	 *            The parent composite. Must not be <code>null</code>.
	 * @param input
	 *            The input of the viewer. Must not be <code>null</code>.
	 * @param editor
	 *            The underlying editor if any or <code>null</code>.
	 * @param dragAndDropSupport
	 *            Determines whether drag 'n drop is supported.
	 * 
	 * @return the created table viewer
	 * 
	 * @see SpotterExtensionsContentProvider
	 * @see SpotterExtensionsLabelProvider
	 */
	public static TableViewer createTableViewer(Composite parent, IExtensionItem input,
			AbstractExtensionsEditor editor, boolean dragAndDropSupport) {
		if (parent == null) {
			throw new IllegalArgumentException("parent must not be null");
		}
		if (input == null) {
			throw new IllegalArgumentException("input must not be null");
		}
		// configure table layout
		Composite tblExtensionsComp = new Composite(parent, SWT.NONE);
		tblExtensionsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout tblExtensionsColLayout = new TableColumnLayout();
		tblExtensionsComp.setLayout(tblExtensionsColLayout);
		// create table
		Table table = new Table(tblExtensionsComp, VIEWER_CONTROL_STYLE);
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		// create viewer for table
		TableViewer tableViewer = new TableViewer(table);
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
		TableViewerColumn extensionsColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		tblExtensionsColLayout.setColumnData(extensionsColumn.getColumn(), new ColumnWeightData(1));

		if (dragAndDropSupport) {
			addDragAndDropSupport(tableViewer, editor, false);
		}
		tableViewer.setContentProvider(new SpotterExtensionsContentProvider());
		tableViewer.setLabelProvider(new SpotterExtensionsLabelProvider());
		tableViewer.setInput(input);

		return tableViewer;
	}

	/**
	 * Create a tree viewer under the given parent. Initializes the viewer with
	 * the given input. Uses SpotterExtensionsContentProvider as content
	 * provider and SpotterExtensionsLabelProvider as label provider.
	 * 
	 * @param parent
	 *            The parent composite. Must not be <code>null</code>. It is
	 *            recommended to use a {@link GridLayout} on the parent or at
	 *            least a layout that has set the <i>fill flag</i>.
	 * @param input
	 *            The input of the viewer. Must not be <code>null</code>.
	 * @param editor
	 *            The underlying editor if any or <code>null</code>.
	 * @param dragAndDropSupport
	 *            Determines whether drag 'n drop is supported.
	 * 
	 * @return the created table viewer
	 * 
	 * @see SpotterExtensionsContentProvider
	 * @see SpotterExtensionsLabelProvider
	 */
	public static TreeViewer createTreeViewer(Composite parent, IExtensionItem input, AbstractExtensionsEditor editor,
			boolean dragAndDropSupport) {
		if (parent == null) {
			throw new IllegalArgumentException("parent must not be null");
		}
		if (input == null) {
			throw new IllegalArgumentException("input must not be null");
		}
		// configure tree layout
		Composite treeExtensionsComp = new Composite(parent, SWT.NONE);
		if (parent.getLayout() instanceof GridLayout) {
			treeExtensionsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		TreeColumnLayout treeExtensionsColLayout = new TreeColumnLayout();
		treeExtensionsComp.setLayout(treeExtensionsColLayout);
		// create tree
		Tree tree = new Tree(treeExtensionsComp, VIEWER_CONTROL_STYLE);
		tree.setHeaderVisible(false);
		tree.setLinesVisible(false);
		// create viewer for tree
		TreeViewer treeViewer = new TreeViewer(tree);
		ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);
		TreeViewerColumn extensionsColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		treeExtensionsColLayout.setColumnData(extensionsColumn.getColumn(), new ColumnWeightData(1));

		if (dragAndDropSupport) {
			addDragAndDropSupport(treeViewer, editor, true);
		}
		treeViewer.setContentProvider(new SpotterExtensionsContentProvider());
		treeViewer.setLabelProvider(new SpotterExtensionsLabelProvider());
		treeViewer.setInput(input);

		return treeViewer;
	}

	private static void addDragAndDropSupport(StructuredViewer viewer, AbstractExtensionsEditor editor,
			boolean hierarchical) {
		final int operations = DND.DROP_MOVE | DND.DROP_COPY;
		Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer.getTransfer() };

		viewer.addDragSupport(operations, transferTypes, new ExtensionDragListener(viewer, editor));
		viewer.addDropSupport(operations, transferTypes, new ExtensionDropListener(viewer, editor, hierarchical));
	}

	private void createExtensionsGroup(Composite container, boolean editSupport) {
		Group grpConfiguredComponents = new Group(container, SWT.NONE);
		grpConfiguredComponents.setText("configured components");
		grpConfiguredComponents.setLayout(WidgetUtils.createGridLayout(2));

		extensionsViewer = createViewerControl(grpConfiguredComponents, editSupport);
		viewerControl = extensionsViewer.getControl();

		Composite buttonComp = new Composite(grpConfiguredComponents, SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		RowLayout buttonCompLayout = new RowLayout(SWT.VERTICAL);
		buttonCompLayout.fill = true;
		buttonCompLayout.center = true;
		buttonCompLayout.pack = false;
		buttonCompLayout.spacing = WidgetUtils.DEFAULT_VERTICAL_SPACING;
		buttonComp.setLayout(buttonCompLayout);

		createButtons(buttonComp, editSupport);
	}

	private ColumnViewer createViewerControl(Composite parent, boolean editSupport) {
		ColumnViewer columnViewer;
		if (isHierarchical) {
			extensionsTreeViewer = createTreeViewer(parent, extensionsInput, editor, editSupport);
			extensionsTreeViewer.expandAll();
			columnViewer = extensionsTreeViewer;
		} else {
			extensionsTblViewer = createTableViewer(parent, extensionsInput, editor, editSupport);
			columnViewer = extensionsTblViewer;
		}

		if (editSupport) {
			IEditorSite editorSite = editor.getEditorSite();
			if (editorSite == null) {
				throw new IllegalStateException("cannot initialize actions when editor site is null");
			}
			// enable copy/cut/paste functionality
			clipboard = new Clipboard(editorSite.getShell().getDisplay());
			IActionBars bars = editorSite.getActionBars();
			String editorId = editor.getEditorId();
			deleteExtensionAction = new DeleteExtensionAction(columnViewer, editor);
			CopyExtensionAction copyExtensionAction = new CopyExtensionAction(columnViewer, clipboard, editorId);
			bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteExtensionAction);
			bars.setGlobalActionHandler(ActionFactory.CUT.getId(), new CutExtensionAction(columnViewer,
					copyExtensionAction, deleteExtensionAction));
			bars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyExtensionAction);
			bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), new PasteExtensionAction(columnViewer, editorId));

			bars.updateActionBars();
		}

		return columnViewer;
	}

	private void createButtons(Composite parent, boolean editSupport) {
		btnAddExtension = new Button(parent, SWT.PUSH);
		btnAddExtension.setText("Add...");
		btnAddExtension.setToolTipText("Opens a dialog to add more extensions");

		if (isHierarchical) {
			btnAppendExtension = new Button(parent, SWT.PUSH);
			btnAppendExtension.setText("Append...");
			btnAppendExtension.setToolTipText("Opens a dialog to append extensions to the selected item");
			btnAppendExtension.setEnabled(false);
		}

		if (editSupport) {
			btnRemoveExtension = new Button(parent, SWT.PUSH);
			btnRemoveExtension.setText("Remove");
			btnRemoveExtension.setToolTipText("Removes the selected extension");
			btnRemoveExtension.setEnabled(false);
		}

		if (!ignoreConnection) {
			btnRefreshExtensions = new Button(parent, SWT.PUSH);
			btnRefreshExtensions.setText("Refresh");
			btnRefreshExtensions.setToolTipText("Refreshes the connection status of the configured components");
			btnRefreshExtensions.setEnabled(extensionsInput != null && extensionsInput.hasItems());
		}
	}

	/**
	 * Opens and handles a dialog showing available extensions that can be
	 * added.
	 * 
	 * @param parentItem
	 *            the extension item under which the newly created components
	 *            will be added
	 */
	private void showAndHandleAddDialog(IExtensionItem parentItem) {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		ExtensionMetaobject[] extensions = editor.getAvailableExtensions();
		if (extensions == null) {
			return;
		} else if (extensions.length == 0) {
			DialogUtils.openInformation(NO_EXTENSIONS);
			return;
		}

		AddExtensionDialog dialog = new AddExtensionDialog(shell, extensions);
		Control previousFocusControl = Display.getCurrent().getFocusControl();

		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			IModelWrapper parentWrapper = parentItem.getModelWrapper();
			Object xmlParent = parentWrapper == null ? null : parentWrapper.getXMLModel();
			IExtensionItem lastAdded = null;
			for (Object component : result) {
				ExtensionMetaobject metaobject = (ExtensionMetaobject) component;
				IExtensionItem item = processAddedComponent(xmlParent, metaobject);
				parentItem.addItem(item);
				item.updateConnectionStatus();
				lastAdded = item;
			}
			extensionsViewer.setSelection(new StructuredSelection(lastAdded));
			editor.markDirty();
		}

		if (previousFocusControl != null && !previousFocusControl.isFocusControl()) {
			previousFocusControl.forceFocus();
		}
	}

	/**
	 * Processes the given extension component.
	 * 
	 * @param xmlParent
	 *            the XML parent model that will receive the created model
	 * @param extension
	 *            the extension
	 * @return an <code>ExtensionItem</code> object that contains a suitable
	 *         model wrapper
	 */
	private IExtensionItem processAddedComponent(Object xmlParent, ExtensionMetaobject extension) {
		IModelWrapper modelWrapper = editor.createModelWrapper(xmlParent, extension);
		List<XMConfiguration> xmConfigList = new ArrayList<XMConfiguration>();

		for (ConfigParameterDescription confDescr : modelWrapper.getExtensionConfigParams()) {
			if (confDescr.isMandatory()) {
				XMConfiguration xmConfig = new XMConfiguration();
				String key = confDescr.getName();
				xmConfig.setKey(key);
				String value = confDescr.getDefaultValue();
				xmConfig.setValue(value);
				xmConfigList.add(xmConfig);
			}
		}
		if (!xmConfigList.isEmpty()) {
			modelWrapper.setConfig(xmConfigList);
		}
		return extensionItemFactory.createExtensionItem(modelWrapper);
	}

	private void addButtonListeners() {
		btnAddExtension.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// append items to the root
				showAndHandleAddDialog(extensionsInput);
			}
		});

		if (isHierarchical) {
			btnAppendExtension.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// append items to the selected item
					showAndHandleAddDialog(currentSelectedExtension);
				}
			});
		}

		if (btnRemoveExtension != null) {
			btnRemoveExtension.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					deleteExtensionAction.run();
				}
			});
		}

		if (!ignoreConnection) {
			btnRefreshExtensions.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ServiceClientWrapper client = Activator.getDefault().getClient(editor.getProject().getName());
					if (client.testConnection(true)) {
						extensionsInput.updateChildrenConnections();
					} else {
						extensionsInput.setChildrenError(NO_SERVICE_CONNECTION);
					}
				}
			});
		}
	}

	private void addSelectionListeners() {
		extensionsViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					btnRemoveExtension.setEnabled(true);
					if (isHierarchical) {
						btnAppendExtension.setEnabled(true);
					}
					currentSelectedExtension = (IExtensionItem) sel.getFirstElement();
				} else {
					btnRemoveExtension.setEnabled(false);
					if (isHierarchical) {
						btnAppendExtension.setEnabled(false);
					}
					currentSelectedExtension = null;
				}
				updateProperties();
			}
		});
		if (extensionsTreeViewer != null) {
			extensionsTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					Object selectedNode = selection.getFirstElement();
					extensionsTreeViewer.setExpandedState(selectedNode,
							!extensionsTreeViewer.getExpandedState(selectedNode));
				}
			});
		}
	}

	/**
	 * Updates the properties in the <code>PropertiesGroupViewer</code> if one
	 * is set and not <code>null</code>.
	 */
	private void updateProperties() {
		if (propertiesGroupViewer != null) {
			propertiesGroupViewer.updateProperties(currentSelectedExtension);
		}
	}

	@Override
	public void childAdded(IExtensionItem parent, IExtensionItem item) {
		updateRefreshButton();
	}

	@Override
	public void childRemoved(IExtensionItem parent, IExtensionItem item) {
		updateRefreshButton();
	}

	@Override
	public void appearanceChanged(IExtensionItem item) {
	}

	private void updateRefreshButton() {
		if (!ignoreConnection) {
			btnRefreshExtensions.setEnabled(extensionsInput != null && extensionsInput.hasItems());
		}
	}

}
