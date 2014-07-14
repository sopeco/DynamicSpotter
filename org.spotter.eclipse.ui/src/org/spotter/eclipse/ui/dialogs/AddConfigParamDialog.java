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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.lpe.common.config.ConfigParameterDescription;

/**
 * A dialog to add configuration parameters.
 */
public class AddConfigParamDialog extends TitleAreaDialog {

	private static final String MSG_MULTI_SELECT = "Selected %d config parameters.";

	private final ConfigParameterDescription[] configParams;
	private ConfigParameterDescription[] result;
	private List listConfigParams;
	private Label lblDescription;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell The parent shell of this dialog
	 */
	public AddConfigParamDialog(Shell parentShell, ConfigParameterDescription[] configParams) {
		super(parentShell);
		this.configParams = configParams;
		this.result = null;
		setHelpAvailable(false);
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent The parent composite the content is placed in
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Choose which non-mandatory config parameters you want to add.");
		setTitle("Add Config Parameters");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		listConfigParams = new List(container, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		listConfigParams.setItems(createListItems());
		listConfigParams.setBounds(10, 10, 309, 183);
		createListListener();

		lblDescription = new Label(container, SWT.WRAP);
		lblDescription.setBounds(325, 10, 209, 183);
		lblDescription.setText(configParams[0].getDescription());

		return area;
	}

	private void createListListener() {
		listConfigParams.setSelection(0);
		listConfigParams.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionCount = listConfigParams.getSelectionCount();
				if (selectionCount == 1) {
					int index = listConfigParams.getSelectionIndex();
					lblDescription.setText(configParams[index].getDescription());
				} else if (selectionCount > 1) {
					lblDescription.setText(String.format(MSG_MULTI_SELECT, selectionCount));
				}
			}
		});
		listConfigParams.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				int itemCount = listConfigParams.getItemCount();
				int itemHeight = listConfigParams.getItemHeight();
				if (e.y <= itemCount * itemHeight) {
					okPressed();
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
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(550, 350);
	}

	private String[] createListItems() {
		String[] items = new String[configParams.length];
		for (int i = 0; i < configParams.length; ++i) {
			items[i] = configParams[i].getName();
		}
		return items;
	}

	@Override
	protected void okPressed() {
		int selectionCount = listConfigParams.getSelectionCount();
		if (selectionCount != 0) {
			result = new ConfigParameterDescription[selectionCount];
			int[] indices = listConfigParams.getSelectionIndices();
			for (int i = 0; i < selectionCount; ++i) {
				result[i] = configParams[indices[i]];
			}
		}
		super.okPressed();
	}

	/**
	 * @return the previously selected configuration parameter descriptions if
	 *         any or 'null'
	 */
	public ConfigParameterDescription[] getResult() {
		return result;
	}

}
