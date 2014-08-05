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
package org.spotter.eclipse.ui.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.dialogs.ConnectionTestComposite;
import org.spotter.eclipse.ui.listeners.IConnectionChangedListener;

/**
 * A wizard page for the creation of a new project providing a
 * <code>ConnectionTestComposite</code> to test Spotter service connectivity.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ConnectionWizardPage extends WizardPage implements IConnectionChangedListener {

	public static final String TITLE = "Connection Settings for DynamicSpotter Service";
	public static final String DESCRIPTION = "Define the connection settings to the DynamicSpotter service.";

	private ConnectionTestComposite connTestComposite;

	/**
	 * Create wizard page with a title and a description.
	 */
	public ConnectionWizardPage() {
		super(TITLE);
		setTitle(TITLE);
		setDescription(DESCRIPTION);
	}

	@Override
	public void createControl(Composite parent) {
		connTestComposite = new ConnectionTestComposite(parent, true, new ServiceClientWrapper());
		connTestComposite.addConnectionChangedListener(this);
		connTestComposite.testConnection();

		setControl(connTestComposite.getControl());
	}

	/**
	 * Returns the host.
	 * 
	 * @return The host
	 */
	public String getHost() {
		return connTestComposite.getHost();
	}

	/**
	 * Returns the port.
	 * 
	 * @return The port
	 */
	public String getPort() {
		return connTestComposite.getPort();
	}

	@Override
	public void connectionChanged(boolean connectionOk) {
		setErrorMessage(connTestComposite.getErrorMessage());
		setPageComplete(connectionOk);
	}

	/**
	 * Test the connection with the currently configured host and port. The test
	 * will also force an update of the wizard buttons.
	 * 
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	public boolean testConnection() {
		boolean connectionOk = connTestComposite.testConnection();

		// force update of buttons when page is not active
		if (!isCurrentPage()) {
			getContainer().updateButtons();
		}

		return connectionOk;
	}

}
