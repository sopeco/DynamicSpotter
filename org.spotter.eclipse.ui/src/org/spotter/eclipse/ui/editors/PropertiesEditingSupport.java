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

import java.util.Set;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.eclipse.ui.listeners.TextEditorErrorListener;
import org.spotter.eclipse.ui.model.AbstractPropertyItem;
import org.spotter.eclipse.ui.model.ExtensionItem;
import org.spotter.eclipse.ui.viewers.PropertiesGroupViewer;

/**
 * A class implementing editing support for properties. The cell editor returned
 * by this editing support depends on the type of the property in the cell.
 * 
 * @author Denis Knoepfle
 * 
 */
public final class PropertiesEditingSupport extends EditingSupport {

	private static final String[] BOOLEAN_VALUES = { Boolean.TRUE.toString(), Boolean.FALSE.toString() };

	private AbstractSpotterEditor editor;
	private PropertiesGroupViewer propertiesViewer;
	// default text editor
	private TextCellEditor cellDefaultTextEditor;
	// editor for numbers
	private TextCellEditor cellNumberEditor;
	// editor for booleans
	private ComboBoxCellEditor cellBooleanEditor;
	// editor for parameters that are no sets but have options available
	private ComboBoxCellEditor cellComboBoxEditor;
	// custom editor for special purposes like directories, files and sets
	private CustomDialogCellEditor cellCustomDialogEditor;

	/**
	 * Creates an editing support for the given operating viewer.
	 * 
	 * @param operatingViewer
	 *            the viewer this editing support works for
	 * @param editor
	 *            the editor which is operated in
	 * @param propertiesViewer
	 *            the properties group viewer that contains the operating viewer
	 */
	public PropertiesEditingSupport(ColumnViewer operatingViewer, AbstractSpotterEditor editor,
			PropertiesGroupViewer propertiesViewer) {
		super(operatingViewer);
		this.editor = editor;
		this.propertiesViewer = propertiesViewer;
		Composite parent = (Composite) getViewer().getControl();
		cellDefaultTextEditor = new TextCellEditor(parent);

		cellNumberEditor = new TextCellEditor(parent);
		ControlDecoration decor = new ControlDecoration(cellNumberEditor.getControl(), SWT.LEFT | SWT.TOP);
		cellNumberEditor.addListener(new TextEditorErrorListener(cellNumberEditor, decor));

		cellBooleanEditor = new ComboBoxCellEditor(parent, BOOLEAN_VALUES, SWT.DROP_DOWN | SWT.READ_ONLY);
		cellBooleanEditor.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION
				| ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION);
		cellComboBoxEditor = new ComboBoxCellEditor(parent, new String[0], SWT.DROP_DOWN | SWT.READ_ONLY);

		cellCustomDialogEditor = new CustomDialogCellEditor(parent);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		CellEditor cellEditor = null;
		if (element instanceof AbstractPropertyItem) {
			AbstractPropertyItem tableItem = (AbstractPropertyItem) element;
			ConfigParameterDescription desc = tableItem.getConfigParameterDescription();
			LpeSupportedTypes type = desc.getType();
			String lowerBoundary = desc.getLowerBoundary();
			String upperBoundary = desc.getUpperBoundary();
			switch (type) {
			case Integer:
			case Long:
			case Float:
			case Double:
				cellNumberEditor.setValidator(new NumberValidator(type, lowerBoundary, upperBoundary));
				cellEditor = cellNumberEditor;
				break;
			case Boolean:
				cellEditor = cellBooleanEditor;
				break;
			case String:
				if (desc.isASet() || desc.isDirectory() || desc.isAFile()) {
					cellCustomDialogEditor.setConfigParameterDescription(desc);
					cellEditor = cellCustomDialogEditor;
				} else if (desc.optionsAvailable()) {
					Set<String> availableOptions = desc.getOptions();
					cellComboBoxEditor.setItems(availableOptions.toArray(new String[availableOptions.size()]));
					cellEditor = cellComboBoxEditor;
				} else {
					cellEditor = cellDefaultTextEditor;
				}
				break;
			default:
				cellEditor = cellDefaultTextEditor;
				break;
			}
		}
		return cellEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof AbstractPropertyItem) {
			AbstractPropertyItem tableItem = (AbstractPropertyItem) element;
			String value = tableItem.getValue();
			ConfigParameterDescription desc = tableItem.getConfigParameterDescription();
			if (desc.getType() == LpeSupportedTypes.Boolean) {
				return BOOLEAN_VALUES[0].equals(value) ? 0 : 1;
			} else if (desc.getType() == LpeSupportedTypes.String && !desc.isASet() && desc.optionsAvailable()) {
				return getOptionIndex(value, desc);
			} else {
				return value;
			}
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (value != null && element instanceof AbstractPropertyItem) {
			AbstractPropertyItem tableItem = (AbstractPropertyItem) element;
			String newValue;
			ConfigParameterDescription desc = tableItem.getConfigParameterDescription();
			if (desc.getType() == LpeSupportedTypes.Boolean) {
				newValue = (String) BOOLEAN_VALUES[(Integer) value];
			} else if (desc.getType() == LpeSupportedTypes.String && !desc.isASet() && desc.optionsAvailable()) {
				newValue = (String) desc.getOptions().toArray()[(Integer) value];
			} else {
				newValue = (String) value;
			}
			if (tableItem.getValue() == null || !tableItem.getValue().equals(newValue)) {
				tableItem.updateValue(newValue);
				ExtensionItem inputModel = propertiesViewer.getInputModel();
				inputModel.propertyDirty(tableItem);
				// we can assume that at this point inputModel is never null
				if (desc.isMandatory()) {
					inputModel.updateConnectionStatus();
				} else {
					inputModel.fireItemAppearanceChanged();
				}
				editor.markDirty();
			}
		}
	}

	/**
	 * Retrieves the index of the value string in the descriptions options set.
	 * 
	 * @param value
	 *            the value as string
	 * @param desc
	 *            the description to look in for the index
	 * @return the index of the option in the options set or 0 if no match found
	 */
	private Integer getOptionIndex(String value, ConfigParameterDescription desc) {
		int index = 0;
		for (String option : desc.getOptions()) {
			if (option.equals(value)) {
				return Integer.valueOf(index);
			}
			index++;
		}
		return Integer.valueOf(0);
	}

}
