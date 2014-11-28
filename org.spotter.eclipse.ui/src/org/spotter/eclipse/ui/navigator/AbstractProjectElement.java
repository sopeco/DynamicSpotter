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
import org.spotter.eclipse.ui.handlers.HandlerMediatorHelper;
import org.spotter.eclipse.ui.handlers.IHandlerMediator;

/**
 * An abstract implementation for a project element.
 * 
 * @author Denis Knoepfle
 * 
 */
public abstract class AbstractProjectElement implements ISpotterProjectElement {

	private final IHandlerMediator handlerMediatorHelper = new HandlerMediatorHelper();
	private final String className;
	private String imagePath;
	private Image image;

	protected ISpotterProjectElement[] children;

	/**
	 * Creates a new element.
	 */
	public AbstractProjectElement() {
		this.className = getClass().getName();
		this.imagePath = null;
	}

	/**
	 * Creates a new element which will use the given image path to create an
	 * image.
	 * 
	 * @param imagePath
	 *            a path to the image representing the element
	 */
	public AbstractProjectElement(String imagePath) {
		this.className = getClass().getName();
		this.imagePath = imagePath;
	}

	/**
	 * Returns the image path.
	 * 
	 * @return the image path
	 */
	public String getImagePath() {
		return imagePath;
	}

	/**
	 * Sets the image path and updates the image using this path.
	 * 
	 * @param imagePath
	 *            the path to the image
	 */
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
		this.image = null;
	}

	@Override
	public boolean canHandle(String commandId) {
		return handlerMediatorHelper.canHandle(commandId);
	}

	@Override
	public Object getHandler(String commandId) {
		return handlerMediatorHelper.getHandler(commandId);
	}

	@Override
	public void addHandler(String commandId, Object handler) {
		handlerMediatorHelper.addHandler(commandId, handler);
	}

	@Override
	public void removeHandler(String commandId) {
		handlerMediatorHelper.removeHandler(commandId);
	}

	@Override
	public Image getImage() {
		if (image == null && imagePath != null) {
			image = Activator.getImage(imagePath);
		}

		return image;
	}

	@Override
	public Object[] getChildren() {
		if (children == null) {
			children = initializeChildren(getProject());
		}

		return children;
	}

	@Override
	public void refreshChildren() {
		children = initializeChildren(getProject());
	}

	@Override
	public boolean hasChildren() {
		if (children == null) {
			children = initializeChildren(getProject());
		}

		return children.length > 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractProjectElement)) {
			return false;
		}

		AbstractProjectElement other = (AbstractProjectElement) obj;
		if (!className.equals(other.className)) {
			return false;
		}

		return getProject().equals(other.getProject());
	}

	@Override
	public int hashCode() {
		return getProject().hashCode();
	}

	/**
	 * Initializes the children. This framework method returns an empty array.
	 * Subclasses may reimplement.
	 * 
	 * @param project
	 *            the associated project
	 * @return the newly created children
	 */
	protected ISpotterProjectElement[] initializeChildren(IProject project) {
		return ISpotterProjectElement.NO_CHILDREN;
	}

}
