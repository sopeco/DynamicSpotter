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
package org.spotter.eclipse.ui.editors.factory;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.spotter.eclipse.ui.editors.AbstractSpotterEditorInput;
import org.spotter.eclipse.ui.editors.HierarchyEditor;
import org.spotter.eclipse.ui.editors.HierarchyEditorInput;
import org.spotter.eclipse.ui.editors.InstrumentationEditor;
import org.spotter.eclipse.ui.editors.InstrumentationEditorInput;
import org.spotter.eclipse.ui.editors.MeasurementEditor;
import org.spotter.eclipse.ui.editors.MeasurementEditorInput;
import org.spotter.eclipse.ui.editors.SpotterConfigEditor;
import org.spotter.eclipse.ui.editors.SpotterConfigEditorInput;
import org.spotter.eclipse.ui.editors.WorkloadEditor;
import org.spotter.eclipse.ui.editors.WorkloadEditorInput;

/**
 * A element factory to restore Spotter editor inputs.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ElementFactory implements IElementFactory {

	/**
	 * The id of this factory.
	 */
	public static final String ID = "org.spotter.eclipse.ui.editors.elementFactory"; //$NON-NLS-1$

	private static final String TAG_PATH = "path"; //$NON-NLS-1$
	private static final String TAG_EDITOR_ID = "editorId"; //$NON-NLS-1$
	private static final String DLG_TITLE = "Restore Editor Input";
	private static final String ERR_RESTORE_INPUT = "Could not restore editor input!";

	// these IDs and input classes are used to create the mapping, thus the
	// lengths of these two arrays must be identical
	private static final String[] EDITOR_IDS = { InstrumentationEditor.ID, MeasurementEditor.ID, WorkloadEditor.ID,
			HierarchyEditor.ID, SpotterConfigEditor.ID };
	private static final Class<?>[] EDITOR_INPUT_CLASSES = { InstrumentationEditorInput.class,
			MeasurementEditorInput.class, WorkloadEditorInput.class, HierarchyEditorInput.class,
			SpotterConfigEditorInput.class };
	private static final Map<String, EditorInputFactory> FACTORY_MAP = createFactoryMapping();

	@Override
	public IAdaptable createElement(IMemento memento) {
		// get the file name
		String fileName = memento.getString(TAG_PATH);
		if (fileName == null) {
			return null;
		}

		// get a handle to the IFile
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileName));
		if (file == null) {
			return null;
		}

		// get the editor and create suitable editor input
		String editorId = memento.getString(TAG_EDITOR_ID);
		EditorInputFactory factory = FACTORY_MAP.get(editorId);

		IAdaptable element = null;
		
		if (factory != null) {
			try {
				element = factory.createInstance(file);
			} catch (IllegalArgumentException e) {
				// UI show error message
				element = null;
				MessageDialog.openWarning(null, DLG_TITLE, ERR_RESTORE_INPUT);
			}
		}
		
		return element;
	}

	/**
	 * Saves the state of the given spotter editor input into the given memento.
	 * 
	 * @param memento
	 *            the storage area for element state
	 * @param input
	 *            the spotter editor input
	 */
	public static void saveState(IMemento memento, AbstractSpotterEditorInput input) {
		IFile file = input.getFile();
		memento.putString(TAG_PATH, file.getFullPath().toString());
		memento.putString(TAG_EDITOR_ID, input.getEditorId());
	}

	/**
	 * Creates a Spotter editor input with the given file.
	 * 
	 * @param editorId
	 *            the id of the editor the input will be created for
	 * @param file
	 *            the resource file
	 * @return the created editor input
	 */
	public static AbstractSpotterEditorInput createEditorInput(String editorId, IFile file) { 
		EditorInputFactory factory = FACTORY_MAP.get(editorId);
		if (factory != null) {
			return factory.createInstance(file); }
		else {
			return null;
		}
	}

	private static Map<String, EditorInputFactory> createFactoryMapping() {
		if (EDITOR_IDS.length != EDITOR_INPUT_CLASSES.length) {
			throw new RuntimeException("EDITOR_IDS array length and EDITOR_INPUT_CLASSES array length do not match");
		}
		Map<String, EditorInputFactory> map = new HashMap<>();
		for (int i = 0; i < EDITOR_IDS.length; i++) {
			map.put(EDITOR_IDS[i], new EditorInputFactory(EDITOR_INPUT_CLASSES[i]));
		}
		return map;
	}

}
