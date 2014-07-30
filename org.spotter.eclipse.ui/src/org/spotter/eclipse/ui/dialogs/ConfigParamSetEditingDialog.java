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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.lpe.common.config.ConfigParameterDescription;

/**
 * A dialog to edit a configuration parameter set.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ConfigParamSetEditingDialog extends TitleAreaDialog {

	private String result;
	private final ConfigParameterDescription desc;
	private final Set<String> configuredElements;
	private List listConfiguredElements;
	private Label lblDescription;
	private Text textNewItem;
	private Button btnOk;
	private Button btnAdd;
	private Button btnRemove;
	private Button btnSelectAll;
	private Button btnSelectNone;
	private CheckboxTableViewer checkConfElemsTblViewer;
	private Table tblConfiguredElements;

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
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Add or remove elements to or from the set.");
		setTitle("Edit Config Parameter Set");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		lblDescription = new Label(container, SWT.WRAP);
		lblDescription.setBounds(325, 10, 209, 183);
		lblDescription.setText(desc.getDescription());

		if (desc.optionsAvailable()) {
			createAreaWithPredefinedOptions(container);
		} else {
			createAreaWithoutPredefinedOptions(container);
		}

		return area;
	}

	private void createAreaWithoutPredefinedOptions(Composite container) {
		listConfiguredElements = new List(container, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		listConfiguredElements.setItems(configuredElementsToArray());
		listConfiguredElements.setBounds(10, 10, 150, 183);

		textNewItem = new Text(container, SWT.BORDER);
		textNewItem.setBounds(170, 10, 145, 21);
		textNewItem.setFocus();

		btnAdd = new Button(container, SWT.NONE);
		btnAdd.setToolTipText("Add the new item to the list.");
		btnAdd.setBounds(245, 37, 70, 25);
		btnAdd.setText("Add");
		btnAdd.setEnabled(false);

		btnRemove = new Button(container, SWT.NONE);
		btnRemove.setToolTipText("Remove the selected item from the list.");
		btnRemove.setBounds(170, 37, 70, 25);
		btnRemove.setText("Remove");
		btnRemove.setEnabled(false);

		createButtonActionsWithoutPredefinedOptions();
		createListListener();
		createTextListener();
	}

	private void createAreaWithPredefinedOptions(Composite container) {
		checkConfElemsTblViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL);
		checkConfElemsTblViewer.setContentProvider(new ArrayContentProvider());
		tblConfiguredElements = checkConfElemsTblViewer.getTable();
		tblConfiguredElements.setBounds(10, 10, 225, 183);
		checkConfElemsTblViewer.setInput(desc.getOptions().toArray(new String[desc.getOptions().size()]));
		checkConfElemsTblViewer.setCheckedElements(configuredElementsToArray());

		btnSelectAll = new Button(container, SWT.NONE);
		btnSelectAll.setToolTipText("Select all items.");
		btnSelectAll.setBounds(240, 40, 75, 25);
		btnSelectAll.setText("Select All");

		btnSelectNone = new Button(container, SWT.NONE);
		btnSelectNone.setToolTipText("Deselect all items.");
		btnSelectNone.setBounds(240, 70, 75, 25);
		btnSelectNone.setText("Select None");

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
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnOk = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(550, 350);
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
