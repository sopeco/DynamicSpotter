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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.spotter.eclipse.ui.editors.factory.ElementFactory;
import org.spotter.shared.configuration.SpotterExtensionType;
import org.spotter.shared.environment.model.XMeasurementEnvObject;
import org.spotter.shared.environment.model.XMeasurementEnvironment;

/**
 * A measurement editor to edit measurement controllers.
 * 
 * @author Denis Knoepfle
 * 
 */
public class MeasurementEditor extends AbstractEnvironmentEditor {

	/**
	 * The id of this editor.
	 */
	public static final String ID = "org.spotter.eclipse.ui.editors.measurement";

	private static final String EDITOR_NAME = "Measurement Satellite Adapter";
	private static final SpotterExtensionType EXTENSION_TYPE = SpotterExtensionType.MEASUREMENT_EXTENSION;

	private List<XMeasurementEnvObject> measurementControllers;

	@Override
	protected String getEditorName() {
		return EDITOR_NAME;
	}
	
	@Override
	public String getEditorId() {
		return ID;
	}

	@Override
	protected void applyChanges(Object xmlModelRoot) {
		XMeasurementEnvironment env = (XMeasurementEnvironment) xmlModelRoot;
		env.setMeasurementController(measurementControllers);
	}

	@Override
	protected AbstractSpotterEditorInput createEditorInput(IFile file) {
		return ElementFactory.createEditorInput(ID, file);
	}

	@Override
	protected List<XMeasurementEnvObject> getMeasurementEnvironmentObjects() {
		if (measurementControllers == null) {
			MeasurementEditorInput editorInput = (MeasurementEditorInput) getEditorInput();
			measurementControllers = editorInput.getMeasurementControllers();
		}
		return measurementControllers;
	}

	@Override
	protected SpotterExtensionType getExtensionType() {
		return EXTENSION_TYPE;
	}

}
