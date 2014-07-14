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
package org.spotter.eclipse.ui.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;
import org.spotter.eclipse.ui.Activator;

/**
 * This is the parent element of all other items and represents the project node.
 */
public class SpotterProjectParent implements ISpotterProjectElement {

	public static final ISpotterProjectElement[] NO_CHILDREN = new ISpotterProjectElement[0];
	public static final String IMAGE_PATH = "icons/project.gif"; //$NON-NLS-1$

	private IProject project;
	private ISpotterProjectElement[] children;
	private Image image;

	/**
	 * Creates a new instance of this element.
	 * 
	 * @param project
	 *            the associated project
	 */
	public SpotterProjectParent(IProject project) {
		this.project = project;
	}

	@Override
	public String getText() {
		return project.getName();
	}

	@Override
	public Image getImage() {
		if (image == null) {
			image = Activator.getImage(IMAGE_PATH);
		}

		return image;
	}

	@Override
	public Object[] getChildren() {
		if (children == null) {
			children = initializeChildren(project);
		}
		// else we have already initialized them

		return children;
	}

	@Override
	public boolean hasChildren() {
		if (children == null) {
			children = initializeChildren(project);
		}
		return children.length > 0;
	}

	@Override
	public Object getParent() {
		return null;
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void open() {
		// can not be opened
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SpotterProjectParent)) {
			return false;
		}
		SpotterProjectParent other = (SpotterProjectParent) obj;
		return project.equals(other.project);
	}

	@Override
	public int hashCode() {
		return getProject().getName().hashCode();
	}

	private ISpotterProjectElement[] initializeChildren(IProject project) {
		ISpotterProjectElement[] children = {
				new SpotterProjectConfig(this),
				new SpotterProjectHierarchy(this),
				new SpotterProjectResults(this) };

		return children;
	}

}
