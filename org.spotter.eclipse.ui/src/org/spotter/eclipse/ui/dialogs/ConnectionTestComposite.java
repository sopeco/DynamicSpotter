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

public class ConnectionTestComposite {

	private static final String[] LABEL_EXPLANATION = {
			"To create a new project it is necessary to retrieve some data from the Spotter Service.",
			"Therefore a valid connection is required." };
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

	public ConnectionTestComposite(Composite parent, boolean showExplanation, ServiceClientWrapper client) {
		this.client = client;
		this.connectionListeners = new LinkedList<>();
		this.errorMessage = null;
		createControl(parent, showExplanation);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void fireConnectionChangedEvent(boolean connectionOk) {
		for (IConnectionChangedListener listener : connectionListeners) {
			listener.connectionChanged(connectionOk);
		}
	}

	public void addConnectionChangedListener(IConnectionChangedListener listener) {
		connectionListeners.add(listener);
	}

	public void removeConnectionChangedListener(IConnectionChangedListener listener) {
		connectionListeners.remove(listener);
	}

	public Composite getControl() {
		return container;
	}

	public String getHost() {
		return textHost.getText();
	}

	public String getPort() {
		return textPort.getText();
	}

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
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);

		if (showExplanation) {
			Label lblExplanationBegin = new Label(container, SWT.NONE);
			lblExplanationBegin.setText(LABEL_EXPLANATION[0]);
			Label lblExplanationEnd = new Label(container, SWT.NONE);
			lblExplanationEnd.setText(LABEL_EXPLANATION[1]);
			Label separator = new Label(container, SWT.NONE);
			separator.setText(" ");

			GridData gdExplanation = new GridData();
			gdExplanation.horizontalSpan = 2;
			gdExplanation.horizontalAlignment = SWT.LEFT;
			gdExplanation.verticalAlignment = SWT.TOP;
			lblExplanationBegin.setLayoutData(gdExplanation);
			gdExplanation = new GridData();
			gdExplanation.horizontalSpan = 2;
			gdExplanation.horizontalAlignment = SWT.LEFT;
			gdExplanation.verticalAlignment = SWT.TOP;
			lblExplanationEnd.setLayoutData(gdExplanation);

			GridData gdSeparator = new GridData(GridData.FILL_HORIZONTAL);
			gdSeparator.horizontalSpan = 2;
			gdSeparator.grabExcessHorizontalSpace = true;
			separator.setLayoutData(gdSeparator);
		}

		Label labelHost = new Label(container, SWT.NONE);
		labelHost.setText(LABEL_HOST);
		Label labelPort = new Label(container, SWT.NONE);
		labelPort.setText(LABEL_PORT);

		textHost = new Text(container, SWT.BORDER | SWT.SINGLE);
		textHost.setText(client.getHost());
		textHost.setSize(150, 21);
		textPort = new Text(container, SWT.BORDER | SWT.SINGLE);
		textPort.setText(client.getPort());
		textPort.setSize(150, 21);

		Composite cmpConn = new Composite(container, SWT.NONE);
		cmpConn.setLayout(new RowLayout());
		btnTestConn = new Button(cmpConn, SWT.PUSH);
		btnTestConn.setText("Test Connection");
		labelConnection = new Label(cmpConn, SWT.NONE);
		labelConnection.setText("");

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		textHost.setLayoutData(gd);
		textPort.setLayoutData(gd);

		GridData gdCmp = new GridData(GridData.FILL_BOTH);
		gdCmp.horizontalSpan = 2;
		gdCmp.grabExcessVerticalSpace = true;
		gdCmp.grabExcessHorizontalSpace = true;
		cmpConn.setLayoutData(gdCmp);

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

}