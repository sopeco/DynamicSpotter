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
package org.spotter.eclipse.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.lpe.common.config.GlobalConfiguration;
import org.osgi.framework.BundleContext;
import org.spotter.eclipse.ui.navigator.SpotterProjectResults;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.spotter.eclipse.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	private static ImageRegistry imageRegistry;

	// The clients per project identified by project name
	// (each project can have individual host/port settings)
	private Map<String, ServiceClientWrapper> serviceClients = new HashMap<>();
	private TreeViewer navigatorviewer;
	// The current project history elements in the navigator
	private Map<String, SpotterProjectResults> projectHistoryElements = new HashMap<>();
	// The currently selected projects in the navigator
	private Set<IProject> selectedProjects = new HashSet<IProject>();

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		imageRegistry = plugin.getImageRegistry();
		Properties properties = new Properties();
		properties.setProperty("org.lpe.common.extension.appRootDir", "none");
		GlobalConfiguration.initialize(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns the Service Client Wrapper for the given project. If the client
	 * is requested for the first time for that project, a new one will be
	 * created and cached for later requests.
	 * 
	 * @param projectName
	 *            The name of the project the client is requested for
	 * @return the Service Client Wrapper of the given project
	 */
	public ServiceClientWrapper getClient(String projectName) {
		if (!serviceClients.containsKey(projectName)) {
			serviceClients.put(projectName, new ServiceClientWrapper(projectName));
		}
		return serviceClients.get(projectName);
	}

	/**
	 * Tests the connection status to the Spotter Service for the given project.
	 * On failure an error message is displayed to the user. When
	 * <code>resetCacheOnFailure</code> is set to true, the corresponding cache
	 * will be reset.
	 * 
	 * @param projectName
	 *            The name of the project to do the test for
	 * @param resetCacheOnFailure
	 *            flag to indicate whether to reset cached data from the server
	 *            on failure
	 * @return <code>true</code> on success, otherwise <code>false</code>
	 */
	public boolean testServiceStatus(String projectName, boolean resetCacheOnFailure) {
		ServiceClientWrapper client = getClient(projectName);
		boolean connection = client.testConnection(true);
		if (!connection && resetCacheOnFailure) {
			client.clearCache();
		}
		return connection;
	}

	/**
	 * Returns the navigator viewer.
	 * 
	 * @return the navigator viewer
	 */
	public TreeViewer getNavigatorViewer() {
		return navigatorviewer;
	}

	/**
	 * Sets the navigator viewer.
	 * 
	 * @param navigatorViewer
	 *            the navigator viewer to set
	 */
	public void setNavigatorViewer(TreeViewer navigatorViewer) {
		this.navigatorviewer = navigatorViewer;
	}

	/**
	 * @return the current project history elements in the navigator
	 */
	public Map<String, SpotterProjectResults> getProjectHistoryElements() {
		return projectHistoryElements;
	}

	/**
	 * Sets the current project history elements.
	 * 
	 * @param projectHistoryElements
	 *            the project history elements to set
	 */
	public void setProjectHistoryElements(Map<String, SpotterProjectResults> projectHistoryElements) {
		this.projectHistoryElements = projectHistoryElements;
	}

	/**
	 * @return the currently selected projects
	 */
	public Set<IProject> getSelectedProjects() {
		return selectedProjects;
	}

	/**
	 * Sets the currently selected projects.
	 * 
	 * @param selectedProjects
	 *            the selected projects
	 */
	public void setSelectedProjects(Set<IProject> selectedProjects) {
		this.selectedProjects = selectedProjects;
	}

	/**
	 * Returns the image resource defined by the image path. All images will be
	 * cached and disposed at the end of the plugin's life-cycle.
	 * 
	 * @param imagePath
	 *            Path to the image
	 * @return image defined by <code>imagePath</code>
	 */
	public static Image getImage(String imagePath) {
		Image image = imageRegistry.get(imagePath);
		if (image == null || image.isDisposed()) {
			ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, imagePath);
			imageRegistry.put(imagePath, imageDescriptor);
		}

		return imageRegistry.get(imagePath);
	}

}
