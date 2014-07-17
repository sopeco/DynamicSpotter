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
package org.spotter.shared.environment.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Menvironment.
 * 
 * @author Alexander Wert
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MeasurementEnvironment", propOrder = { "instrumentationController", "measurementController",
		"workloadAdapter" })
@XmlRootElement(name = "measurementEnvironment")
public class XMeasurementEnvironment {
	private List<XMeasurementEnvObject> instrumentationController;
	private List<XMeasurementEnvObject> measurementController;
	private List<XMeasurementEnvObject> workloadAdapter;

	/**
	 * 
	 * @return list of controllers
	 */
	public List<XMeasurementEnvObject> getMeasurementController() {
		return measurementController;
	}

	/**
	 * sets the controller list.
	 * 
	 * @param measurementController
	 *            list of controllers
	 */
	public void setMeasurementController(List<XMeasurementEnvObject> measurementController) {
		this.measurementController = measurementController;
	}

	/**
	 * @return the instrumentationController
	 */
	public List<XMeasurementEnvObject> getInstrumentationController() {
		return instrumentationController;
	}

	/**
	 * @param instrumentationController
	 *            the instrumentationController to set
	 */
	public void setInstrumentationController(List<XMeasurementEnvObject> instrumentationController) {
		this.instrumentationController = instrumentationController;
	}

	/**
	 * @return the workloadAdapter
	 */
	public List<XMeasurementEnvObject> getWorkloadAdapter() {
		return workloadAdapter;
	}

	/**
	 * @param workloadAdapter
	 *            the workloadAdapter to set
	 */
	public void setWorkloadAdapter(List<XMeasurementEnvObject> workloadAdapter) {
		this.workloadAdapter = workloadAdapter;
	}

}
