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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.listeners.IConnectionChangedListener;
import org.spotter.eclipse.ui.wizard.ConnectionWizardPage;

/**
 * A dialog to edit the service client settings of a DynamicSpotter project.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ServiceClientSettingsDialog extends TitleAreaDialog implements IConnectionChangedListener {

	private final ServiceClientWrapper client;
	private ConnectionTestComposite connTestComposite;

	/**
	 * Creates a new dialog for the given project.
	 * 
	 * @param parentShell
	 *            The underlying shell for this dialog
	 * @param projectName
	 *            The name of the project whose settings will be edited
	 */
	public ServiceClientSettingsDialog(Shell parentShell, String projectName) {
		super(parentShell);
		this.client = Activator.getDefault().getClient(projectName);
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
		setTitle(ConnectionWizardPage.TITLE);
		setMessage(ConnectionWizardPage.DESCRIPTION);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new FillLayout());

		connTestComposite = new ConnectionTestComposite(container, false, client);
		connTestComposite.addConnectionChangedListener(this);

		return area;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 *            The parent composite the content is placed in
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		String host = connTestComposite.getHost();
		String port = connTestComposite.getPort();
		if (!client.saveServiceClientSettings(host, port)) {
			return;
		}
		super.okPressed();
	}

	@Override
	public void connectionChanged(boolean connectionOk) {
		String errorMsg = connTestComposite.getErrorMessage();
		setErrorMessage(errorMsg);
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setEnabled(errorMsg == null);
		}
	}

}
