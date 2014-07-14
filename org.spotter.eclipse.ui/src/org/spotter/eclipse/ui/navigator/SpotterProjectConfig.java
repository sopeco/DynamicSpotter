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
 * This is the parent element of all other configuration elements represents the configuration node.
 */
public class SpotterProjectConfig implements ISpotterProjectElement {

	public static final String IMAGE_PATH = "icons/config.png"; //$NON-NLS-1$
	
	private static final String ELEMENT_NAME = "Configuration";

	private ISpotterProjectElement parent;
	private ISpotterProjectElement[] children;
	private Image image;

	/**
	 * Creates a new instance of this element.
	 * 
	 * @param parent
	 *            the parent element
	 */
	public SpotterProjectConfig(ISpotterProjectElement parent) {
		this.parent = parent;
	}

	@Override
	public String getText() {
		return ELEMENT_NAME;
	}

	@Override
	public Image getImage() {
		if (image == null) {
			image = Activator.getImage(IMAGE_PATH);
		}

		return image;
	}

	@Override
	public ISpotterProjectElement[] getChildren() {
		if (children == null) {
			children = initializeChildren(getProject());
		}
		// else the children are just fine

		return children;
	}

	@Override
	public boolean hasChildren() {
		if (children == null) {
			children = initializeChildren(getProject());
		}
		// else we have already initialized them

		return children.length > 0;
	}

	@Override
	public Object getParent() {
		return parent;
	}

	@Override
	public IProject getProject() {
		return parent.getProject();
	}

	@Override
	public void open() {
		// not editable
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SpotterProjectConfig)) {
			return false;
		}
		SpotterProjectConfig other = (SpotterProjectConfig) obj;
		return getProject().equals(other.getProject());
	}

	@Override
	public int hashCode() {
		return getProject().getName().hashCode();
	}

	private ISpotterProjectElement[] initializeChildren(IProject iProject) {
		ISpotterProjectElement[] children = new ISpotterProjectElement[] {
				new SpotterProjectConfigFile(this),
				new SpotterProjectConfigInstrumentation(this),
				new SpotterProjectConfigMeasurement(this),
				new SpotterProjectConfigWorkload(this) };

		return children;
	}

}
