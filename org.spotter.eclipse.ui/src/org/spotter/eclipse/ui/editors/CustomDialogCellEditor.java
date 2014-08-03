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
package org.spotter.eclipse.ui.editors;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.dialogs.ConfigParamSetEditingDialog;

/**
 * A custom dialog cell editor used by <code>PropertiesEditingSupport</code>. No
 * SWT control is used by this editor to open the dialog, editing starts
 * directly when the cell editor gets activated.
 * 
 * @author Denis Knoepfle
 * 
 */
public class CustomDialogCellEditor extends CellEditor {

	/**
	 * Default CustomDialogCellEditor style
	 */
	private static final int defaultStyle = SWT.NONE;

	/**
	 * The corresponding configuration parameter description.
	 */
	private ConfigParameterDescription configParamDesc;

	/**
	 * The editor control.
	 */
	private Composite editor;

	/**
	 * The label widget that gets reused by <code>updateLabel</code>.
	 */
	private Label defaultLabel;

	/**
	 * The value of this cell editor; initially <code>null</code>.
	 */
	private Object value = null;

	/**
	 * Creates a new dialog cell editor with no control.
	 */
	public CustomDialogCellEditor() {
		setStyle(defaultStyle);
	}

	/**
	 * Creates a new custom dialog cell editor parented under the given control.
	 * The cell editor value is <code>null</code> initially, and has no
	 * validator.
	 * 
	 * @param parent
	 *            the parent control
	 */
	public CustomDialogCellEditor(Composite parent) {
		this(parent, defaultStyle);
	}

	/**
	 * Creates a new custom dialog cell editor parented under the given control.
	 * The cell editor value is <code>null</code> initially, and has no
	 * validator.
	 * 
	 * @param parent
	 *            the parent control
	 * @param style
	 *            the style bits
	 */
	public CustomDialogCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Sets the corresponding configuration parameter description.
	 * 
	 * @param configParamDesc
	 *            The configuration parameter description.
	 */
	public void setConfigParameterDescription(ConfigParameterDescription configParamDesc) {
		this.configParamDesc = configParamDesc;
	}

	/**
	 * This will directly start editing the cell. Needs to be executed as async
	 * Runnable because of the structure of the framework and
	 * <code>activate()</code> must return first before <code>editValue()</code>
	 * can be called.
	 */
	@Override
	public void activate() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				editValue();
			}
		});
	}

	/**
	 * Creates the controls used to show the value of this cell editor.
	 * 
	 * @param cell
	 *            the control for this cell editor
	 * @return the underlying control
	 */
	protected Control createContents(Composite cell) {
		defaultLabel = new Label(cell, SWT.LEFT);
		defaultLabel.setFont(cell.getFont());
		defaultLabel.setBackground(cell.getBackground());
		return defaultLabel;
	}

	/**
	 * Creates a composite as this cell editor's control.
	 * 
	 * @return the new control
	 */
	protected Control createControl(Composite parent) {
		Font font = parent.getFont();
		Color bgColor = parent.getBackground();

		editor = new Composite(parent, getStyle());
		editor.setFont(font);
		editor.setBackground(bgColor);
		editor.setLayout(new FillLayout());

		createContents(editor);
		updateContents(value);

		setValueValid(true);

		return editor;
	}

	/**
	 * Returns this cell editor's value.
	 * 
	 * @return the value of this cell editor
	 */
	@Override
	protected Object doGetValue() {
		return value;
	}

	/**
	 * Sets the focus to the cell editor's default label.
	 */
	@Override
	protected void doSetFocus() {
		defaultLabel.setFocus();
	}

	/**
	 * Sets this cell editor's value and updates the contents.
	 * 
	 * @param value
	 *            the value of this cell editor
	 */
	protected void doSetValue(Object value) {
		this.value = value;
		updateContents(value);
	}

	/**
	 * Opens a dialog box under the given parent control and returns the
	 * dialog's value when it closes, or <code>null</code> if the dialog was
	 * canceled or no selection was made in the dialog.
	 * 
	 * @param cellEditorWindow
	 *            the parent control cell editor's window
	 * @return the selected value, or <code>null</code> if the dialog was
	 *         canceled or no selection was made in the dialog
	 */
	private Object openDialogBox(Control cellEditorWindow) {
		if (configParamDesc == null) {
			return null;
		}

		String result = null;
		String oldValue = (String) value;
		boolean needCharConversion = false;
		Shell shell = new Shell(Display.getDefault());

		if (configParamDesc.isADirectory()) {
			result = openDirectoryDialog(shell, oldValue);
			needCharConversion = result != null;
		} else if (configParamDesc.isAFile()) {
			result = openFileDialog(shell, oldValue);
			needCharConversion = result != null;
		} else if (configParamDesc.isASet()) {
			ConfigParamSetEditingDialog dialog = new ConfigParamSetEditingDialog(shell, configParamDesc,
					defaultLabel.getText());
			if (dialog.open() == Window.OK) {
				result = dialog.getResult();
			}
		}
		return needCharConversion ? ((String) result).replace('\\', '/') : result;
	}

	private String openDirectoryDialog(Shell shell, String oldValue) {
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog.setFilterPath(oldValue);
		dialog.setText("Edit " + configParamDesc.getName());
		dialog.setMessage("Choose a directory for '" + configParamDesc.getName() + "':");

		return dialog.open();
	}

	private String openFileDialog(Shell shell, String oldValue) {
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);

		String defaultFileName = configParamDesc.getDefaultFileName();
		boolean useOldValue = oldValue != null && !oldValue.isEmpty();

		dialog.setFileName(useOldValue ? oldValue : defaultFileName);

		dialog.setText("Edit " + configParamDesc.getName());
		dialog.setFilterExtensions(configParamDesc.getFileExtensions());

		return dialog.open();
	}

	/**
	 * Lets the user edit the value via a custom dialog.
	 */
	private void editValue() {
		Control previousFocusControl = Display.getCurrent().getFocusControl();
		Object newValue = openDialogBox(editor);
		if (previousFocusControl != null && !previousFocusControl.isFocusControl()) {
			previousFocusControl.setFocus();
		}

		if (newValue != null) {
			boolean newValidState = isCorrect(newValue);
			if (newValidState) {
				markDirty();
				doSetValue(newValue);
			} else {
				// try to insert the current value into the error message.
				setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { newValue.toString() }));
			}
			fireApplyEditorValue();
		} else {
			fireCancelEditor();
		}
	}

	/**
	 * Updates the controls showing the value of this cell editor.
	 * 
	 * @param value
	 *            the new value of this cell editor
	 */
	protected void updateContents(Object value) {
		if (defaultLabel == null) {
			return;
		}

		String text = ""; //$NON-NLS-1$
		if (value != null) {
			text = value.toString();
		}
		defaultLabel.setText(text);
	}
}
