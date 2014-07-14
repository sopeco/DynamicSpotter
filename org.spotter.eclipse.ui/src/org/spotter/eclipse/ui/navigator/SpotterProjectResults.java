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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;

/**
 * An element that represents the results node.
 */
public class SpotterProjectResults implements ISpotterProjectElement {

	public static final String IMAGE_PATH = "icons/results.gif"; //$NON-NLS-1$

	private static final String ELEMENT_NAME = "Results";

	private ISpotterProjectElement parent;
	private ISpotterProjectElement[] children;
	private Image image;

	/**
	 * Creates a new instance of this element.
	 * 
	 * @param parent
	 *            the parent element
	 */
	public SpotterProjectResults(ISpotterProjectElement parent) {
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
			refreshChildren();
		}
		// else the children are just fine

		return children;
	}

	@Override
	public boolean hasChildren() {
		if (children == null) {
			refreshChildren();
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
		if (!(obj instanceof SpotterProjectResults)) {
			return false;
		}
		SpotterProjectResults other = (SpotterProjectResults) obj;
		return getProject().equals(other.getProject());
	}

	@Override
	public int hashCode() {
		return getProject().getName().hashCode();
	}

	public void refreshChildren() {
		children = initializeChildren(getProject());
	}

	private ISpotterProjectElement[] initializeChildren(IProject iProject) {
		ISpotterProjectElement[] children = SpotterProjectParent.NO_CHILDREN;
		String defaultResultsDir = SpotterProjectSupport.DEFAULT_RESULTS_DIR_NAME;
		IFolder resDir = iProject.getFolder(defaultResultsDir);
		File res = new File(resDir.getLocation().toString());
		List<File> runFolders = new ArrayList<>();
		if (res.exists() && res.isDirectory()) {
			File[] files = res.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					runFolders.add(file);
				}
			}

			children = new ISpotterProjectElement[runFolders.size()];
			int i = 0;
			for (File runFolder : runFolders) {
				IFolder runResultFolder = iProject.getFolder(defaultResultsDir + File.separatorChar + runFolder.getName());
				children[i++] = new SpotterProjectRunResult(this, runFolder.getName(), runResultFolder);
			}
		}

		return children;
	}

}
