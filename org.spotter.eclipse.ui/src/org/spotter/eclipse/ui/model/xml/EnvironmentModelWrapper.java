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

import java.util.ArrayList;
import java.util.List;

import org.spotter.eclipse.ui.model.ExtensionMetaobject;
import org.spotter.shared.environment.model.XMConfiguration;
import org.spotter.shared.environment.model.XMeasurementEnvObject;

/**
 * A model wrapper which wraps an environment measurement satellite and a
 * corresponding extension.
 * 
 * @author Denis Knoepfle
 * 
 */
public class EnvironmentModelWrapper extends AbstractModelWrapper {

	private List<XMeasurementEnvObject> modelContainingList;
	private final XMeasurementEnvObject wrappedModel;
	private final List<XMeasurementEnvObject> children;

	/**
	 * Creates a new wrapper without any children. Only the root wrapper
	 * assigned to the editor input is assumed to have children.
	 * 
	 * @param extension
	 *            the associated extension
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
		this.children = new ArrayList<>();
	}

	/**
	 * Creates a new wrapper. This constructor only serves for the root wrapper
	 * which will be assigned to the editor input.
	 * 
	 * @param children
	 *            the children of this wrapper
	 */
	public EnvironmentModelWrapper(List<XMeasurementEnvObject> children) {
		super(null);
		this.modelContainingList = new ArrayList<>();
		this.wrappedModel = null;
		this.children = children;
	}

	@Override
	public List<XMConfiguration> getConfig() {
		return wrappedModel != null ? wrappedModel.getConfig() : null;
	}

	@Override
	public void setConfig(List<XMConfiguration> config) {
		if (wrappedModel != null) {
			wrappedModel.setConfig(config);
		}
	}

	@Override
	public IModelWrapper copy() {
		XMeasurementEnvObject modelCopy = MeasurementEnvironmentFactory.getInstance().copyMeasurementEnvObject(
				wrappedModel);
		EnvironmentModelWrapper wrapper = new EnvironmentModelWrapper(extension, modelContainingList, modelCopy);

		return wrapper;
	}

	@Override
	public Object getXMLModel() {
		return wrappedModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setXMLModelContainingList(List<?> modelContainingList) {
		this.modelContainingList = (List<XMeasurementEnvObject>) modelContainingList;
	}

	@Override
	public List<?> getChildren() {
		return children;
	}

	@Override
	public void added() {
		if (modelContainingList != null && wrappedModel != null && !modelContainingList.contains(wrappedModel)) {
			modelContainingList.add(wrappedModel);
		}
	}

	@Override
	public void removed() {
		if (modelContainingList != null && wrappedModel != null) {
			modelContainingList.remove(wrappedModel);
		}
	}

	@Override
	public void moved(int destinationIndex) {
		if (modelContainingList == null || wrappedModel == null) {
			return;
		}

		int index = modelContainingList.lastIndexOf(wrappedModel);
		if (index != -1 && index != destinationIndex && destinationIndex >= 0
				&& destinationIndex < modelContainingList.size()) {
			modelContainingList.remove(wrappedModel);
			if (destinationIndex < modelContainingList.size()) {
				modelContainingList.add(destinationIndex, wrappedModel);
			} else {
				modelContainingList.add(wrappedModel);
			}
		}
	}

}
