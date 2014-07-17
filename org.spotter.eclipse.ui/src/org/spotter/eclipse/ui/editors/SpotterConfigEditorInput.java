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
import org.spotter.eclipse.ui.navigator.SpotterProjectConfigFile;

/**
 * Editor input for the Spotter Config Editor.
 * 
 * @author Denis Knoepfle
 * 
 */
public class SpotterConfigEditorInput extends AbstractSpotterEditorInput {

	private static final String NAME = "Spotter Config";
	private static final String IMAGE_PATH = SpotterProjectConfigFile.IMAGE_PATH;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file
	 *            the associated file.
	 */
	public SpotterConfigEditorInput(IFile file) {
		super(file);
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
		return SpotterConfigEditor.ID;
	}

}
