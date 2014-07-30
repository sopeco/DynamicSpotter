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
package org.spotter.eclipse.ui.listeners;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.spotter.eclipse.ui.dialogs.ConnectionTestComposite;

/**
 * A modify listener that validates fields in the ConnectionTestComposite.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ConnectionParamModifyListener implements ModifyListener {

	private static final int MIN_PORT = 0;
	private static final int MAX_PORT = 65535;

	private final ConnectionTestComposite connComposite;
	private final Text textHost;
	private final Text textPort;
	private final Label lblConnection;
	private final Button btnTestConn;

	/**
	 * Create a new listener for the given connection composite.
	 * 
	 * @param connComposite
	 *            the connection composite
	 * @param textHost
	 *            the text field of the host
	 * @param textPort
	 *            the text field of the port
	 * @param lblConnection
	 *            the connection label that should be updated
	 * @param btnTestConn
	 *            the test connection button that should be updated
	 */
	public ConnectionParamModifyListener(ConnectionTestComposite connComposite, Text textHost, Text textPort,
			Label lblConnection, Button btnTestConn) {
		this.connComposite = connComposite;
		this.textHost = textHost;
		this.textPort = textPort;
		this.lblConnection = lblConnection;
		this.btnTestConn = btnTestConn;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		boolean hostEmpty = textHost.getText().isEmpty();
		boolean portEmpty = textPort.getText().isEmpty();
		boolean validPort = isValidPort();

		if (hostEmpty) {
			connComposite.setErrorMessage("Host must not be empty!");
		} else if (portEmpty) {
			connComposite.setErrorMessage("Port must not be empty!");
		} else if (!validPort) {
			connComposite.setErrorMessage("Port must be a number between " + MIN_PORT + " and " + MAX_PORT);
		} else if (connComposite.getErrorMessage() != null) {
			connComposite.setErrorMessage(null);
		}

		lblConnection.setText("");
		lblConnection.pack();
		btnTestConn.setEnabled(!hostEmpty && !portEmpty && validPort);
		// require that the user tests the connection first
		connComposite.fireConnectionChangedEvent(false);
	}

	private boolean isValidPort() {
		try {
			int port = Integer.parseInt(textPort.getText());
			return port >= MIN_PORT && port <= MAX_PORT;
		} catch (NumberFormatException exception) {
			return false;
		}
	}
}