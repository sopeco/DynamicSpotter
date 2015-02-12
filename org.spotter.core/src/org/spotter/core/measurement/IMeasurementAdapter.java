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
package org.spotter.core.measurement;

import java.io.OutputStream;
import java.util.Properties;

import org.aim.api.exceptions.MeasurementException;
import org.aim.api.measurement.MeasurementData;
import org.aim.description.InstrumentationDescription;
import org.lpe.common.extension.IExtensionArtifact;

/**
 * Common Interface for all Measurement Controllers. Allows initialization,
 * activation and deactivation as well as collecting of data from the specific
 * measurement controllers.
 * 
 * @author Alexander Wert
 * 
 */
public interface IMeasurementAdapter extends IExtensionArtifact {
	/**
	 * Prepares monitoring with given description.
	 * 
	 * @param monitoringDescription
	 *            description
	 * @throws MeasurementException
	 */
	void prepareMonitoring(InstrumentationDescription monitoringDescription) throws MeasurementException;

	/**
	 * Resets monitoring
	 * 
	 * @throws MeasurementException
	 */
	void resetMonitoring() throws MeasurementException;

	/**
	 * Enables monitoring or measurement data collection.
	 * 
	 * @param monitoringDescription
	 *            description what to monitor
	 * @throws MeasurementException
	 *             thrown if monitoring fails
	 */
	void enableMonitoring() throws MeasurementException;

	/**
	 * Disables monitoring or measurement data collection.
	 * 
	 * @throws MeasurementException
	 *             thrown if monitoring fails
	 */
	void disableMonitoring() throws MeasurementException;

	/**
	 * 
	 * @return collected measurement data
	 * @throws MeasurementException
	 *             thrown if data cannot be retrieved
	 */
	MeasurementData getMeasurementData() throws MeasurementException;

	/**
	 * Pipes the measurement data to the given outpit stream. Note: this method
	 * call is blocking until all data has been written to the outputstream!!
	 * 
	 * @param oStream
	 *            stream where to pipe to
	 * @throws MeasurementException
	 *             thrown if streaming fails
	 */
	void pipeToOutputStream(OutputStream oStream) throws MeasurementException;

	/**
	 * Initializes measruement controller.
	 * 
	 * @throws MeasurementException
	 *             thrown if initialization fails
	 */
	void initialize() throws MeasurementException;

	/**
	 * 
	 * @return the current local timestamp of the specific controller
	 */
	long getCurrentTime();

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
	 * @param properties
	 *            the properties to set
	 */
	void setProperties(Properties properties);

	/**
	 * 
	 * @return the controller relative time of the experiment start
	 */
	long getControllerRelativeTime();

	/**
	 * 
	 * @param relativeTime
	 *            the controller relative time of the experiment start
	 */
	void setControllerRelativeTime(long relativeTime);

	/**
	 * 
	 * @param path
	 *            the path where the report file is to be stored
	 * @throws MeasurementException
	 *             thrown if storing fails
	 */
	void storeReport(String path) throws MeasurementException;
}
