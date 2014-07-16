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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.model.ExtensionMetaobject;

/**
 * A dialog to add extensions.
 * 
 * @author Denis Knoepfle
 * 
 */
public class AddExtensionDialog extends TitleAreaDialog {

	private static final String MSG_MULTI_SELECT = "Selected %d extensions.";
	private static final String MSG_NO_DESCRIPTION = "No description available.";

	private final ExtensionMetaobject[] extensions;
	private ExtensionMetaobject[] result;
	private List listExtensions;
	private Text textDescription;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 *            The parent shell of this dialog
	 */
	public AddExtensionDialog(Shell parentShell, ExtensionMetaobject[] extensions) {
		super(parentShell);
		if (extensions == null) {
			this.extensions = new ExtensionMetaobject[0];
		} else {
			this.extensions = extensions;
		}
		this.result = null;
		setHelpAvailable(false);
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 *            The parent composite the content is placed in
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Choose which extensions you want to add.");
		setTitle("Add Extensions");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		listExtensions = new List(container, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		listExtensions.setItems(createListItems());
		if (listExtensions.getItemCount() > 0) {
			listExtensions.setSelection(0);
		}
		listExtensions.setBounds(10, 10, 309, 183);
		createListListener();

		textDescription = new Text(container, SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		textDescription.setBounds(325, 10, 209, 183);
		if (extensions.length > 0) {
			updateDescriptionText(extensions[0]);
		}

		return area;
	}

	private void updateDescriptionText(ExtensionMetaobject extension) {
		String projectName = extension.getProjectName();
		ServiceClientWrapper client = Activator.getDefault().getClient(projectName);
		String description = client.getExtensionDescription(extension.getExtensionName());
		textDescription.setText(description == null ? MSG_NO_DESCRIPTION : description);
	}

	private void createListListener() {
		listExtensions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionCount = listExtensions.getSelectionCount();
				getButton(Window.OK).setEnabled(selectionCount > 0);
				if (selectionCount == 0) {
					textDescription.setText("");
				} else if (selectionCount == 1) {
					int index = listExtensions.getSelectionIndex();
					updateDescriptionText(extensions[index]);
				} else if (selectionCount > 1) {
					textDescription.setText(String.format(MSG_MULTI_SELECT, selectionCount));
				}
			}
		});
		listExtensions.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				int itemCount = listExtensions.getItemCount();
				int itemHeight = listExtensions.getItemHeight();
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
		
		getButton(Window.OK).setEnabled(extensions.length > 0);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(550, 350);
	}

	private String[] createListItems() {
		String[] items = new String[extensions.length];
		for (int i = 0; i < extensions.length; ++i) {
			items[i] = extensions[i].getExtensionName();
		}
		return items;
	}

	@Override
	protected void okPressed() {
		int selectionCount = listExtensions.getSelectionCount();
		if (selectionCount != 0) {
			result = new ExtensionMetaobject[selectionCount];
			int[] indices = listExtensions.getSelectionIndices();
			for (int i = 0; i < selectionCount; ++i) {
				result[i] = extensions[indices[i]];
			}
		}
		super.okPressed();
	}

	/**
	 * @return the previously selected extensions if any or 'null'
	 */
	public ExtensionMetaobject[] getResult() {
		return result;
	}

}
