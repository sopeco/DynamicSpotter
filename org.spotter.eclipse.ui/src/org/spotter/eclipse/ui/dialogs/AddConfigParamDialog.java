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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.lpe.common.config.ConfigParameterDescription;

/**
 * A dialog to add configuration parameters.
 * 
 * @author Denis Knoepfle
 * 
 */
public class AddConfigParamDialog extends AbstractAddDialog {

	/**
	 * Creates a new dialog to add configuration parameters.
	 * 
	 * @param parentShell
	 *            the parent shell of this dialog
	 * @param configParams
	 *            the configuration parameters as input
	 */
	public AddConfigParamDialog(Shell parentShell, ConfigParameterDescription[] configParams) {
		super(parentShell, configParams);
	}

	@Override
	protected String getElementName(Object element) {
		return ((ConfigParameterDescription) element).getName();
	}

	@Override
	protected String getElementDescription(Object element) {
		return ((ConfigParameterDescription) element).getDescription();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Choose which non-mandatory config parameters you want to add.");
		setTitle("Add Config Parameters");

		return super.createDialogArea(parent);
	}

}
