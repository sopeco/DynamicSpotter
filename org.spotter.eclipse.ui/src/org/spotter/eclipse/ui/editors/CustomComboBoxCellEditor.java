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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A cell editor that presents it's items in a native combo box instead of the
 * customized <code>CCombo</code>.
 * <p>
 * Thus there is no unnecessary white space shown as in the CCombo variant. Also
 * clicking on an item in the list with the mouse automatically deactivates the
 * editor.
 * </p>
 * 
 * @author Denis Knoepfle
 * 
 */
public class CustomComboBoxCellEditor extends ComboBoxCellEditor {

	private String[] items;
	private int selection;
	private boolean precedingTraversalEvent = false;

	/**
	 * The editor's control (using the native combo box)
	 */
	private Combo comboBox;
	private int activationStyle = SWT.NONE;

	private static final int defaultStyle = SWT.NONE;
	private static final int CONTROL_MINIMUM_WIDTH = 50;

	/**
	 * Creates an empty combo without parent.
	 */
	public CustomComboBoxCellEditor() {
		setStyle(defaultStyle);
	}

	/**
	 * Creates a new cell editor with a combo box parented under the given
	 * control containing the given list of items.
	 * 
	 * @param parent
	 *            the parent control
	 * @param items
	 *            the list of strings for the combo box
	 */
	public CustomComboBoxCellEditor(Composite parent, String[] items) {
		super(parent, items, defaultStyle);
	}

	/**
	 * Creates a new cell editor with a combo box parented under the given
	 * control containing the given list of items.
	 * 
	 * @param parent
	 *            the parent control
	 * @param items
	 *            the strings for the combo box
	 * @param style
	 *            the style bits
	 */
	public CustomComboBoxCellEditor(Composite parent, String[] items, int style) {
		setStyle(style);
		create(parent);
		setItems(items);
	}

	/**
	 * @return the items of the combo box
	 */
	@Override
	public String[] getItems() {
		return this.items;
	}

	/**
	 * Sets the items in the combo box.
	 * 
	 * @param items
	 *            the items to set for the combo box
	 */
	@Override
	public void setItems(String[] items) {
		Assert.isNotNull(items);
		this.items = items;
		populateComboBox();
	}

	@Override
	public void setActivationStyle(int activationStyle) {
		this.activationStyle = activationStyle;
	}

	@Override
	public void activate(ColumnViewerEditorActivationEvent activationEvent) {
		super.activate(activationEvent);
		if (activationStyle != SWT.NONE) {

			boolean mouseSelection = activationEvent.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
					|| activationEvent.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION;
			boolean keyboardSelection = activationEvent.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED;
			boolean programmatic = activationEvent.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			boolean traversal = activationEvent.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL;

			if (mouseSelection && (activationStyle & DROP_DOWN_ON_MOUSE_ACTIVATION) != 0 || keyboardSelection
					&& (activationStyle & DROP_DOWN_ON_KEY_ACTIVATION) != 0 || programmatic
					&& (activationStyle & DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION) != 0 || traversal
					&& (activationStyle & DROP_DOWN_ON_TRAVERSE_ACTIVATION) != 0) {

				comboBox.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						comboBox.setListVisible(true);
					}
				});

			}
		}
	}

	@Override
	protected Control createControl(Composite parent) {
		comboBox = new Combo(parent, getStyle());
		comboBox.setFont(parent.getFont());

		populateComboBox();

		comboBox.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				keyReleaseOccured(e);
			}
		});

		comboBox.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN || e.detail == SWT.TRAVERSE_ESCAPE) {
					e.doit = false;
				} else {
					precedingTraversalEvent = true;
				}
			}
		});

		comboBox.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				applyAndDeactivate();
			}

			public void widgetSelected(SelectionEvent event) {
				// when there was a preceding traversal event by keyboard only
				// save the selection, don't apply yet
				if (precedingTraversalEvent) {
					selection = comboBox.getSelectionIndex();
					precedingTraversalEvent = false;
				} else {
					// assume the selection was done by mouse click
					applyAndDeactivate();
				}
			}
		});

		comboBox.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				CustomComboBoxCellEditor.this.focusLost();
			}
		});
		return comboBox;
	}

	/**
	 * Returns the zero-based index of the current selection.
	 * 
	 * @return the zero-based index of the current selection wrapped as an
	 *         <code>Integer</code>
	 */
	protected Object doGetValue() {
		return new Integer(selection);
	}

	@Override
	protected void doSetFocus() {
		comboBox.setFocus();
	}

	@Override
	public LayoutData getLayoutData() {
		LayoutData layoutData = super.getLayoutData();
		if ((comboBox == null) || comboBox.isDisposed()) {
			layoutData.minimumWidth = CONTROL_MINIMUM_WIDTH;
		}
		return layoutData;
	}

	/**
	 * Sets the value by accepting a zero-based index of a selection.
	 * 
	 * @param value
	 *            the zero-based index of the selection wrapped as an
	 *            <code>Integer</code>
	 */
	@Override
	protected void doSetValue(Object value) {
		Assert.isTrue(comboBox != null && (value instanceof Integer));
		selection = ((Integer) value).intValue();
		comboBox.select(selection);
	}

	/**
	 * Populates the combo box with the items.
	 */
	private void populateComboBox() {
		if (comboBox != null && items != null) {
			comboBox.removeAll();
			for (int i = 0; i < items.length; i++) {
				comboBox.add(items[i], i);
			}

			setValueValid(true);
			selection = 0;
		}
	}

	/**
	 * Applies the currently selected value and deactivates the cell editor.
	 */
	private void applyAndDeactivate() {
		selection = comboBox.getSelectionIndex();

		Object newValue = doGetValue();
		markDirty();
		boolean isValid = isCorrect(newValue);
		setValueValid(isValid);

		if (!isValid) {
			// only format if the selection index is valid
			if (items.length > 0 && selection >= 0 && selection < items.length) {
				// try to insert the current value into the error message.
				setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { items[selection] }));
			} else {
				// Since we don't have a valid index, assume we're using an
				// editable combo so format using its text value
				setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { comboBox.getText() }));
			}
		}

		fireApplyEditorValue();
		deactivate();
	}

	@Override
	protected void focusLost() {
		if (isActivated()) {
			applyAndDeactivate();
		}
	}

	@Override
	protected void keyReleaseOccured(KeyEvent keyEvent) {
		if (keyEvent.keyCode == SWT.ESC) {
			fireCancelEditor();
		} else if (keyEvent.character == SWT.CR || keyEvent.keyCode == SWT.TAB) {
			applyAndDeactivate();
		}
	}
}
