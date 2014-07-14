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
package org.spotter.core.instrumentation;

import java.util.Properties;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.instrumentation.description.InstrumentationDescription;
import org.lpe.common.extension.IExtensionArtifact;

/**
 * PPD Interface, wrapping {@link IInstrumentation} and
 * {@link IExtensionArtifact}.
 * 
 * @author Alexander Wert
 * 
 */
public interface ISpotterInstrumentation extends IExtensionArtifact {

	String INSTRUMENTATION_INCLUDES = "org.spotter.instrumentation.packageIncludes";
	String INSTRUMENTATION_EXCLUDES = "org.spotter.instrumentation.packageExcludes";

	/**
	 * @return the name
	 */
	String getName();

	/**
	 * @return the port
	 */
	String getPort();

	/**
	 * @return the host
	 */
	String getHost();

	/**
	 * @return the properties
	 */
	Properties getProperties();

	/**
	 * Initializes the instrumentation engine.
	 * 
	 * @throws InstrumentationException
	 *             thrown if exception occur during initialization
	 */
	void initialize() throws InstrumentationException;

	/**
	 * Instruments the code according to the passed
	 * {@link InstrumentationDescription}.
	 * 
	 * @param description
	 *            describes where and how to instrument the application code
	 * @throws InstrumentationException
	 *             thrown if exception occur during instrumentation
	 */
	void instrument(InstrumentationDescription description) throws InstrumentationException;

	/**
	 * Reverts all previous instrumentation steps and resets the application
	 * code to the original state.
	 * 
	 * false
	 * 
	 * @throws InstrumentationException
	 *             thrown if exception occur during uninstrumentation
	 */
	void uninstrument() throws InstrumentationException;

	/**
	 * @param properties
	 *            the properties to set
	 */
	void setProperties(Properties properties);

}
