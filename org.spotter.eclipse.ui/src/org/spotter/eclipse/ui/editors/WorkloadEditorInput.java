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

import org.eclipse.core.resources.IFile;
import org.spotter.eclipse.ui.model.xml.MeasurementEnvironmentFactory;
import org.spotter.eclipse.ui.navigator.SpotterProjectConfigWorkload;
import org.spotter.shared.environment.model.XMeasurementEnvObject;
import org.spotter.shared.environment.model.XMeasurementEnvironment;

/**
 * Editor input for the Workload Editor.
 * 
 * @author Denis Knoepfle
 * 
 */
public class WorkloadEditorInput extends AbstractSpotterEditorInput {

	private static final String NAME = "Workload";
	private static final String IMAGE_PATH = SpotterProjectConfigWorkload.IMAGE_PATH;
	
	private List<XMeasurementEnvObject> workloadAdapters;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file
	 *            the associated file.
	 * @throws IllegalArgumentException
	 *             when there's a problem creating the input from the given file
	 */
	public WorkloadEditorInput(IFile file) throws IllegalArgumentException {
		super(file);
		MeasurementEnvironmentFactory factory = MeasurementEnvironmentFactory.getInstance();
		XMeasurementEnvironment measurementEnv;

		try {
			measurementEnv = factory.parseXMLFile(getPath().toString());
		} catch (IllegalArgumentException e) {
			measurementEnv = null;
		}

		if (measurementEnv == null || measurementEnv.getWorkloadAdapter() == null) {
			workloadAdapters = new ArrayList<>();
		} else {
			workloadAdapters = measurementEnv.getWorkloadAdapter();
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected String getImagePath() {
		return IMAGE_PATH;
	}

	@Override
	public String getEditorId() {
		return WorkloadEditor.ID;
	}

	/**
	 * @return the workload adapters of this editor input
	 */
	public List<XMeasurementEnvObject> getWorkloadAdapters() {
		return workloadAdapters;
	}

}
