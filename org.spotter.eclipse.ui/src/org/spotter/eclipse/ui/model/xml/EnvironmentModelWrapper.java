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
package org.spotter.eclipse.ui.model.xml;

import java.util.List;

import org.spotter.eclipse.ui.model.ExtensionMetaobject;
import org.spotter.shared.environment.model.XMConfiguration;
import org.spotter.shared.environment.model.XMeasurementEnvObject;

/**
 * A model wrapper which wraps an instrumentation controller and an
 * instrumentation extension.
 */
public class EnvironmentModelWrapper extends AbstractModelWrapper {

	private final List<XMeasurementEnvObject> modelContainingList;
	private final XMeasurementEnvObject wrappedModel;

	/**
	 * Creates a new wrapper.
	 * 
	 * @param extension
	 *            the associated extension. Must not be <code>null</code>.
	 * @param allControllers
	 *            the containing list for the given controller
	 * @param controller
	 *            the associated instrumentation controller model
	 */
	public EnvironmentModelWrapper(ExtensionMetaobject extension, List<XMeasurementEnvObject> allControllers,
			XMeasurementEnvObject controller) {
		super(extension);
		this.modelContainingList = allControllers;
		this.wrappedModel = controller;
	}

	@Override
	public List<XMConfiguration> getConfig() {
		return wrappedModel.getConfig();
	}

	@Override
	public void setConfig(List<XMConfiguration> config) {
		wrappedModel.setConfig(config);
	}

	@Override
	public Object getXMLModel() {
		return wrappedModel;
	}

	@Override
	public void removed() {
		modelContainingList.remove(wrappedModel);
	}

}
