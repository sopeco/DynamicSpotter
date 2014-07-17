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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.editors.factory.ElementFactory;

/**
 * Abstract base class for all spotter editor inputs.
 * 
 * @author Denis Knoepfle
 * 
 */
public abstract class AbstractSpotterEditorInput extends FileEditorInput {

	/**
	 * Creates an editor input based of the given file resource.
	 * 
	 * @param file
	 *            the file resource
	 */
	public AbstractSpotterEditorInput(IFile file) {
		super(file);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, getImagePath());
	}

	/**
	 * Returns the name of this editor input. Implementing classes should return
	 * a meaningful name which makes this input distinguishable from others.
	 * 
	 * @return the name of this editor input
	 */
	@Override
	public abstract String getName();

	@Override
	public void saveState(IMemento memento) {
		ElementFactory.saveState(memento, this);
	}

	@Override
	public String getFactoryId() {
		return ElementFactory.ID;
	}

	// copied from
	// org.eclipse.ui.part.FileEditorInput#getAdapter(java.lang.Class)
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (IWorkbenchAdapter.class.equals(adapter)) {
			return new IWorkbenchAdapter() {

				public Object[] getChildren(Object o) {
					return new Object[0];
				}

				public ImageDescriptor getImageDescriptor(Object object) {
					return AbstractSpotterEditorInput.this.getImageDescriptor();
				}

				public String getLabel(Object o) {
					return AbstractSpotterEditorInput.this.getName();
				}

				public Object getParent(Object o) {
					return AbstractSpotterEditorInput.this.getFile().getParent();
				}
			};
		}

		return super.getAdapter(adapter);
	}

	@Override
	public boolean equals(Object obj) {
		boolean matchEditor = true;
		if (obj instanceof AbstractSpotterEditorInput) {
			matchEditor = getEditorId().equals(((AbstractSpotterEditorInput) obj).getEditorId());
		}
		return matchEditor && super.equals(obj);
	}

	@Override
	public int hashCode() {
		return getEditorId().hashCode() * super.hashCode();
	}

	/**
	 * @return the project that is associated with the underlying file handler
	 */
	public IProject getProject() {
		return getFile().getProject();
	}

	/**
	 * Implementing classes should return a valid path to a suitable image for
	 * this editor input.
	 * 
	 * @return path to an image associated with this input
	 */
	protected abstract String getImagePath();

	/**
	 * Implementing classes must return the id of the editor this input should
	 * be opened with.
	 * 
	 * @return the id of the corresponding editor
	 */
	public abstract String getEditorId();

}
