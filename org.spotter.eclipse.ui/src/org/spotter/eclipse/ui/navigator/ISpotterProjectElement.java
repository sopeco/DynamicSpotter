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
import org.spotter.eclipse.ui.handlers.IHandlerMediator;

/**
 * An interface for DynamicSpotter project elements of the DynamicSpotter
 * Project Navigator.
 * 
 * @author Denis Knoepfle
 * 
 */
public interface ISpotterProjectElement extends IHandlerMediator {

	/**
	 * No children.
	 */
	ISpotterProjectElement[] NO_CHILDREN = new ISpotterProjectElement[0];

	/**
	 * @return the text of this element
	 */
	String getText();

	/**
	 * @return the image of this element
	 */
	Image getImage();

	/**
	 * @return the children elements of this element
	 */
	Object[] getChildren();

	/**
	 * Recreates the children nodes.
	 */
	void refreshChildren();

	/**
	 * @return whether this element has children
	 */
	boolean hasChildren();

	/**
	 * @return the parent of this element
	 */
	Object getParent();

	/**
	 * @return the associated project
	 */
	IProject getProject();

}
