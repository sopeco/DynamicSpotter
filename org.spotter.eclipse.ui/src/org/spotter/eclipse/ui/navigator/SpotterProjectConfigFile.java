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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.editors.AbstractSpotterEditor;
import org.spotter.eclipse.ui.editors.SpotterConfigEditor;
import org.spotter.eclipse.ui.editors.SpotterConfigEditorInput;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;

/**
 * An element that represents the DynamicSpotter configuration file node.
 * 
 * @author Denis Knoepfle
 * 
 */
public class SpotterProjectConfigFile implements IOpenableProjectElement {

	public static final String IMAGE_PATH = "icons/spotter-conf.gif"; //$NON-NLS-1$

	private static final String ELEMENT_NAME = "DynamicSpotter Config";

	private ISpotterProjectElement parent;
	private Image image;

	/**
	 * Creates a new instance of this element.
	 * 
	 * @param parent
	 *            the parent element
	 */
	public SpotterProjectConfigFile(ISpotterProjectElement parent) {
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
		return SpotterProjectParent.NO_CHILDREN;
	}

	@Override
	public boolean hasChildren() {
		return false;
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
		if (!Activator.getDefault().testServiceStatus(getProject().getName(), true)) {
			return;
		}
		IFile file = getProject().getFile(SpotterProjectSupport.SPOTTER_CONFIG_FILENAME);
		AbstractSpotterEditor.openInstance(new SpotterConfigEditorInput(file), SpotterConfigEditor.ID);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SpotterProjectConfigFile)) {
			return false;
		}
		SpotterProjectConfigFile other = (SpotterProjectConfigFile) obj;
		return getProject().equals(other.getProject());
	}

	@Override
	public int hashCode() {
		return getProject().getName().hashCode();
	}

}
