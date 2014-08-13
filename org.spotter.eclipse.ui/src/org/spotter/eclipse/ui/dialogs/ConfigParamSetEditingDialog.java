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
package org.spotter.eclipse.ui.dialogs;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.util.WidgetUtils;

/**
 * A dialog to edit a configuration parameter set.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ConfigParamSetEditingDialog extends TitleAreaDialog {

	private static final int ITEM_LIST_HEIGHT_HINT = 150;
	private static final int DESCRIPTION_WIDTH_HINT = 150;

	private String result;
	private final ConfigParameterDescription desc;
	private final Set<String> configuredElements;
	private List listConfiguredElements;
	private Text textNewItem;
	private Button btnOk;
	private Button btnAdd;
	private Button btnRemove;
	private Button btnSelectAll;
	private Button btnSelectNone;
	private CheckboxTableViewer checkConfElemsTblViewer;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 *            The parent of this shell
	 * @param desc
	 *            The configuration parameter description to get the data from
	 * @param csvString
	 *            The comma separated values for the set in a string
	 */
	public ConfigParamSetEditingDialog(Shell parentShell, ConfigParameterDescription desc, String csvString) {
		super(parentShell);
		this.result = "";
		this.desc = desc;

		this.configuredElements = new HashSet<String>();
		if (csvString != null && !csvString.isEmpty()) {
			String[] elements = csvString.split(ConfigParameterDescription.LIST_VALUE_SEPARATOR);
			for (String element : elements) {
				if (!element.isEmpty()) {
					this.configuredElements.add(element);
				}
			}
		}
		setHelpAvailable(false);
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 *            parent composite
	 * @return a {@link Control} instance
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Add or remove elements to or from the set.");
		setTitle("Edit Config Parameter Set");

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final int columnsCount = 3;
		container.setLayout(WidgetUtils.createGridLayout(columnsCount));

		if (desc.optionsAvailable()) {
			createAreaWithPredefinedOptions(container);
		} else {
			createAreaWithoutPredefinedOptions(container);
		}

		Text textDescription = new Text(container, SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		textDescription.setText(desc.getDescription());
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, true);
		gridData.widthHint = DESCRIPTION_WIDTH_HINT;
		textDescription.setLayoutData(gridData);

		return area;
	}

	private void createAreaWithoutPredefinedOptions(Composite container) {
		listConfiguredElements = new List(container, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		listConfiguredElements.setItems(configuredElementsToArray());
		GridData listGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		listGridData.heightHint = ITEM_LIST_HEIGHT_HINT;
		listConfiguredElements.setLayoutData(listGridData);

		Composite controlComp = new Composite(container, SWT.NONE);
		controlComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		controlComp.setLayout(new GridLayout(2, true));

		textNewItem = new Text(controlComp, SWT.BORDER);
		textNewItem.setFocus();
		GridData textGridData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		textNewItem.setLayoutData(textGridData);

		btnRemove = new Button(controlComp, SWT.NONE);
		btnRemove.setToolTipText("Remove the selected item from the list.");
		btnRemove.setText("Remove");
		btnRemove.setEnabled(false);
		btnRemove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		btnAdd = new Button(controlComp, SWT.NONE);
		btnAdd.setToolTipText("Add the new item to the list.");
		btnAdd.setText("Add");
		btnAdd.setEnabled(false);
		btnAdd.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		createButtonActionsWithoutPredefinedOptions();
		createListListener();
		createTextListener();
	}

	private void createAreaWithPredefinedOptions(Composite container) {
		Composite tableComp = new Composite(container, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = ITEM_LIST_HEIGHT_HINT;
		tableComp.setLayoutData(gridData);
		TableColumnLayout tableColLayout = new TableColumnLayout();
		tableComp.setLayout(tableColLayout);

		checkConfElemsTblViewer = CheckboxTableViewer.newCheckList(tableComp, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL);

		TableViewerColumn tableColumn = new TableViewerColumn(checkConfElemsTblViewer, SWT.NONE);
		tableColLayout.setColumnData(tableColumn.getColumn(), new ColumnWeightData(1));

		checkConfElemsTblViewer.setContentProvider(new ArrayContentProvider());
		checkConfElemsTblViewer.setLabelProvider(new LabelProvider());
		checkConfElemsTblViewer.setInput(desc.getOptions().toArray(new String[desc.getOptions().size()]));
		checkConfElemsTblViewer.setCheckedElements(configuredElementsToArray());

		Composite controlComp = new Composite(container, SWT.NONE);
		controlComp.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		controlComp.setLayout(new GridLayout());

		btnSelectAll = new Button(controlComp, SWT.NONE);
		btnSelectAll.setToolTipText("Select all items.");
		btnSelectAll.setText("Select All");
		btnSelectAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		btnSelectNone = new Button(controlComp, SWT.NONE);
		btnSelectNone.setToolTipText("Deselect all items.");
		btnSelectNone.setText("Select None");
		btnSelectNone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		createButtonActionsWithPredefinedOptions();
		createTableListener();
	}

	private void createButtonActionsWithoutPredefinedOptions() {
		btnAdd.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String newItem = textNewItem.getText();
				if (!configuredElements.contains(newItem)) {
					String[] selected = listConfiguredElements.getSelection();
					configuredElements.add(newItem);
					listConfiguredElements.setItems(configuredElementsToArray());
					if (selected.length > 0) {
						listConfiguredElements.setSelection(selected);
					}
					btnAdd.setEnabled(false);
					getShell().setDefaultButton(btnOk);
				}
				textNewItem.selectAll();
				textNewItem.setFocus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] selected = listConfiguredElements.getSelection();
				if (selected.length > 0) {
					for (String item : selected) {
						configuredElements.remove(item);
					}
					listConfiguredElements.setItems(configuredElementsToArray());
					listConfiguredElements.setFocus();
					btnRemove.setEnabled(false);
					String text = textNewItem.getText();
					if (!text.isEmpty() && !configuredElements.contains(text) && !btnAdd.isEnabled()) {
						btnAdd.setEnabled(true);
						getShell().setDefaultButton(btnAdd);
					}
				}
			}
		});
	}

	private void createButtonActionsWithPredefinedOptions() {
		btnSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkConfElemsTblViewer.setAllChecked(true);
			}
		});
		btnSelectNone.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkConfElemsTblViewer.setAllChecked(false);
			}
		});
	}

	private void createTableListener() {
		final Table tblConfiguredElements = checkConfElemsTblViewer.getTable();
		tblConfiguredElements.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (tblConfiguredElements.getSelectionCount() > 0) {
					TableItem item = tblConfiguredElements.getSelection()[0];
					if (item.getBounds(0).contains(new Point(e.x, e.y))) {
						Object data = item.getData();
						boolean oldState = checkConfElemsTblViewer.getChecked(data);
						checkConfElemsTblViewer.setChecked(data, !oldState);
					}
				}
			}
		});
	}

	private void createListListener() {
		listConfiguredElements.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = listConfiguredElements.getSelectionIndex();
				if (index != -1) {
					btnRemove.setEnabled(true);
				} else if (btnRemove.isEnabled()) {
					btnRemove.setEnabled(false);
				}
			}
		});
	}

	private void createTextListener() {
		textNewItem.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String text = textNewItem.getText();
				int index = text.indexOf(ConfigParameterDescription.LIST_VALUE_SEPARATOR);
				if (index != -1) {
					text = text.substring(0, index) + text.substring(index + 1, text.length());
					textNewItem.setText(text);
				}
				if ((text.isEmpty() || configuredElements.contains(text)) && btnAdd.isEnabled()) {
					btnAdd.setEnabled(false);
					getShell().setDefaultButton(btnOk);
				} else if (!btnAdd.isEnabled() && !text.isEmpty() && !configuredElements.contains(text)) {
					btnAdd.setEnabled(true);
					getShell().setDefaultButton(btnAdd);
				}
			}
		});
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 *            parent composite
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnOk = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * @return the configured elements as string array
	 */
	private String[] configuredElementsToArray() {
		return configuredElements.toArray(new String[configuredElements.size()]);
	}

	@Override
	protected void okPressed() {
		StringBuilder sb = new StringBuilder();

		String[] items = retrieveItems();

		if (items.length > 0) {
			sb.append(items[0]);
		}
		for (int i = 1; i < items.length; ++i) {
			sb.append(ConfigParameterDescription.LIST_VALUE_SEPARATOR + items[i]);
		}
		result = sb.toString();
		super.okPressed();
	}

	private String[] retrieveItems() {
		if (desc.optionsAvailable()) {
			Object[] checkedItems = checkConfElemsTblViewer.getCheckedElements();
			if (checkedItems == null || checkedItems.length == 0) {
				return new String[0];
			}
			String[] items = new String[checkedItems.length];
			for (int i = 0; i < checkedItems.length; ++i) {
				items[i] = checkedItems[i].toString();
			}
			return items;
		} else {
			return listConfiguredElements.getItems();
		}
	}

	/**
	 * @return the configured elements as a string
	 */
	public String getResult() {
		return result;
	}
}
