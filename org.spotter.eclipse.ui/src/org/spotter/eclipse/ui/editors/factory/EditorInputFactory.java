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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.spotter.eclipse.ui.editors.AbstractSpotterEditorInput;

/**
 * A factory to create instances of {@link AbstractSpotterEditorInput} using
 * reflection. The runtime type of the produced editor input is determined by
 * the passed class in the constructor.
 * 
 * @author Denis Knoepfle
 * 
 */
public class EditorInputFactory {

	private static final String ERR_CALL_CONSTRUCTOR = "Error occured while trying to create Editor Input";

	private Class<?> editorInputClazz;

	/**
	 * Creates a new factory instance for the given type. The class must have a
	 * public constructor with a single parameter of type {@link IFile}.
	 * 
	 * @param clazz
	 *            The class of the concrete editor input.
	 */
	public EditorInputFactory(Class<?> clazz) {
		this.editorInputClazz = clazz;
	}

	/**
	 * Creates a concrete instance of this editor input using reflection.
	 * 
	 * @param file
	 *            the file resource
	 * @return a new instance of this editor input
	 * @throws IllegalArgumentException
	 *             when input cannot be created from the given file
	 */
	public AbstractSpotterEditorInput createInstance(IFile file) throws IllegalArgumentException {
		AbstractSpotterEditorInput input = null;

		Constructor<?>[] allConstructors = editorInputClazz.getDeclaredConstructors();
		for (Constructor<?> constructor : allConstructors) {
			Class<?>[] pType = constructor.getParameterTypes();
			if (pType.length == 1 && pType[0].equals(IFile.class)) {
				try {
					input = (AbstractSpotterEditorInput) constructor.newInstance(file);
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
					throw new IllegalArgumentException(ERR_CALL_CONSTRUCTOR, e);
				}
				break;
			}
		}
		return input;
	}

}
