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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;

/**
 * An element that represents the results node.
 * 
 * @author Denis Knoepfle
 * 
 */
public class SpotterProjectResults implements ISpotterProjectElement {

	public static final String IMAGE_PATH = "icons/results.gif"; //$NON-NLS-1$

	private static final Logger LOGGER = LoggerFactory.getLogger(SpotterProjectResults.class);

	private static final String ELEMENT_NAME = "Results";
	private static final String EMPTY_SUFFIX = " (empty)";

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
		String suffix = hasChildren() ? "" : EMPTY_SUFFIX;
		return ELEMENT_NAME + suffix;
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

	/**
	 * Recreates the children nodes.
	 */
	public void refreshChildren() {
		children = initializeChildren(getProject());
	}

	private ISpotterProjectElement[] initializeChildren(IProject iProject) {
		String defaultResultsDir = SpotterProjectSupport.DEFAULT_RESULTS_DIR_NAME;
		IFolder resDir = iProject.getFolder(defaultResultsDir);

		if (!resDir.isSynchronized(IResource.DEPTH_INFINITE)) {
			try {
				resDir.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				LOGGER.warn("Failed to refresh results directory");
				return SpotterProjectParent.NO_CHILDREN;
			}
		}

		File res = new File(resDir.getLocation().toString());
		List<File> runFolders = new ArrayList<>();
		ISpotterProjectElement[] elements = SpotterProjectParent.NO_CHILDREN;

		if (res.exists() && res.isDirectory()) {
			File[] files = res.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					runFolders.add(file);
				}
			}

			elements = new ISpotterProjectElement[runFolders.size()];
			int i = 0;
			for (File runFolder : runFolders) {
				IFolder runResultFolder = iProject.getFolder(defaultResultsDir + File.separator + runFolder.getName());
				elements[i++] = new SpotterProjectRunResult(this, runFolder.getName(), runResultFolder);
			}
		}

		return elements;
	}

}
