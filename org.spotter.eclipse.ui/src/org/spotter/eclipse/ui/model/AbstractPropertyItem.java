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
package org.spotter.eclipse.ui.model;

import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.model.xml.IModelWrapper;

/**
 * Abstract base class to represent a property.
 * <p>
 * This container holds an underlying model and the
 * <code>ConfigParameterDescription</code> that provides information about how
 * the property might be changed. Concrete property items must implement the
 * value access/modify methods.
 * </p>
 * 
 * @author Denis Knoepfle
 * 
 */
public abstract class AbstractPropertyItem {

	protected final IModelWrapper modelWrapper;
	private final ConfigParameterDescription confParamDesc;

	/**
	 * Subclasses must call this constructor.
	 * 
	 * @param modelWrapper
	 *            the model wrapper of the property item
	 * @param confParamDesc
	 *            the parameter description of this property item
	 */
	public AbstractPropertyItem(IModelWrapper modelWrapper, ConfigParameterDescription confParamDesc) {
		this.modelWrapper = modelWrapper;
		this.confParamDesc = confParamDesc;
	}

	@Override
	public String toString() {
		return getValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof AbstractPropertyItem)) {
			return false;
		}
		AbstractPropertyItem o = (AbstractPropertyItem) obj;
		return modelWrapper == o.modelWrapper && confParamDesc == o.confParamDesc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((confParamDesc == null) ? 0 : confParamDesc.hashCode());
		result = prime * result + ((modelWrapper == null) ? 0 : modelWrapper.hashCode());
		return result;
	}

	/**
	 * @return the name of this item
	 */
	public String getName() {
		return confParamDesc == null ? "" : confParamDesc.getName();
	}

	/**
	 * @return the tool tip for this item
	 */
	public String getToolTip() {
		return confParamDesc == null ? null : createToolTip(confParamDesc);
	}

	/**
	 * @return the corresponding config parameter description.
	 */
	public ConfigParameterDescription getConfigParameterDescription() {
		return confParamDesc;
	}

	private String createToolTip(ConfigParameterDescription desc) {
		String description = desc.getDescription();
		String type = desc.getType().toString();
		return description + " (as " + type + ")";
	}

	/**
	 * @return the value of this item
	 */
	public abstract String getValue();

	/**
	 * Updates the value of this item.
	 * 
	 * @param value
	 *            the new value
	 */
	public abstract void updateValue(String value);

}
