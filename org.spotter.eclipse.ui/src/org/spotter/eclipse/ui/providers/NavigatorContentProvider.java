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
package org.spotter.eclipse.ui.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ProjectNature;
import org.spotter.eclipse.ui.navigator.FixedOrderViewerComparator;
import org.spotter.eclipse.ui.navigator.ISpotterProjectElement;
import org.spotter.eclipse.ui.navigator.SpotterProjectParent;
import org.spotter.eclipse.ui.navigator.SpotterProjectResults;
import org.spotter.eclipse.ui.util.SpotterUtils;

/**
 * Content provider for items of DynamicSpotter Project Navigator. This provider
 * is used by the DynamicSpotter Project Navigator.
 * 
 * @author Denis Knoepfle
 * 
 */
public class NavigatorContentProvider implements ITreeContentProvider, IResourceChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(NavigatorContentProvider.class);

	private static final Object[] NO_CHILDREN = {};
	private final Map<String, Object> wrapperCache = new HashMap<String, Object>();
	private CommonViewer viewer;
	private boolean listenersRegistered;
	private IDoubleClickListener dblClickOpenListener;
	private ISelectionChangedListener projectSelectionListener;

	/**
	 * Creates a new content provider. Registers the newly created instance as
	 * resource change listener at the workspace.
	 */
	public NavigatorContentProvider() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		listenersRegistered = false;
		Activator.getDefault().setNavigatorContentProvider(this);
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		if (listenersRegistered && viewer != null) {
			removeListeners();
		}

		Activator.getDefault().setNavigatorContentProvider(null);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (!(viewer instanceof CommonViewer)) {
			throw new IllegalStateException("Illegal viewer type provided");
		}

		if (listenersRegistered && this.viewer != null) {
			removeListeners();
		}

		this.viewer = (CommonViewer) viewer;
		this.viewer.setComparator(new FixedOrderViewerComparator());

		registerListeners();
	}

	/**
	 * Returns the common viewer currently associated with this content
	 * provider.
	 * 
	 * @return the currently associated viewer
	 */
	public CommonViewer getViewer() {
		return this.viewer;
	}

	private void removeListeners() {
		viewer.removeDoubleClickListener(dblClickOpenListener);
		viewer.removeSelectionChangedListener(projectSelectionListener);

		Activator.getDefault().setSelectedProjects(viewer, Collections.<IProject> emptySet());
		dblClickOpenListener = null;
		projectSelectionListener = null;
		listenersRegistered = false;
	}

	private void registerListeners() {
		dblClickOpenListener = createDblClickOpenListener();
		projectSelectionListener = createProjectSelectionListener();

		viewer.addDoubleClickListener(dblClickOpenListener);
		viewer.addSelectionChangedListener(projectSelectionListener);
		listenersRegistered = true;
	}

	private IDoubleClickListener createDblClickOpenListener() {
		IDoubleClickListener listener = new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				SpotterUtils.openElement(sel.getFirstElement());
			}
		};

		return listener;
	}

	private ISelectionChangedListener createProjectSelectionListener() {
		ISelectionChangedListener listener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				Set<IProject> selectedProjects = new HashSet<IProject>();

				if (!sel.isEmpty()) {
					for (Object obj : sel.toArray()) {
						if (obj instanceof ISpotterProjectElement) {
							ISpotterProjectElement element = (ISpotterProjectElement) obj;
							selectedProjects.add(element.getProject());

							if (!(obj instanceof SpotterProjectParent) && selectedProjects.size() >= 2) {

								// already another project selected and mixed
								// element types are not allowed

								selectedProjects.clear();
								break;
							}
						}
					}
				}

				Activator.getDefault().setSelectedProjects(viewer, selectedProjects);
			}
		};

		return listener;
	}

	/**
	 * Removes a project from the cache.
	 * 
	 * @param projectName
	 *            the name of the project to remove
	 */
	public void removeCachedProject(String projectName) {
		wrapperCache.remove(projectName);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		Object[] children = null;
		if (parentElement instanceof IWorkspaceRoot) {
			IProject[] projects = ((IWorkspaceRoot) parentElement).getProjects();
			children = createSpotterProjectParents(projects);
		} else if (parentElement instanceof ISpotterProjectElement) {
			children = ((ISpotterProjectElement) parentElement).getChildren();
		} else {
			children = NO_CHILDREN;
		}

		return children;
	}

	private Object[] createSpotterProjectParents(IProject[] projects) {
		Object[] result = null;

		List<Object> list = new ArrayList<>();
		Set<String> deadProjects = new HashSet<>();
		deadProjects.addAll(wrapperCache.keySet());

		for (int i = 0; i < projects.length; i++) {
			String projectName = projects[i].getName();
			deadProjects.remove(projectName);
			Object spotterProjectParent = wrapperCache.get(projectName);
			if (spotterProjectParent == null) {
				spotterProjectParent = createSpotterProjectParent(projects[i]);
				wrapperCache.put(projects[i].getName(), spotterProjectParent);
			}

			if (spotterProjectParent != null) {
				list.add(spotterProjectParent);
			} // else ignore the project
		}

		// clean up the cache from projects that no longer exist
		for (String deadProject : deadProjects) {
			wrapperCache.remove(deadProject);
		}

		result = new Object[list.size()];
		Map<String, SpotterProjectResults> projectHistoryElements = Activator.getDefault().getProjectHistoryElements();
		projectHistoryElements.clear();
		for (Object element : list) {
			SpotterProjectParent projectParent = (SpotterProjectParent) element;
			for (Object child : projectParent.getChildren()) {
				if (child instanceof SpotterProjectResults) {
					projectHistoryElements.put(projectParent.getProject().getName(), (SpotterProjectResults) child);
				}
			}
		}
		list.toArray(result);

		return result;
	}

	private Object createSpotterProjectParent(IProject parentElement) {
		Object result = null;
		try {
			if (parentElement.getNature(ProjectNature.NATURE_ID) != null) {
				result = new SpotterProjectParent(parentElement);
			}
		} catch (CoreException e) {
			LOGGER.warn("Unable to resolve nature for project " + parentElement.getName() + ".", e);
			// Ignore and go to the next IProject
		}

		return result;
	}

	@Override
	public Object getParent(Object element) {
		Object parent = null;
		if (IProject.class.isInstance(element)) {
			parent = ((IProject) element).getWorkspace().getRoot();
		} else if (ISpotterProjectElement.class.isInstance(element)) {
			parent = ((ISpotterProjectElement) element).getParent();
		}
		return parent;
	}

	@Override
	public boolean hasChildren(Object element) {
		boolean hasChildren = false;

		if (IWorkspaceRoot.class.isInstance(element)) {
			hasChildren = ((IWorkspaceRoot) element).getProjects().length > 0;
		} else if (ISpotterProjectElement.class.isInstance(element)) {
			hasChildren = ((ISpotterProjectElement) element).hasChildren();
		}
		// else it is not one of these so return false

		return hasChildren;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org
	 * .eclipse.core.resources.IResourceChangeEvent)
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		UIJob uiJob = new UIJob("refresh Project Navigator") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (viewer != null && !viewer.getControl().isDisposed() && !viewer.isBusy()) {
					TreePath[] treePaths = viewer.getExpandedTreePaths();
					viewer.refresh();
					viewer.setExpandedTreePaths(treePaths);
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.schedule();
	}

}
