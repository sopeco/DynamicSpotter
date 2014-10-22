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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ILinkHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.editors.AbstractSpotterEditorInput;
import org.spotter.eclipse.ui.handlers.IHandlerMediator;
import org.spotter.eclipse.ui.handlers.OpenHandler;
import org.spotter.eclipse.ui.menu.IOpenable;
import org.spotter.eclipse.ui.providers.NavigatorContentProvider;
import org.spotter.eclipse.ui.util.SpotterUtils;

/**
 * A helper class that links a selection in the Navigator to the corresponding
 * active editor and vice-versa if the "Link with Editor" option is enabled.
 * 
 * @author Denis Knoepfle
 * 
 */
public class LinkHelper implements ILinkHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkHelper.class);

	@Override
	public IStructuredSelection findSelection(IEditorInput anInput) {
		if (!(anInput instanceof AbstractSpotterEditorInput)) {
			throw new RuntimeException("Invalid input type");
		}
		AbstractSpotterEditorInput input = (AbstractSpotterEditorInput) anInput;
		IProject correspondingProject = input.getProject();
		String editorId = input.getEditorId();

		NavigatorContentProvider provider = Activator.getDefault().getNavigatorContentProvider();
		CommonViewer viewer = provider.getViewer();
		Object[] parentObjects = provider.getChildren(viewer.getInput());

		for (Object rawParent : parentObjects) {
			ISpotterProjectElement parent = (ISpotterProjectElement) rawParent;
			if (parent.getProject().equals(correspondingProject)) {
				ISpotterProjectElement element = recursiveElementSearch(editorId, parent);
				if (element != null) {
					// found a valid matching selection, so make it visible
					if (viewer.testFindItem(element) == null) {
						expandToElement(viewer, element);
					}
					viewer.reveal(element);
					return new StructuredSelection(element);
				}
				break;
			}
		}

		return null;
	}

	private ISpotterProjectElement recursiveElementSearch(String editorId, ISpotterProjectElement parent) {
		NavigatorContentProvider provider = Activator.getDefault().getNavigatorContentProvider();
		if (!provider.hasChildren(parent)) {
			return null;
		}

		Object[] rawChildren = provider.getChildren(parent);
		Activator.getDefault().getNavigatorViewer().refresh(parent);

		for (Object rawChild : rawChildren) {
			ISpotterProjectElement element = (ISpotterProjectElement) rawChild;
			IOpenable openHandler = getOpenHandler(rawChild);
			if (openHandler != null) {
				if (editorId.equals(openHandler.getOpenId())) {
					return element;
				}
			}
			element = recursiveElementSearch(editorId, element);
			if (element != null) {
				return element;
			}
		}
		return null;
	}

	private void expandToElement(CommonViewer viewer, ISpotterProjectElement element) {
		ISpotterProjectElement parent = element;
		List<ISpotterProjectElement> ancestorList = new ArrayList<>();
		while (!SpotterProjectParent.class.isInstance(parent)) {
			parent = (ISpotterProjectElement) parent.getParent();
			ancestorList.add(parent);
		}

		for (int i = ancestorList.size() - 1; i >= 0; i--) {
			viewer.expandToLevel(ancestorList.get(i), 1);
		}
	}

	@Override
	public void activateEditor(IWorkbenchPage aPage, IStructuredSelection aSelection) {
		Object rawElement = aSelection.getFirstElement();
		IOpenable openHandler = getOpenHandler(rawElement);
		if (!(rawElement instanceof ISpotterProjectElement) || openHandler == null) {
			return;
		}
		if (!aPage.isEditorAreaVisible()) {
			aPage.setEditorAreaVisible(true);
		}

		ISpotterProjectElement element = (ISpotterProjectElement) rawElement;
		IProject project = element.getProject();

		for (IEditorReference reference : aPage.getEditorReferences()) {
			try {
				IEditorInput editorInput = reference.getEditorInput();
				if (editorInput instanceof AbstractSpotterEditorInput) {
					AbstractSpotterEditorInput input = (AbstractSpotterEditorInput) editorInput;
					if (project.equals(input.getProject()) && input.getEditorId().equals(openHandler.getOpenId())) {
						aPage.activate(reference.getEditor(true));
						return;
					}
				}
			} catch (PartInitException e) {
				LOGGER.warn("Skipping editor reference: failed to retrieve corresponding editor input");
			}
		}
	}

	private IOpenable getOpenHandler(Object rawElement) {
		IOpenable openHandler = null;
		IHandlerMediator mediator = SpotterUtils.toHandlerMediator(rawElement);
		if (mediator != null) {
			Object handler = mediator.getHandler(OpenHandler.OPEN_COMMAND_ID);
			if (handler instanceof IOpenable) {
				openHandler = (IOpenable) handler;
			}
		}

		return openHandler;
	}

}
