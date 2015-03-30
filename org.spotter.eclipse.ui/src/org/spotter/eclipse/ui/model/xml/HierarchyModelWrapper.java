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
import org.spotter.shared.hierarchy.model.XPerformanceProblem;

/**
 * A model wrapper which wraps a performance problem and a detection extension.
 * <p>
 * Does not support connection tests which means <code>testConnection()</code>
 * will do nothing and always return <code>null</code>.
 * </p>
 * 
 * @author Denis Knoepfle
 * 
 */
public class HierarchyModelWrapper extends AbstractModelWrapper {

	private List<XPerformanceProblem> modelContainingList;
	private final XPerformanceProblem wrappedModel;

	/**
	 * Creates a new wrapper.
	 * 
	 * @param extension
	 *            the associated extension. May be <code>null</code>.
	 * @param allProblems
	 *            the containing list for the given problem
	 * @param problem
	 *            the associated problem model
	 */
	public HierarchyModelWrapper(ExtensionMetaobject extension, List<XPerformanceProblem> allProblems,
			XPerformanceProblem problem) {
		super(extension);
		this.modelContainingList = allProblems;
		this.wrappedModel = problem;
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
		XPerformanceProblem modelCopy = HierarchyFactory.getInstance().copyPerformanceProblem(wrappedModel);
		HierarchyModelWrapper wrapper = new HierarchyModelWrapper(extension, modelContainingList, modelCopy);

		return wrapper;
	}

	@Override
	public Object getXMLModel() {
		return wrappedModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setXMLModelContainingList(List<?> modelContainingList) {
		this.modelContainingList = (List<XPerformanceProblem>) modelContainingList;
	}

	@Override
	public List<?> getChildren() {
		if (wrappedModel.getProblem() == null) {
			wrappedModel.setProblem(new ArrayList<XPerformanceProblem>());
		}
		return wrappedModel.getProblem();
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
