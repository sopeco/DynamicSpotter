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

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.dialogs.AddConfigParamDialog;
import org.spotter.eclipse.ui.editors.AbstractSpotterEditor;
import org.spotter.eclipse.ui.editors.PropertiesEditingSupport;
import org.spotter.eclipse.ui.model.AbstractPropertyItem;
import org.spotter.eclipse.ui.model.ConfigParamPropertyItem;
import org.spotter.eclipse.ui.model.ExtensionItem;
import org.spotter.eclipse.ui.providers.PropertiesContentProvider;
import org.spotter.eclipse.ui.providers.PropertiesLabelProvider;
import org.spotter.eclipse.ui.util.AbstractNameFormatter;
import org.spotter.eclipse.ui.util.SpotterUtils;
import org.spotter.eclipse.ui.util.WidgetUtils;
import org.spotter.shared.environment.model.XMConfiguration;

/**
 * This viewer is ready-to-use and views a properties group containing a table
 * and some controls to edit the properties.
 * <p>
 * The viewer looks best if placed within a composite with a
 * <code>FillLayout</code> or similar in order to use all the available space.
 * The viewer's content provider expects input of type {@link ExtensionItem}.
 * The input model can be updated via {@link #updateProperties(ExtensionItem)}.
 * </p>
 * 
 * @author Denis Knoepfle
 * 
 */
public class PropertiesGroupViewer {

	private static final int TABLE_COLUMN_NAME_WEIGHT = 40;
	private static final int TABLE_COLUMN_VALUE_WEIGHT = 60;
	private static final int GRP_PROPERTIES_COLUMNS = 3;
	private static final int TABLE_COMPOSITE_HOR_SPAN = 4;

	private static class PropertiesComparator extends ViewerComparator {

		private boolean mandatoriesFirst = false;

		/**
		 * Toggles the sort mode. This either enables or disables prioritizing
		 * of mandatory-flagged properties.
		 */
		public void toggleSortMode() {
			mandatoriesFirst = !mandatoriesFirst;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			AbstractPropertyItem i1 = (AbstractPropertyItem) e1;
			AbstractPropertyItem i2 = (AbstractPropertyItem) e2;

			return mandatoriesFirst ? doCompare(i1, i2) : doCompareIgnoreMandatory(i1, i2);
		}

		private int doCompare(AbstractPropertyItem e1, AbstractPropertyItem e2) {
			int mandatory1 = e1.getConfigParameterDescription().isMandatory() ? 0 : 1;
			int mandatory2 = e2.getConfigParameterDescription().isMandatory() ? 0 : 1;

			// places mandatory items before non-mandatory items
			int diff = mandatory1 - mandatory2;

			if (diff != 0) {
				return diff;
			} else {
				return doCompareIgnoreMandatory(e1, e2);
			}
		}

		private int doCompareIgnoreMandatory(AbstractPropertyItem e1, AbstractPropertyItem e2) {
			return e1.getName().compareToIgnoreCase(e2.getName());
		}

	}

	private final AbstractSpotterEditor editor;
	private ExtensionItem inputModel;
	private AbstractNameFormatter nameFormatter;

	private Table tblProperties;
	private TableViewer propertiesTblViewer;
	private PropertiesEditingSupport editingSupport;
	private Combo comboDisplayName;
	private Button btnAddProperty, btnRestorePropertyDefault, btnRemoveProperty, btnClearAllProperties;

	/**
	 * Creates a new instance of this viewer.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param editor
	 *            the associated editor
	 */
	public PropertiesGroupViewer(Composite parent, AbstractSpotterEditor editor) {
		this.editor = editor;
		this.inputModel = null;

		createPropertiesGroup(parent);
		addButtonListeners();
		addSelectionListeners();
		addKeyListeners();
	}

	/**
	 * @return the current name formatter in use
	 */
	public AbstractNameFormatter getNameFormatter() {
		return nameFormatter;
	}

	/**
	 * Updates the properties and sets the new internal input model.
	 * 
	 * @param inputModel
	 *            the new input model
	 */
	public void updateProperties(ExtensionItem inputModel) {
		this.inputModel = inputModel;
		propertiesTblViewer.setInput(inputModel);
		boolean enabled = inputModel == null ? false : inputModel.hasConfigurableExtensionConfigParams();
		btnAddProperty.setEnabled(enabled);
		if (btnRemoveProperty.isEnabled()) {
			btnRemoveProperty.setEnabled(false);
		}
		if (btnRestorePropertyDefault.isEnabled()) {
			btnRestorePropertyDefault.setEnabled(false);
		}
		initClearAllPropertiesBtn();
	}

	/**
	 * @return the input model that is currently in use
	 */
	public ExtensionItem getInputModel() {
		return inputModel;
	}

	/**
	 * Asks this viewer to take focus.
	 */
	public void setFocus() {
		if (tblProperties != null) {
			tblProperties.setFocus();
		}
	}

	/**
	 * @return the table viewer used for displaying the properties
	 */
	public TableViewer getTableViewer() {
		return propertiesTblViewer;
	}

	private void createPropertiesGroup(Composite container) {
		Group grpProperties = new Group(container, SWT.NONE);
		grpProperties.setText("properties");
		grpProperties.setLayout(WidgetUtils.createGridLayout(GRP_PROPERTIES_COLUMNS));

		createTableViewer(grpProperties);
		createTraversalSupport(propertiesTblViewer);
		createNameFormatters(grpProperties);
		createButtons(grpProperties);
	}

	private void createTableViewer(Composite grpProperties) {
		// configure table layout
		Composite tblPropertiesComp = new Composite(grpProperties, SWT.NONE);
		tblPropertiesComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, TABLE_COMPOSITE_HOR_SPAN, 1));
		TableColumnLayout tableColLayout = new TableColumnLayout();
		tblPropertiesComp.setLayout(tableColLayout);
		// create table
		tblProperties = new Table(tblPropertiesComp, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL);
		tblProperties.setHeaderVisible(true);
		tblProperties.setLinesVisible(true);
		// create viewer for table
		propertiesTblViewer = new TableViewer(tblProperties);
		ColumnViewerToolTipSupport.enableFor(propertiesTblViewer, ToolTip.NO_RECREATE);
		TableViewerColumn nameColumn = new TableViewerColumn(propertiesTblViewer, SWT.NONE);
		nameColumn.getColumn().setText("name");
		tableColLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(TABLE_COLUMN_NAME_WEIGHT));

		TableViewerColumn valueColumn = new TableViewerColumn(propertiesTblViewer, SWT.NONE);
		valueColumn.getColumn().setText("value");
		tableColLayout.setColumnData(valueColumn.getColumn(), new ColumnWeightData(TABLE_COLUMN_VALUE_WEIGHT));
		editingSupport = new PropertiesEditingSupport(valueColumn.getViewer(), editor, this);
		valueColumn.setEditingSupport(editingSupport);
		propertiesTblViewer.setContentProvider(new PropertiesContentProvider());
		propertiesTblViewer.setLabelProvider(new PropertiesLabelProvider(this));

		PropertiesComparator comparator = new PropertiesComparator();
		nameColumn.getColumn().addSelectionListener(createColumnSelectionAdapter(comparator));
		propertiesTblViewer.setComparator(comparator);
	}

	private void createTraversalSupport(final TableViewer tableViewer) {
		final Table table = tableViewer.getTable();
		TraverseListener traverseListener = new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.TAB) {
					int index = table.getSelectionIndex();
					int shiftState = e.stateMask & SWT.SHIFT;
					if (shiftState != 0 && index > 0 || shiftState == 0 && index < table.getItemCount() - 1) {
						e.doit = false;
					}
				}
			}
		};
		KeyListener keyListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.TAB && !tableViewer.getSelection().isEmpty()) {
					int index = table.getSelectionIndex();
					if ((e.stateMask & SWT.SHIFT) != 0) {
						// backwards
						index--;
						if (index >= 0) {
							Object element = tableViewer.getElementAt(index);
							table.setSelection(index);
							tableViewer.setSelection(new StructuredSelection(element));
						}
					} else {
						// forwards
						index++;
						if (index < table.getItemCount()) {
							Object element = tableViewer.getElementAt(index);
							table.setSelection(index);
							tableViewer.setSelection(new StructuredSelection(element));
						}
					}
				}
			}
		};

		table.addTraverseListener(traverseListener);
		table.addKeyListener(keyListener);
	}

	private void createNameFormatters(Composite grpProperties) {
		Label lblDisplayName = new Label(grpProperties, SWT.LEFT);
		lblDisplayName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblDisplayName.setText("display name");

		comboDisplayName = new Combo(grpProperties, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboDisplayName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		nameFormatter = SpotterUtils.NAME_FORMATTERS.length == 0 ? null : SpotterUtils.NAME_FORMATTERS[0];
		String[] formatterNames = new String[SpotterUtils.NAME_FORMATTERS.length];
		for (int i = 0; i < formatterNames.length; ++i) {
			formatterNames[i] = SpotterUtils.NAME_FORMATTERS[i].getFormatterName();
		}
		comboDisplayName.setItems(formatterNames);
		comboDisplayName.select(0);
	}

	private void createButtons(Composite grpProperties) {
		// button bar composite to make buttons equal width
		Composite btnBarComp = new Composite(grpProperties, SWT.NONE);
		btnBarComp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
		fillLayout.spacing = WidgetUtils.DEFAULT_HORIZONTAL_SPACING;
		btnBarComp.setLayout(fillLayout);

		btnClearAllProperties = new Button(btnBarComp, SWT.PUSH);
		btnClearAllProperties.setText("Clear All");
		btnClearAllProperties.setToolTipText("Removes all non-mandatory items");
		btnClearAllProperties.setEnabled(false);

		btnRemoveProperty = new Button(btnBarComp, SWT.PUSH);
		btnRemoveProperty.setText("Remove");
		btnRemoveProperty.setToolTipText("Removes all selected non-mandatory items");
		btnRemoveProperty.setEnabled(false);

		btnRestorePropertyDefault = new Button(btnBarComp, SWT.PUSH);
		btnRestorePropertyDefault.setText("Restore Default");
		btnRestorePropertyDefault.setToolTipText("Restores the default value of all selected items");
		btnRestorePropertyDefault.setEnabled(false);

		btnAddProperty = new Button(btnBarComp, SWT.PUSH);
		btnAddProperty.setText("Add...");
		btnAddProperty.setToolTipText("Opens a dialog to add more properties");
		btnAddProperty.setEnabled(false);
	}

	private SelectionListener createColumnSelectionAdapter(final PropertiesComparator comparator) {
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.toggleSortMode();
				propertiesTblViewer.refresh();
			}
		};
		return listener;
	}

	private void addButtonListeners() {
		btnAddProperty.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = Display.getCurrent().getActiveShell();
				Collection<ConfigParameterDescription> remainingDescs = inputModel
						.getConfigurableExtensionConfigParams();
				ConfigParameterDescription[] descriptions = new ConfigParameterDescription[remainingDescs.size()];
				AddConfigParamDialog dialog = new AddConfigParamDialog(shell, remainingDescs.toArray(descriptions));
				Control previousFocusControl = Display.getCurrent().getFocusControl();

				if (dialog.open() == Window.OK) {
					for (Object selectedDesc : dialog.getResult()) {
						inputModel.addConfigParamUsingDescription((ConfigParameterDescription) selectedDesc);
					}
					btnClearAllProperties.setEnabled(true);
					if (!inputModel.hasConfigurableExtensionConfigParams()) {
						btnAddProperty.setEnabled(false);
					}
					editor.markDirty();
				}

				if (previousFocusControl != null && !previousFocusControl.isFocusControl()) {
					previousFocusControl.forceFocus();
				}
			}
		});

		btnRestorePropertyDefault.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) propertiesTblViewer.getSelection();
				for (Object element : sel.toArray()) {
					AbstractPropertyItem item = (AbstractPropertyItem) element;
					ConfigParameterDescription desc = item.getConfigParameterDescription();
					String oldVal = item.getValue();
					String defaultVal = desc.getDefaultValue();
					boolean needsUpdate = false;
					if (oldVal != null) {
						needsUpdate = !oldVal.equals(defaultVal);
					} else if (defaultVal != null) {
						needsUpdate = !defaultVal.equals(oldVal);
					}
					if (needsUpdate) {
						item.updateValue(defaultVal);
						inputModel.propertyDirty(item);
						inputModel.fireItemAppearanceChanged();
						editor.markDirty();
					}
				}
			}
		});

		btnRemoveProperty.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedProperties();
			}
		});

		btnClearAllProperties.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearAllNonMandatoryProperties();
			}
		});
	}

	private void initClearAllPropertiesBtn() {
		if (inputModel == null) {
			btnClearAllProperties.setEnabled(false);
			return;
		}
		List<XMConfiguration> xmConfigList = inputModel.getModelWrapper().getConfig();
		if (xmConfigList != null) {
			for (XMConfiguration conf : xmConfigList) {
				ConfigParameterDescription desc = inputModel.getExtensionConfigParam(conf.getKey());
				if (desc != null && !desc.isMandatory()) {
					btnClearAllProperties.setEnabled(true);
					return;
				}
			}
		}
		btnClearAllProperties.setEnabled(false);
	}

	private void addSelectionListeners() {
		tblProperties.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) propertiesTblViewer.getSelection();
				boolean restoreEnabled = !sel.isEmpty();
				boolean removeEnabled = false;
				for (Object element : sel.toArray()) {
					AbstractPropertyItem item = (AbstractPropertyItem) element;
					removeEnabled = !item.getConfigParameterDescription().isMandatory();
					if (removeEnabled) {
						break;
					}
				}
				if (btnRestorePropertyDefault.isEnabled() != restoreEnabled) {
					btnRestorePropertyDefault.setEnabled(restoreEnabled);
				}
				if (btnRemoveProperty.isEnabled() != removeEnabled) {
					btnRemoveProperty.setEnabled(removeEnabled);
				}
			}
		});

		comboDisplayName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int n = comboDisplayName.getSelectionIndex();
				if (n == -1) {
					return;
				}
				nameFormatter = SpotterUtils.NAME_FORMATTERS[n];
				if (inputModel != null) {
					inputModel.fireItemPropertiesChanged();
				}
			}
		});
	}

	private void addKeyListeners() {
		tblProperties.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					int index = tblProperties.getSelectionIndex();
					if (index != -1) {
						Object element = propertiesTblViewer.getElementAt(index);
						propertiesTblViewer.editElement(element, 1);
					}
				} else if (e.keyCode == SWT.DEL) {
					removeSelectedProperties();
				}
			}
		});
	}

	/**
	 * Removes all selected properties which are not mandatory.
	 */
	private void removeSelectedProperties() {
		IStructuredSelection sel = (IStructuredSelection) propertiesTblViewer.getSelection();
		int itemRemoved = 0;
		for (Object element : sel.toArray()) {
			ConfigParamPropertyItem item = (ConfigParamPropertyItem) element;
			ConfigParameterDescription desc = item.getConfigParameterDescription();
			if (!desc.isMandatory()) {
				inputModel.removeConfigParam(item);
				++itemRemoved;
			}
		}
		if (itemRemoved > 0) {
			btnRemoveProperty.setEnabled(false);
			btnAddProperty.setEnabled(true);
			if (itemRemoved == sel.size()) { // all items were non-mandatory
				btnRestorePropertyDefault.setEnabled(false);
			}
			editor.markDirty();
			initClearAllPropertiesBtn();
		}
	}

	/**
	 * Removes all non-mandatory properties.
	 */
	private void clearAllNonMandatoryProperties() {
		IStructuredSelection sel = (IStructuredSelection) propertiesTblViewer.getSelection();
		inputModel.removeNonMandatoryConfigParams();
		btnClearAllProperties.setEnabled(false);
		btnRemoveProperty.setEnabled(false);
		btnAddProperty.setEnabled(true);
		inputModel.fireItemPropertiesChanged();
		propertiesTblViewer.setSelection(sel, false);
		boolean restoreEnabled = !propertiesTblViewer.getSelection().isEmpty();
		if (btnRestorePropertyDefault.isEnabled() != restoreEnabled) {
			btnRestorePropertyDefault.setEnabled(restoreEnabled);
		}
		editor.markDirty();
	}

}
