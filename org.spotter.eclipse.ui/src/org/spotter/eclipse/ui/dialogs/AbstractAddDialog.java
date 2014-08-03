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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.spotter.eclipse.ui.util.WidgetUtils;

/**
 * An abstract dialog to add components.
 * 
 * @author Denis Knoepfle
 * 
 */
public abstract class AbstractAddDialog extends TitleAreaDialog {

	private final class ElementLabelProvider extends LabelProvider {

		/**
		 * Returns the name of the element provided by the implementing class of
		 * the dialog.
		 * 
		 * @param element
		 *            the element the name is retrieved for
		 */
		@Override
		public String getText(Object element) {
			return AbstractAddDialog.this.getElementName(element);
		}
	}

	private static final String MSG_NO_SELECT = "No elements selected.";
	private static final String MSG_MULTI_SELECT = "Selected %d elements.";
	private static final String MSG_NO_DESCRIPTION = "No description available.";

	private static final Point INITIAL_DIALOG_SIZE = new Point(580, 380);
	private static final int TABLE_VIEWER_RATIO = 55;
	private static final int DESCRIPTION_RATIO = 45;

	private final Object[] viewerInput;
	private Object[] result;
	private TableViewer viewer;
	private Text textDescription;

	/**
	 * Creates the dialog under the given parent shell. Turns available help
	 * off. The given input is sorted alphabetically using the names of the
	 * elements.
	 * 
	 * @param parentShell
	 *            The parent shell of this dialog
	 * @param viewerInput
	 *            the input for the dialog's table viewer. May be
	 *            <code>null</code> for zero elements.
	 */
	public AbstractAddDialog(Shell parentShell, Object[] viewerInput) {
		super(parentShell);
		if (viewerInput == null) {
			this.viewerInput = new Object[0];
		} else {
			this.viewerInput = viewerInput;
			Arrays.sort(this.viewerInput, createInputComparator());
		}
		this.result = null;
		setHelpAvailable(false);
	}

	/**
	 * Implementing classes must provide a name for the given element.
	 * 
	 * @return the name of the given element
	 */
	protected abstract String getElementName(Object element);

	/**
	 * Implementing classes must provide a description for the given element or
	 * <code>null</code> if none.
	 * 
	 * @return the description of the given element or <code>null</code> if none
	 */
	protected abstract String getElementDescription(Object element);

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 *            The parent composite the content is placed in
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(WidgetUtils.createFillLayout(SWT.HORIZONTAL));

		SashForm sashContainer = new SashForm(container, SWT.HORIZONTAL | SWT.SMOOTH);

		Composite tableComp = new Composite(sashContainer, SWT.NONE);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout tableColLayout = new TableColumnLayout();
		tableComp.setLayout(tableColLayout);

		viewer = new TableViewer(tableComp, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);

		TableViewerColumn tableColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableColLayout.setColumnData(tableColumn.getColumn(), new ColumnWeightData(1));
		tableColumn.getColumn().setResizable(false);
		tableColumn.getColumn().setMoveable(false);
		viewer.getTable().setLinesVisible(false);

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ElementLabelProvider());
		viewer.setInput(viewerInput);

		createTableListeners();

		textDescription = new Text(sashContainer, SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		if (viewerInput.length > 0) {
			viewer.getTable().setSelection(0);
			updateDescriptionText(viewerInput[0]);
		}

		sashContainer.setWeights(new int[] { TABLE_VIEWER_RATIO, DESCRIPTION_RATIO });

		return area;
	}

	private Comparator<Object> createInputComparator() {
		Comparator<Object> comparator = new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				String name1 = getElementName(o1);
				String name2 = getElementName(o2);

				return name1.compareTo(name2);
			}

		};

		return comparator;
	}

	private void updateDescriptionText(Object element) {
		String description = getElementDescription(element);
		textDescription.setText(description == null ? MSG_NO_DESCRIPTION : description);
	}

	private void createTableListeners() {
		final Table table = viewer.getTable();
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				int selectionCount = table.getSelectionCount();
				getButton(Window.OK).setEnabled(selectionCount > 0);
				if (selectionCount == 0) {
					textDescription.setText(MSG_NO_SELECT);
				} else if (selectionCount == 1) {
					int index = table.getSelectionIndex();
					updateDescriptionText(viewerInput[index]);
				} else if (selectionCount > 1) {
					textDescription.setText(String.format(MSG_MULTI_SELECT, selectionCount));
				}
			}

		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				int itemCount = table.getItemCount();
				int itemHeight = table.getItemHeight();
				if (e.y <= itemCount * itemHeight) {
					okPressed();
				}
			}
		});
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		getButton(Window.OK).setEnabled(viewerInput.length > 0);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return INITIAL_DIALOG_SIZE;
	}

	@Override
	protected void okPressed() {
		Table table = viewer.getTable();
		int selectionCount = table.getSelectionCount();
		if (selectionCount != 0) {
			result = new Object[selectionCount];
			int[] indices = table.getSelectionIndices();
			for (int i = 0; i < selectionCount; ++i) {
				result[i] = viewerInput[indices[i]];
			}
		}
		super.okPressed();
	}

	/**
	 * Returns the previously selected elements if any or <code>null</code>.
	 * 
	 * @return the previously selected elements if any or <code>null</code>
	 */
	public Object[] getResult() {
		return result;
	}

}
