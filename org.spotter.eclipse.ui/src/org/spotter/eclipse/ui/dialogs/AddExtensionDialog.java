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
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.model.ExtensionMetaobject;

/**
 * A dialog to add extensions.
 * 
 * @author Denis Knoepfle
 * 
 */
public class AddExtensionDialog extends AbstractAddDialog {

	/**
	 * Creates a new dialog to add extensions.
	 * 
	 * @param parentShell
	 *            the parent shell of this dialog
	 * @param extensions
	 *            the extensions as input
	 */
	public AddExtensionDialog(final Shell parentShell, final ExtensionMetaobject[] extensions) {
		super(parentShell, extensions);
	}

	@Override
	protected String getElementName(final Object element) {
		return ((ExtensionMetaobject) element).getExtensionDisplayLabel() + " <" + ((ExtensionMetaobject) element).getExtensionName() + ">";
	}

	@Override
	protected String getElementDescription(final Object element) {
		final ExtensionMetaobject extension = (ExtensionMetaobject) element;
		final String projectName = extension.getProjectName();
		final ServiceClientWrapper client = Activator.getDefault().getClient(projectName);

		final String description = client.getExtensionDescription(extension.getExtensionName());

		return description;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		setMessage("Choose which extensions you want to add.");
		setTitle("Add Extensions");

		return super.createDialogArea(parent);
	}

}
