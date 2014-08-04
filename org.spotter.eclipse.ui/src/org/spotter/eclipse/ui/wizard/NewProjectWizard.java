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
package org.spotter.eclipse.ui.wizard;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;

/**
 * A wizard to create new DynamicSpotter projects.
 * 
 * @author Denis Knoepfle
 * 
 */
public class NewProjectWizard extends Wizard implements INewWizard, IExecutableExtension {

	private static final String WIZARD_NAME = "New DynamicSpotter Project";
	private static final String PAGE_TITLE = "DynamicSpotter Project";
	private static final String PAGE_DESCRIPTION = "Enter a project name.";

	private IConfigurationElement configurationElement;
	private WizardNewProjectCreationPage pageOne;
	private ConnectionWizardPage pageTwo;

	/**
	 * Creates a new wizard.
	 */
	public NewProjectWizard() {
		super();
		setWindowTitle(WIZARD_NAME);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		super.addPages();

		pageOne = new WizardNewProjectCreationPage(PAGE_TITLE);
		pageOne.setTitle(PAGE_TITLE);
		pageOne.setDescription(PAGE_DESCRIPTION);

		pageTwo = new ConnectionWizardPage();

		addPage(pageOne);
		addPage(pageTwo);
	}

	@Override
	public boolean performFinish() {
		String host = pageTwo.getHost();
		String port = pageTwo.getPort();

		if (!pageTwo.testConnection()) {
			ServiceClientWrapper.showConnectionProblemMessage(null, host, port, true);
			return false;
		}

		String name = pageOne.getProjectName();
		URI location = null;
		if (!pageOne.useDefaults()) {
			location = pageOne.getLocationURI();
		} // else location == null

		// make sure there are no preferences cached from an old project with
		// the same name
		SpotterProjectSupport.deleteProjectPreferences(name);

		ServiceClientWrapper client = Activator.getDefault().getClient(name);
		if (!client.saveServiceClientSettings(host, port)) {
			return false;
		}
		if (SpotterProjectSupport.createProject(name, location) == null) {
			return false;
		}
		// change to proper perspective
		BasicNewProjectResourceWizard.updatePerspective(configurationElement);

		return true;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		configurationElement = config;
	}

}
