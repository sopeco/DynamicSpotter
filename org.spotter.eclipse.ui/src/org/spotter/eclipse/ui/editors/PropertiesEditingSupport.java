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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
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

	/**
	 * Class that handles the cycling activation of cell editors within a table
	 * viewer during traversal when cell editing is active. This listener should
	 * be added to the control of the corresponding cell editor.
	 */
	private class ActivationTraverser implements TraverseListener {

		/**
		 * The cell editor that should be activated.
		 */
		protected final CellEditor cellEditor;

		/**
		 * Creates an activation traverser for the given cell editor.
		 * 
		 * @param cellEditor
		 *            the cell editor that should be activated
		 */
		public ActivationTraverser(CellEditor cellEditor) {
			this.cellEditor = cellEditor;
		}

		/**
		 * Performs the apply and deactivation mechanism of the cell editor.
		 */
		protected void performApplyAndDeactivate() {
			try {
				Method m = cellEditor.getClass().getSuperclass()
						.getDeclaredMethod("fireApplyEditorValue", (Class<?>[]) new Class[0]);
				m.setAccessible(true);
				m.invoke(cellEditor, (Object[]) null);
				cellEditor.deactivate();
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException("Could not apply editor value", e);
			}
		}

		@Override
		public void keyTraversed(TraverseEvent e) {
			if (e.detail != SWT.TRAVERSE_TAB_NEXT && e.detail != SWT.TRAVERSE_TAB_PREVIOUS) {
				return;
			}

			performApplyAndDeactivate();

			e.doit = false;
			TableViewer tableViewer = PropertiesEditingSupport.this.propertiesViewer.getTableViewer();
			Table table = tableViewer.getTable();
			if (table.getItemCount() <= 1) {
				return;
			}
			if (!tableViewer.getSelection().isEmpty()) {
				int index = table.getSelectionIndex();
				if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					// backwards
					index = (index - 1 + table.getItemCount()) % table.getItemCount();
					Object element = tableViewer.getElementAt(index);
					table.setSelection(index);
					tableViewer.setSelection(new StructuredSelection(element));
					tableViewer.editElement(element, table.getColumnCount() - 1);
				} else {
					// forwards
					index = (index + 1) % table.getItemCount();
					Object element = tableViewer.getElementAt(index);
					table.setSelection(index);
					tableViewer.setSelection(new StructuredSelection(element));
					tableViewer.editElement(element, table.getColumnCount() - 1);
				}
			}
		}
	}

	private class ComboActivationTraverser extends ActivationTraverser {

		public ComboActivationTraverser(CellEditor cellEditor) {
			super(cellEditor);
		}

		@Override
		protected void performApplyAndDeactivate() {
			try {
				Method m = cellEditor.getClass().getDeclaredMethod("applyAndDeactivate", (Class<?>[]) new Class[0]);
				m.setAccessible(true);
				m.invoke(cellEditor, (Object[]) null);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e1) {
				throw new RuntimeException("Could not apply editor value", e1);
			}
		}
	}

	private static final String[] BOOLEAN_VALUES = { Boolean.TRUE.toString(), Boolean.FALSE.toString() };
	private static final int COMBO_ACTIVATION_STYLE = ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION
			| ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | ComboBoxCellEditor.DROP_DOWN_ON_PROGRAMMATIC_ACTIVATION;

	private AbstractSpotterEditor editor;
	private PropertiesGroupViewer propertiesViewer;
	// default text editor
	private TextCellEditor cellDefaultTextEditor;
	// editor for numbers
	private TextCellEditor cellNumberEditor;
	// editor for booleans
	private CustomComboBoxCellEditor cellBooleanEditor;
	// editor for parameters that are no sets but have options available
	private CustomComboBoxCellEditor cellComboBoxEditor;
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
		cellDefaultTextEditor.getControl().addTraverseListener(new ActivationTraverser(cellDefaultTextEditor));

		cellNumberEditor = new TextCellEditor(parent);
		ControlDecoration decor = new ControlDecoration(cellNumberEditor.getControl(), SWT.LEFT | SWT.TOP);
		cellNumberEditor.addListener(new TextEditorErrorListener(cellNumberEditor, decor));
		cellNumberEditor.getControl().addTraverseListener(new ActivationTraverser(cellNumberEditor));

		cellBooleanEditor = new CustomComboBoxCellEditor(parent, BOOLEAN_VALUES, SWT.DROP_DOWN | SWT.READ_ONLY);
		cellBooleanEditor.setActivationStyle(COMBO_ACTIVATION_STYLE);
		cellBooleanEditor.getControl().addTraverseListener(new ComboActivationTraverser(cellBooleanEditor));
		cellComboBoxEditor = new CustomComboBoxCellEditor(parent, new String[0], SWT.DROP_DOWN | SWT.READ_ONLY);
		cellComboBoxEditor.setActivationStyle(COMBO_ACTIVATION_STYLE);
		cellComboBoxEditor.getControl().addTraverseListener(new ComboActivationTraverser(cellComboBoxEditor));

		cellCustomDialogEditor = new CustomDialogCellEditor(parent);
		cellCustomDialogEditor.getControl().addTraverseListener(new ActivationTraverser(cellCustomDialogEditor));
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
				if (desc.isASet() || desc.isADirectory() || desc.isAFile()) {
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
