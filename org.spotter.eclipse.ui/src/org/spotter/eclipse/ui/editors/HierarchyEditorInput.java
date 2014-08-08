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
import org.spotter.eclipse.ui.UICoreException;
import org.spotter.eclipse.ui.model.xml.HierarchyFactory;
import org.spotter.eclipse.ui.navigator.SpotterProjectHierarchy;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;

/**
 * Editor input for the Hierarchy Editor.
 * 
 * @author Denis Knoepfle
 * 
 */
public class HierarchyEditorInput extends AbstractSpotterEditorInput {

	private static final String NAME = "Hierarchy";
	private static final String IMAGE_PATH = SpotterProjectHierarchy.IMAGE_PATH;

	private XPerformanceProblem problemRoot;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file
	 *            the associated file.
	 */
	public HierarchyEditorInput(IFile file) {
		super(file);
		HierarchyFactory factory = HierarchyFactory.getInstance();

		try {
			problemRoot = factory.parseHierarchyFile(getPath().toString());
		} catch (UICoreException e) {
			problemRoot = HierarchyFactory.getInstance().createEmptyHierarchy();
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
		return HierarchyEditor.ID;
	}

	/**
	 * Returns the root of the problem hierarchy.
	 * 
	 * @return the performance problem root of this editor input; never <code>null</code>
	 */
	public XPerformanceProblem getPerformanceProblemRoot() {
		return problemRoot;
	}

}
