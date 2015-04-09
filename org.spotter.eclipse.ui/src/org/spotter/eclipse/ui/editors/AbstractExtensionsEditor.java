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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.model.ExtensionMetaobject;
import org.spotter.eclipse.ui.model.IExtensionItem;
import org.spotter.eclipse.ui.model.xml.IModelWrapper;
import org.spotter.eclipse.ui.viewers.ExtensionsGroupViewer;
import org.spotter.eclipse.ui.viewers.PropertiesGroupViewer;

/**
 * An abstract extensions editor which is divided into two vertical parts. The
 * upper part consists of an <code>ExtensionsGroupViewer</code> which can be
 * configured hierarchical or non-hierarchical by overriding
 * {@link #supportsHierarchicalExtensionItems()}. This implementation defaults
 * to a non-hierarchical extensions viewer. The lower part consists of a
 * <code>PropertiesGroupViewer</code> showing the editable properties of the
 * selected extension.
 * 
 * @author Denis Knoepfle
 * 
 */
public abstract class AbstractExtensionsEditor extends AbstractSpotterEditor {

	private PropertiesGroupViewer propertiesGroupViewer;
	private ExtensionsGroupViewer extensionsGroupViewer;

	/**
	 * Delegates to implementing editors to provide the initial extensions
	 * input.
	 * 
	 * @return the initial extensions input
	 */
	public abstract IExtensionItem getInitialExtensionsInput();

	/**
	 * Returns extensions that suit this editor type wrapped in description
	 * objects containing their name and configuration parameters.
	 * 
	 * @return the extensions that suit this editor type or <code>null</code>
	 *         when there was an error retrieving them
	 */
	public abstract ExtensionMetaobject[] getAvailableExtensions();

	/**
	 * Creates an empty XML model for the given extension component and returns
	 * a wrapper containing both the XML model and the corresponding extension.
	 * 
	 * @param parent
	 *            the XML parent model that will receive the created model
	 * @param extensionComponent
	 *            the extension component to be wrapped
	 * @return a wrapper for the given component
	 */
	public abstract IModelWrapper createModelWrapper(Object parent, ExtensionMetaobject extensionComponent);

	@Override
	public void createPartControl(Composite parent) {
		AbstractSpotterEditorInput editorInput = (AbstractSpotterEditorInput) getEditorInput();
		setPartName(editorInput.getName());
		setContentDescription(editorInput.getPath().toString());

		// ensure that the parent's layout is a FillLayout
		if (!(parent.getLayout() instanceof FillLayout)) {
			parent.setLayout(new FillLayout());
		}

		String projectName = editorInput.getProject().getName();
		ServiceClientWrapper client = Activator.getDefault().getClient(projectName);
		if (!client.testConnection(false)) {
			// cannot create part without server information
			Label label = new Label(parent, SWT.WRAP);
			label.setText(ERR_MSG_INIT + "\n\nReason: No connection to Spotter Service. Check settings and try again.");
			return;
		}

		SashForm container = new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);

		// first part: configured components group
		extensionsGroupViewer = new ExtensionsGroupViewer(container, this, supportsHierarchicalExtensionItems(), true);

		// second part: properties group
		propertiesGroupViewer = new PropertiesGroupViewer(container, this);

		extensionsGroupViewer.setPropertiesGroupViewer(propertiesGroupViewer);
		getSite().setSelectionProvider(extensionsGroupViewer.getViewer());

		// define proportioning of configured components and properties group
		container.setWeights(new int[] { 1, 2 });
	}

	@Override
	public void setFocus() {
		if (extensionsGroupViewer != null) {
			extensionsGroupViewer.setFocus();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (extensionsGroupViewer != null) {
			extensionsGroupViewer.dispose();
		}
	}

	/**
	 * To support hierarchical extension items implementing editors must
	 * override this method and return <code>true</code>. The default
	 * implementation returns <code>false</code>.
	 * 
	 * @return <code>true</code> to support hierarchical extension items,
	 *         otherwise <code>false</code>
	 */
	protected boolean supportsHierarchicalExtensionItems() {
		return false;
	}

}
