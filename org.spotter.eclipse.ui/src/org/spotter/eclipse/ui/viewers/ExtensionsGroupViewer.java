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
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.dialogs.AddExtensionDialog;
import org.spotter.eclipse.ui.editors.AbstractExtensionsEditor;
import org.spotter.eclipse.ui.model.ExtensionItem;
import org.spotter.eclipse.ui.model.ExtensionMetaobject;
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
 * The viewer's content provider expects input of type {@link ExtensionItem} and
 * interprets it as root element. Depending on the input's
 * <code>isConnectionIgnored()</code> return value there will be a refresh
 * button to update the connection status of all elements. When a properties
 * group viewer is set, it will be updated when the selection of the extension
 * changes.
 * </p>
 * 
 * @author Denis Knoepfle
 * 
 */
public class ExtensionsGroupViewer {

	private static final int VIEWER_CONTROL_STYLE = SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL
			| SWT.V_SCROLL;

	private static final String NO_SERVICE_CONNECTION = "No connection to Spotter Service";
	private static final String NO_EXTENSIONS = "No exension in the category was found. Ensure that "
			+ "you have placed the extension jar in the correct directory PATH/TO/PLUGINS.";

	private final AbstractExtensionsEditor editor;
	private final boolean isHierarchical;
	private final boolean ignoreConnection;
	private final ExtensionItem extensionsInput;
	private ExtensionItem currentSelectedExtension;
	private PropertiesGroupViewer propertiesGroupViewer;

	private Control viewerControl;
	private TableViewer extensionsTblViewer;
	private TreeViewer extensionsTreeViewer;
	private ColumnViewer extensionsViewer;
	private Button btnAddExtension, btnAppendExtension, btnRemoveExtension, btnRefreshExtensions;

	/**
	 * Creates a non-hierarchical extensions group viewer under the given parent
	 * which is associated with the provided editor.
	 * 
	 * @param parent
	 *            the parent of this viewer
	 * @param editor
	 *            the associated editor
	 */
	public ExtensionsGroupViewer(Composite parent, AbstractExtensionsEditor editor) {
		this(parent, editor, false);
	}

	/**
	 * Creates an extensions group viewer under the given parent which is
	 * associated with the provided editor.
	 * 
	 * @param parent
	 *            the parent of this viewer
	 * @param editor
	 *            the associated editor
	 * @param hierarchical
	 *            determines whether extension items can have children
	 */
	public ExtensionsGroupViewer(Composite parent, AbstractExtensionsEditor editor, boolean hierarchical) {
		this.editor = editor;
		this.isHierarchical = hierarchical;
		this.extensionsInput = editor.getInitialExtensionsInput();
		this.ignoreConnection = extensionsInput.isConnectionIgnored();

		createExtensionsGroup(parent);
		addButtonListeners();
		addSelectionListeners();
		addKeyListeners();

		if (extensionsInput != null) {
			extensionsInput.updateChildrenConnections();
		}
	}

	/**
	 * Sets the <code>PropertiesGroupViewer</code>. It will be updated via
	 * {@link PropertiesGroupViewer#updateProperties(ExtensionItem)
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
	 * Create a table viewer under the given parent. Initializes the viewer with
	 * the given input. Uses SpotterExtensionsContentProvider as content
	 * provider and SpotterExtensionsLabelProvider as label provider.
	 * 
	 * @param parent
	 *            The parent composite. Must not be <code>null</code>.
	 * @param input
	 *            The input of the viewer. Must not be <code>null</code>.
	 * 
	 * @return the created table viewer
	 * 
	 * @see SpotterExtensionsContentProvider
	 * @see SpotterExtensionsLabelProvider
	 */
	public static TableViewer createTableViewer(Composite parent, ExtensionItem input) {
		if (parent == null) {
			throw new IllegalArgumentException("parent must not be null");
		}
		if (input == null) {
			throw new IllegalArgumentException("parent must not be null");
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
	 *            The parent composite. Must not be <code>null</code>.
	 * @param input
	 *            The input of the viewer. Must not be <code>null</code>.
	 * 
	 * @return the created table viewer
	 * 
	 * @see SpotterExtensionsContentProvider
	 * @see SpotterExtensionsLabelProvider
	 */
	public static TreeViewer createTreeViewer(Composite parent, ExtensionItem input) {
		if (parent == null) {
			throw new IllegalArgumentException("parent must not be null");
		}
		if (input == null) {
			throw new IllegalArgumentException("parent must not be null");
		}
		// configure tree layout
		Composite treeExtensionsComp = new Composite(parent, SWT.NONE);
		treeExtensionsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
		treeViewer.setContentProvider(new SpotterExtensionsContentProvider());
		treeViewer.setLabelProvider(new SpotterExtensionsLabelProvider());
		treeViewer.setInput(input);

		return treeViewer;
	}

	private void createExtensionsGroup(Composite container) {
		Group grpConfiguredComponents = new Group(container, SWT.NONE);
		grpConfiguredComponents.setText("configured components");
		grpConfiguredComponents.setLayout(WidgetUtils.createGridLayout(2));

		extensionsViewer = createViewerControl(grpConfiguredComponents);
		viewerControl = extensionsViewer.getControl();

		Composite buttonComp = new Composite(grpConfiguredComponents, SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		RowLayout buttonCompLayout = new RowLayout(SWT.VERTICAL);
		buttonCompLayout.fill = true;
		buttonCompLayout.center = true;
		buttonCompLayout.pack = false;
		buttonCompLayout.spacing = WidgetUtils.DEFAULT_VERTICAL_SPACING;
		buttonComp.setLayout(buttonCompLayout);

		createButtons(buttonComp);
	}

	private ColumnViewer createViewerControl(Composite parent) {
		if (isHierarchical) {
			extensionsTreeViewer = createTreeViewer(parent, extensionsInput);
			extensionsTreeViewer.expandAll();
			return extensionsTreeViewer;
		} else {
			extensionsTblViewer = createTableViewer(parent, extensionsInput);
			return extensionsTblViewer;
		}
	}

	private void createButtons(Composite parent) {
		btnAddExtension = new Button(parent, SWT.PUSH);
		btnAddExtension.setText("Add...");
		btnAddExtension.setToolTipText("Opens a dialog to add more extensions");

		if (isHierarchical) {
			btnAppendExtension = new Button(parent, SWT.PUSH);
			btnAppendExtension.setText("Append...");
			btnAppendExtension.setToolTipText("Opens a dialog to append extensions to the selected item");
			btnAppendExtension.setEnabled(false);
		}

		btnRemoveExtension = new Button(parent, SWT.PUSH);
		btnRemoveExtension.setText("Remove");
		btnRemoveExtension.setToolTipText("Removes the selected extension");
		btnRemoveExtension.setEnabled(false);

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
	private void showAndHandleAddDialog(ExtensionItem parentItem) {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		ExtensionMetaobject[] extensions = editor.getAvailableExtensions();
		if (extensions == null) {
			return;
		} else if (extensions.length == 0) {
			DialogUtils.openInformation(NO_EXTENSIONS);
			return;
		}

		// TODO: refactor AddExtensionsDialog to reuse shared parts of
		// AddConfigParamDialog
		AddExtensionDialog dialog = new AddExtensionDialog(shell, extensions);

		if (dialog.open() == Window.OK) {
			ExtensionMetaobject[] result = dialog.getResult();
			IModelWrapper parentWrapper = parentItem.getModelWrapper();
			Object xmlParent = parentWrapper == null ? null : parentWrapper.getXMLModel();
			ExtensionItem lastAdded = null;
			for (ExtensionMetaobject component : result) {
				ExtensionItem item = processAddedComponent(xmlParent, component);
				parentItem.addItem(item);
				item.updateConnectionStatus();
				lastAdded = item;
			}
			btnRemoveExtension.setEnabled(true);
			if (!ignoreConnection) {
				btnRefreshExtensions.setEnabled(true);
			}
			extensionsViewer.setSelection(new StructuredSelection(lastAdded));
			editor.markDirty();
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
	private ExtensionItem processAddedComponent(Object xmlParent, ExtensionMetaobject extension) {
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
		return new ExtensionItem(modelWrapper);
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

		btnRemoveExtension.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedExtension();
			}
		});

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
					if (isHierarchical && !btnAppendExtension.isEnabled()) {
						btnAppendExtension.setEnabled(true);
					}
					currentSelectedExtension = (ExtensionItem) sel.getFirstElement();
				} else {
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

	private void addKeyListeners() {
		viewerControl.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					removeSelectedExtension();
				}
			}
		});
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

	/**
	 * Removes the selected extension.
	 */
	private void removeSelectedExtension() {
		StructuredSelection sel = (StructuredSelection) extensionsViewer.getSelection();
		if (sel.isEmpty()) {
			return;
		}
		ExtensionItem item = (ExtensionItem) sel.getFirstElement();
		ExtensionItem parentItem = item.getParent();
		int index = parentItem.getItemIndex(item);
		if (index != -1) {
			parentItem.removeItem(index);
			if (parentItem.hasItems()) {
				// parent still has items left, so select next child
				index = Math.min(index, parentItem.getItemCount() - 1);
				extensionsViewer.setSelection(new StructuredSelection(parentItem.getItem(index)));
			} else if (parentItem != extensionsInput) {
				// root not reached yet, so select parent item
				extensionsViewer.setSelection(new StructuredSelection(parentItem));
			} else {
				// no elements left
				btnRemoveExtension.setEnabled(false);
				if (!ignoreConnection) {
					btnRefreshExtensions.setEnabled(false);
				}
			}
			editor.markDirty();
		}
	}

}
