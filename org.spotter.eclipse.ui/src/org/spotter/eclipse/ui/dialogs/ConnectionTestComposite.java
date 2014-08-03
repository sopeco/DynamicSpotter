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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.listeners.ConnectionParamModifyListener;
import org.spotter.eclipse.ui.listeners.IConnectionChangedListener;

/**
 * A composite which offers functionality to test a connection to the Spotter
 * Service with configurable host and port. This is NOT a SWT Composite but
 * serves semantically as one and can be placed inside a parent composite like
 * any other widget.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ConnectionTestComposite {

	private static final String LABEL_EXPLANATION = "To create a new project it is necessary to retrieve some data from the Spotter Service. "
			+ "Therefore a valid connection is required.";
	private static final int LABEL_WIDTH_HINT = 500;

	private static final String LABEL_HOST = "Host";
	private static final String LABEL_PORT = "Port";
	private static final String LABEL_CONN_OK = "Connection OK!";
	private static final String LABEL_CONN_ERR = "No connection could be established!";

	private final ServiceClientWrapper client;
	private final List<IConnectionChangedListener> connectionListeners;
	private Composite container;
	private Text textHost;
	private Text textPort;
	private Button btnTestConn;
	private Label labelConnection;
	private String errorMessage;
	private ModifyListener modifyListener;

	/**
	 * Creates a new ConnectionTestComposite under the given parent.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param showExplanation
	 *            <code>true</code> to create an additional explanation label
	 *            above the text fields
	 * @param client
	 *            the client used to test the connection
	 */
	public ConnectionTestComposite(Composite parent, boolean showExplanation, ServiceClientWrapper client) {
		this.client = client;
		this.connectionListeners = new LinkedList<>();
		this.errorMessage = null;
		createControl(parent, showExplanation);
	}

	/**
	 * Returns the current error message.
	 * 
	 * @return the current error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sets the error message that is displayed.
	 * 
	 * @param errorMessage
	 *            the error message to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Fires a connection changed event to listeners.
	 * 
	 * @param connectionOk
	 *            <code>true</code> to signal that the connection was okay,
	 *            <code>false</code> otherwise
	 */
	public void fireConnectionChangedEvent(boolean connectionOk) {
		for (IConnectionChangedListener listener : connectionListeners) {
			listener.connectionChanged(connectionOk);
		}
	}

	/**
	 * Adds a listener to the list of notified listeners.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addConnectionChangedListener(IConnectionChangedListener listener) {
		connectionListeners.add(listener);
	}

	/**
	 * Removes the listener from the list of notified listeners.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public void removeConnectionChangedListener(IConnectionChangedListener listener) {
		connectionListeners.remove(listener);
	}

	/**
	 * Returns the control of this composite.
	 * 
	 * @return the control of this composite
	 */
	public Composite getControl() {
		return container;
	}

	/**
	 * Returns the host in the host text field.
	 * 
	 * @return the host
	 */
	public String getHost() {
		return textHost.getText();
	}

	/**
	 * Returns the port in the port text field.
	 * 
	 * @return the port
	 */
	public String getPort() {
		return textPort.getText();
	}

	/**
	 * Tests the connection.
	 * 
	 * @return <code>true</code> if the connection was successful,
	 *         <code>false</code> otherwise
	 */
	public boolean testConnection() {
		client.updateUrl(textHost.getText(), textPort.getText());

		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		Cursor waitCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT);
		Cursor oldCursor = shell.getCursor();
		shell.setCursor(waitCursor);
		boolean connectionOk = client.testConnection(false);
		shell.setCursor(oldCursor);

		labelConnection.setText(connectionOk ? LABEL_CONN_OK : LABEL_CONN_ERR);
		labelConnection.pack();
		fireConnectionChangedEvent(connectionOk);
		return connectionOk;
	}

	private void createControl(Composite parent, boolean showExplanation) {
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, true));

		if (showExplanation) {
			addExplanationLabel();
		}

		Label labelHost = new Label(container, SWT.NONE);
		labelHost.setText(LABEL_HOST);
		labelHost.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		Label labelPort = new Label(container, SWT.NONE);
		labelPort.setText(LABEL_PORT);
		labelPort.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

		textHost = new Text(container, SWT.BORDER | SWT.SINGLE);
		textHost.setText(client.getHost());
		textHost.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		textPort = new Text(container, SWT.BORDER | SWT.SINGLE);
		textPort.setText(client.getPort());
		textPort.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Composite cmpConn = new Composite(container, SWT.NONE);
		cmpConn.setLayout(new RowLayout());
		btnTestConn = new Button(cmpConn, SWT.PUSH);
		btnTestConn.setText("Test Connection");
		labelConnection = new Label(cmpConn, SWT.NONE);
		labelConnection.setText("");

		cmpConn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1));

		modifyListener = new ConnectionParamModifyListener(this, textHost, textPort, labelConnection, btnTestConn);
		textHost.addModifyListener(modifyListener);
		textPort.addModifyListener(modifyListener);
		btnTestConn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				testConnection();
			}
		});
	}

	private void addExplanationLabel() {
		Label lblExplanation = new Label(container, SWT.WRAP);
		lblExplanation.setText(LABEL_EXPLANATION);
		GridData gridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		gridData.widthHint = LABEL_WIDTH_HINT;
		lblExplanation.setLayoutData(gridData);

		Label separator = new Label(container, SWT.NONE);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
	}

}