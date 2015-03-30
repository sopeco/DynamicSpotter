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
import java.util.Set;

import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.environment.model.XMConfiguration;

/**
 * An interface for model wrappers.
 * 
 * @author Denis Knoepfle
 * 
 */
public interface IModelWrapper {

	/**
	 * The host configuration key.
	 */
	String HOST_KEY = ConfigKeys.SATELLITE_HOST_KEY;

	/**
	 * The port configuration key.
	 */
	String PORT_KEY = ConfigKeys.SATELLITE_PORT_KEY;

	/**
	 * The name configuration key.
	 */
	String NAME_KEY = ConfigKeys.SATELLITE_ADAPTER_NAME_KEY;

	/**
	 * Creates a copy of this wrapper including its underlying XML model. Any
	 * unique keys are replaced by new ones. Children should not be copied.
	 * 
	 * @return a copy of this wrapper
	 */
	IModelWrapper copy();

	/**
	 * @return the name of the extension that the model is associated with
	 */
	String getExtensionName();

	/**
	 * @return the name of the project the model's extension is associated with
	 */
	String getProjectName();

	/**
	 * @return the name of the model
	 */
	String getName();

	/**
	 * @return the XMConfigList contained in the underlying XML model object
	 */
	List<XMConfiguration> getConfig();

	/**
	 * Sets the XMConfigList of the underlying XML model object.
	 * 
	 * @param config
	 *            the XMConfigList
	 */
	void setConfig(List<XMConfiguration> config);

	/**
	 * @return the underlying XML model object
	 */
	Object getXMLModel();

	/**
	 * Sets the list of siblings.
	 * 
	 * @param modelContainingList
	 *            the list of siblings to set
	 */
	void setXMLModelContainingList(List<?> modelContainingList);

	/**
	 * @return the children of this model if it has any
	 */
	List<?> getChildren();

	/**
	 * @return the configuration parameter descriptions that belong to the
	 *         associated extension
	 */
	Set<ConfigParameterDescription> getExtensionConfigParams();

	/**
	 * Tests the connection using an extension artifact of the associated
	 * extension parameterized with properties extracted from the underlying
	 * model.
	 * 
	 * @return <code>true</code> if connection test successful,
	 *         <code>false</code> if connection test failed
	 * @throws Exception
	 *             if the parameters were not suitable
	 */
	Boolean testConnection() throws Exception;

	/**
	 * Must be called when this model is added.
	 */
	void added();

	/**
	 * Must be called when this model is removed.
	 */
	void removed();

	/**
	 * Must be called when this model is moved.
	 * 
	 * @param destinationIndex
	 *            index where this model is moved to
	 */
	void moved(int destinationIndex);

}
