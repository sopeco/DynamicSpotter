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

import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.UICoreException;
import org.spotter.eclipse.ui.editors.factory.ElementFactory;
import org.spotter.eclipse.ui.model.IExtensionItem;
import org.spotter.eclipse.ui.model.IExtensionItemFactory;
import org.spotter.eclipse.ui.model.ImmutableExtensionItemFactory;
import org.spotter.eclipse.ui.model.xml.IModelWrapper;
import org.spotter.eclipse.ui.model.xml.SpotterConfigModelWrapper;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;
import org.spotter.eclipse.ui.viewers.PropertiesGroupViewer;
import org.spotter.shared.environment.model.XMConfiguration;

/**
 * An editor to edit DynamicSpotter configuration properties.
 * 
 * @author Denis Knoepfle
 * 
 */
public class SpotterConfigEditor extends AbstractSpotterEditor {

	/**
	 * The id of this editor.
	 */
	public static final String ID = "org.spotter.eclipse.ui.editors.spotterconfig";

	private static final String EDITOR_NAME = "DynamicSpotter Config";

	private IModelWrapper wrapper;
	private PropertiesGroupViewer propertiesGroupViewer;

	@Override
	protected String getEditorName() {
		return EDITOR_NAME;
	}
	
	@Override
	public String getEditorId() {
		return ID;
	}

	@Override
	protected AbstractSpotterEditorInput createEditorInput(IFile file) {
		return ElementFactory.createEditorInput(ID, file);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		AbstractSpotterEditorInput input = (AbstractSpotterEditorInput) getEditorInput();

		try {
			Properties properties = new Properties();

			for (XMConfiguration xmConfig : wrapper.getConfig()) {
				String val = xmConfig.getValue() == null ? "" : xmConfig.getValue();
				properties.put(xmConfig.getKey(), val);
			}

			SpotterProjectSupport.saveSpotterConfig(input.getFile(), properties);

			super.doSave(monitor);
		} catch (Exception e) {
			DialogUtils.handleError(ERR_MSG_SAVE, e);
		}
	}

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

		propertiesGroupViewer = new PropertiesGroupViewer(parent, this);
		IExtensionItemFactory factory = new ImmutableExtensionItemFactory(getEditorId());
		IExtensionItem inputModel = createInputModel(editorInput.getFile(), factory);
		if (inputModel != null) {
			propertiesGroupViewer.updateProperties(inputModel);
		} else {
			propertiesGroupViewer.updateProperties(factory.createExtensionItem());
		}
	}

	@Override
	public void setFocus() {
		if (propertiesGroupViewer != null) {
			propertiesGroupViewer.setFocus();
		}
	}

	private IExtensionItem createInputModel(IFile file, IExtensionItemFactory factory) {
		Properties properties;
		try {
			properties = SpotterProjectSupport.getSpotterConfig(file);
		} catch (UICoreException e) {
			DialogUtils.handleError(ERR_MSG_INIT, e);
			return null;
		}

		wrapper = new SpotterConfigModelWrapper(file.getProject().getName(), properties);
		IExtensionItem inputModel = factory.createExtensionItem(wrapper);
		inputModel.setIgnoreConnection(true);

		return inputModel;
	}

	@Override
	protected boolean isInputApplicable(AbstractSpotterEditorInput input) throws Exception {
		Properties properties = SpotterProjectSupport.loadPropertiesFile(input.getFile());
		return properties != null;
	}

	@Override
	protected void makeInputApplicable(AbstractSpotterEditorInput input) throws UICoreException {
		String projectName = input.getProject().getName();
		Properties properties = SpotterProjectSupport.createDefaultSpotterProperties(projectName);
		SpotterProjectSupport.saveSpotterConfig(input.getFile(), properties);
	}

}
