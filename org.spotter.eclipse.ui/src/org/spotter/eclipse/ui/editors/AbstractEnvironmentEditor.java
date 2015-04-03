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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.UICoreException;
import org.spotter.eclipse.ui.model.BasicEditorExtensionItemFactory;
import org.spotter.eclipse.ui.model.ExtensionMetaobject;
import org.spotter.eclipse.ui.model.IExtensionItem;
import org.spotter.eclipse.ui.model.IExtensionItemFactory;
import org.spotter.eclipse.ui.model.xml.EnvironmentModelWrapper;
import org.spotter.eclipse.ui.model.xml.IModelWrapper;
import org.spotter.eclipse.ui.model.xml.MeasurementEnvironmentFactory;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;
import org.spotter.shared.configuration.SpotterExtensionType;
import org.spotter.shared.environment.model.XMeasurementEnvObject;
import org.spotter.shared.environment.model.XMeasurementEnvironment;

/**
 * Abstract base class for environment editors.
 * 
 * @author Denis Knoepfle
 * 
 */
public abstract class AbstractEnvironmentEditor extends AbstractExtensionsEditor {

	/**
	 * Implementing editors must return the corresponding environment objects.
	 * 
	 * @return the corresponding environment objects
	 */
	protected abstract List<XMeasurementEnvObject> getMeasurementEnvironmentObjects();

	/**
	 * Implementing editors must return the corresponding extension type.
	 * 
	 * @return the corresponding extension type
	 */
	protected abstract SpotterExtensionType getExtensionType();

	/**
	 * Applies the implementing editor's related data to the XML model root.
	 * 
	 * @param xmlModelRoot
	 *            the XML model root that shall be modified
	 */
	protected abstract void applyChanges(Object xmlModelRoot);

	@Override
	public IExtensionItem getInitialExtensionsInput() {
		List<XMeasurementEnvObject> envObjects = getMeasurementEnvironmentObjects();
		List<XMeasurementEnvObject> errEnvObjects = new ArrayList<>();
		IExtensionItemFactory factory = new BasicEditorExtensionItemFactory(getEditorId());
		IExtensionItem input = factory.createExtensionItem(new EnvironmentModelWrapper(envObjects));

		String projectName = getProject().getName();
		ServiceClientWrapper client = Activator.getDefault().getClient(projectName);
		for (XMeasurementEnvObject envObj : envObjects) {
			String extName = envObj.getExtensionName();

			if (client.getExtensionConfigParamters(extName) == null) {
				DialogUtils.openWarning(TITLE_CONFIG_ERR_DIALOG, "Skipping extension item '" + extName
						+ "' because the given extension does not exist! In order to "
						+ "recover its configuration you may manually rename the extension "
						+ "in the config file before saving it, otherwise the data will be lost.");
				errEnvObjects.add(envObj);
				continue;
			}

			ExtensionMetaobject extension = new ExtensionMetaobject(projectName, extName);
			IModelWrapper wrapper = new EnvironmentModelWrapper(extension, envObjects, envObj);
			input.addItem(factory.createExtensionItem(wrapper));
		}
		envObjects.removeAll(errEnvObjects);
		return input;
	}

	@Override
	public ExtensionMetaobject[] getAvailableExtensions() {
		String projectName = getProject().getName();
		ServiceClientWrapper client = Activator.getDefault().getClient(projectName);
		return client.getAvailableExtensions(getExtensionType());
	}

	@Override
	public IModelWrapper createModelWrapper(Object parent, ExtensionMetaobject extensionComponent) {
		List<XMeasurementEnvObject> envObjects = getMeasurementEnvironmentObjects();
		XMeasurementEnvObject envObject = new XMeasurementEnvObject();
		envObject.setExtensionName(extensionComponent.getExtensionName());
		return new EnvironmentModelWrapper(extensionComponent, envObjects, envObject);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		AbstractSpotterEditorInput input = (AbstractSpotterEditorInput) getEditorInput();

		try {
			MeasurementEnvironmentFactory factory = MeasurementEnvironmentFactory.getInstance();
			XMeasurementEnvironment measurementEnv = factory.parseXMLFile(input.getPath().toString());

			applyChanges(measurementEnv);
			SpotterProjectSupport.saveEnvironment(input.getFile(), measurementEnv);

			super.doSave(monitor);
		} catch (Exception e) {
			DialogUtils.handleError(ERR_MSG_SAVE, e);
		}
	}

	@Override
	protected boolean isInputApplicable(AbstractSpotterEditorInput input) throws Exception {
		MeasurementEnvironmentFactory factory = MeasurementEnvironmentFactory.getInstance();
		XMeasurementEnvironment env = factory.parseXMLFile(input.getPath().toString());

		return env != null;

		/*
		 * if (env == null || extensionsMap == null && loadExtensions() == null)
		 * { return false; }
		 * 
		 * // TODO: currently only checks the keys but not the values for
		 * (XMeasurementEnvObject envObj : getMeasurementEnvironmentObjects()) {
		 * String extensionName = envObj.getExtensionName(); if
		 * (!extensionsMap.containsKey(extensionName)) {
		 * System.err.println("key '" + extensionName + "' not supported");
		 * return false; } if (envObj.getConfig() != null) {
		 * ExtensionDescription extension =
		 * extensionsMap.get(envObj.getExtensionName()); for (XMConfiguration
		 * xmConfig : envObj.getConfig()) { boolean foundConfigKey = false; for
		 * (ConfigParameterDescription desc : extension.getConfigParams()) { if
		 * (xmConfig.getKey().equals(desc.getName())) { foundConfigKey = true;
		 * break; } } if (!foundConfigKey) { return false; } } } }
		 * 
		 * return true;
		 */
	}

	@Override
	protected void makeInputApplicable(AbstractSpotterEditorInput input) throws UICoreException {
		XMeasurementEnvironment mEnv = MeasurementEnvironmentFactory.getInstance().createMeasurementEnvironment();
		SpotterProjectSupport.saveEnvironment(input.getFile(), mEnv);
	}

}
