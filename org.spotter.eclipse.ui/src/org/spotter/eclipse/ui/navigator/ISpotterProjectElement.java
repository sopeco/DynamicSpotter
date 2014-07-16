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

/**
 * An interface for spotter project elements of the Spotter Project Navigator.
 * 
 * @author Denis Knoepfle
 * 
 */
public interface ISpotterProjectElement {

	/**
	 * @return the text of this element
	 */
	public String getText();

	/**
	 * @return the image of this element
	 */
	public Image getImage();

	/**
	 * @return the children elements of this element
	 */
	public Object[] getChildren();

	/**
	 * @return whether this element has children
	 */
	public boolean hasChildren();

	/**
	 * @return the parent of this element
	 */
	public Object getParent();

	/**
	 * @return the associated project
	 */
	public IProject getProject();

	/**
	 * Opens this element in an according view if opening is supported.
	 */
	public void open();

}
