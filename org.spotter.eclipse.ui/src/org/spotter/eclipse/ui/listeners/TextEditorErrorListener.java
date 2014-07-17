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

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * Cell editor listener that reacts on the error state of a text cell editor.
 * <p>
 * Decorates the cell with the provided control decoration and changes the background color of the
 * cell's text widget to red whenever the error message of the text cell editor is set and not
 * <code>null</code>.
 * </p>
 * 
 * @author Denis Knoepfle
 * 
 */
public class TextEditorErrorListener implements ICellEditorListener {

	private static final Color COLOR_WHITE = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	private static final Color COLOR_LIGHT_RED = new Color(Display.getCurrent(), new RGB(255, 150, 150));
	// Use of an empty dummy image as workaround to make the hover appear correctly next to the cell
	// otherwise it would appear at the upper left corner of the table
	private static final Image DECOR_IMG = new Image(null, 1, 1);

	private final TextCellEditor textCellEditor;
	private final Text textCtrl;
	private final ControlDecoration decor;

	/**
	 * Creates a new text editor error listener.
	 * 
	 * @param textCellEditor
	 *            the associated text cell editor
	 * @param decor
	 *            the control decoration
	 */
	public TextEditorErrorListener(TextCellEditor textCellEditor, ControlDecoration decor) {
		this.textCellEditor = textCellEditor;
		textCtrl = (Text) textCellEditor.getControl();
		this.decor = decor;
		decor.setImage(DECOR_IMG);
		decor.setShowOnlyOnFocus(true);
		decor.setShowHover(true);
		decor.hide();
		decor.hideHover();
	}

	@Override
	public void editorValueChanged(boolean oldValidState, boolean newValidState) {
		String errorMsg = textCellEditor.getErrorMessage();
		if (errorMsg != null) {
			textCtrl.setBackground(COLOR_LIGHT_RED);
			decor.setDescriptionText(errorMsg);
			decor.show();
			decor.showHoverText(errorMsg);
		} else {
			textCtrl.setBackground(COLOR_WHITE);
			decor.hide();
		}
	}

	@Override
	public void cancelEditor() {
		textCtrl.setBackground(COLOR_WHITE);
		decor.hide();
	}

	@Override
	public void applyEditorValue() {
		textCtrl.setBackground(COLOR_WHITE);
		decor.hide();
	}
}
