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
package org.spotter.eclipse.ui.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.lpe.common.config.ConfigParameterDescription;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ProjectNature;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.UICoreException;
import org.spotter.eclipse.ui.editors.AbstractSpotterEditorInput;
import org.spotter.eclipse.ui.model.xml.HierarchyFactory;
import org.spotter.eclipse.ui.model.xml.MeasurementEnvironmentFactory;
import org.spotter.eclipse.ui.view.ResultsView;
import org.spotter.shared.configuration.ConfigKeys;
import org.spotter.shared.environment.model.XMeasurementEnvironment;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;

/**
 * An utility class to support project management for Spotter projects.
 * 
 * @author Denis Knoepfle
 * 
 */
public class SpotterProjectSupport {

	public static final String DEFAULT_RESULTS_DIR_NAME = "results";
	public static final String SPOTTER_CONFIG_FILENAME = "spotter.conf";
	public static final String ENVIRONMENT_FILENAME = "mEnv.xml";
	public static final String HIERARCHY_FILENAME = "hierarchy.xml";

	private static final String TITLE_ERR_DIALOG = "Spotter Project";
	private static final String ERR_GET_SPOTTER_CONFIG_PARAMS = "Error occured while getting DynamicSpotter configuration parameters";
	private static final String ERR_CLOSE_STREAM = "Error occured while closing a stream";
	private static final String ERR_CREATE_PROJECT = "Error occured while creating a new project";
	private static final String ERR_LOAD_PROPERTIES = "Error occured while loading properties from file";
	private static final String ERR_WRITE_ENV_XML = "Error occured while writing to environment XML file";
	private static final String ERR_WRITE_HIERARCHY_XML = "Error occured while writing to hierarchy XML file";
	private static final String ERR_WRITE_SPOTTER_CONF = "Error occured while writing to DynamicSpotter configuration file";

	private static final String KEY_HIERARCHY_FILE_DESC = "path to the XML file describing the problem hierarchy";
	private static final String KEY_ENVIRONMENT_FILE_DESC = "path to the XML file describing all measurement satellites and their configurations";

	private static final String KEY_RESULTS_DIR_DESC = "path to the directory containing the results";

	private static final Logger LOGGER = LoggerFactory.getLogger(SpotterProjectSupport.class);

	// contains all project specific constant parameters
	private static final String[] ALL_KEYS = { ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE,
			ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, ConfigKeys.RESULT_DIR };

	/**
	 * Create a new Spotter project.
	 * 
	 * @param projectName
	 *            the name of the new project
	 * @param location
	 *            the location of the new project or <code>null</code> for
	 *            default location
	 * @return the newly created project or <code>null</code> on failure
	 */
	public static IProject createProject(String projectName, URI location) {
		IProject project;
		try {
			project = createBaseProject(projectName, location);
			addNature(project);
			addEnvironmentXMLConfiguration(project);
			addHierarchyXMLConfiguration(project);
			addSpotterConfig(project);

			String[] paths = { DEFAULT_RESULTS_DIR_NAME };
			addToProjectStructure(project, paths);
		} catch (Exception e) {
			// project could not be created successfully
			LOGGER.error(e.getMessage());
			// project creation failed
			MessageDialog.openError(null, TITLE_ERR_DIALOG, ERR_CREATE_PROJECT);
			project = null;
		}

		return project;
	}

	/**
	 * Delete the given project entirely from the workspace and from disk.
	 * 
	 * @param project
	 *            The project to delete
	 * @throws CoreException
	 *             if deletion fails
	 */
	public static void deleteProject(IProject project) throws CoreException {
		// close or reset open views and editors that refer to the project first
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				// reset results view for the deleted project
				ResultsView.reset(project);

				// close spotter editors
				List<IEditorReference> closeEditors = new ArrayList<IEditorReference>();
				for (IEditorReference ref : page.getEditorReferences()) {
					IEditorPart editorPart = ref.getEditor(true);
					if (editorPart != null && editorPart.getEditorInput() instanceof AbstractSpotterEditorInput) {
						AbstractSpotterEditorInput input = (AbstractSpotterEditorInput) editorPart.getEditorInput();
						if (project.equals(input.getProject())) {
							closeEditors.add(ref);
						}
					}
				}
				page.closeEditors(closeEditors.toArray(new IEditorReference[closeEditors.size()]), false);
			}
		}
		// deletes project completely from disk
		// TODO: adjust dialog to allow soft deletion only from workspace
		// without deleting it from disk
		project.delete(true, true, null);
		deleteProjectPreferences(project.getName());
	}

	/**
	 * Save the given environment to the specified file.
	 * 
	 * @param file
	 *            the destination file
	 * @param env
	 *            the measurement environment to save
	 * @throws UICoreException
	 *             when saving the environment fails
	 */
	public static void saveEnvironment(IFile file, XMeasurementEnvironment env) throws UICoreException {
		try {
			SpotterUtils.writeElementToFile(file, env);
		} catch (Exception e) {
			throw new UICoreException(ERR_WRITE_ENV_XML, e);
		}
	}

	/**
	 * Save the given performance problem hierarchy to the specified file.
	 * 
	 * @param file
	 *            the destination file
	 * @param problem
	 *            the performance problem hierarchy to save
	 * @throws UICoreException
	 *             when saving fails
	 */
	public static void saveHierarchy(IFile file, XPerformanceProblem problem) throws UICoreException {
		try {
			SpotterUtils.writeElementToFile(file, problem);
		} catch (Exception e) {
			throw new UICoreException(ERR_WRITE_HIERARCHY_XML, e);
		}
	}

	/**
	 * Save the given Spotter properties to the specified file. The necessary
	 * general properties will be added automatically.
	 * 
	 * @param file
	 *            the destination file
	 * @param properties
	 *            the Spotter properties to save
	 * @throws UICoreException
	 *             when saving fails
	 */
	public static void saveSpotterConfig(IFile file, Properties properties) throws UICoreException {
		Properties general = createGeneralSpotterProperties(file.getProject());
		String projectName = file.getProject().getName();
		String output = createSpotterConfigFileString(projectName, general, properties);
		InputStream source = new ByteArrayInputStream(output.getBytes());
		try {
			if (file.exists()) {
				file.setContents(source, true, true, null);
			} else {
				file.create(source, true, null);
			}
		} catch (CoreException e) {
			throw new UICoreException(ERR_WRITE_SPOTTER_CONF, e);
		}
		try {
			source.close();
		} catch (IOException e) {
			LOGGER.error(ERR_CLOSE_STREAM);
		}
	}

	/**
	 * Updates the Spotter configuration file. Loads the current configuration
	 * from file and saves it updating project related paths.
	 * 
	 * @param project
	 *            the project the Spotter configuration belongs to
	 * @throws UICoreException
	 *             when updating fails
	 */
	public static void updateSpotterConfig(IProject project) throws UICoreException {
		IFile spotterFile = project.getFile(SPOTTER_CONFIG_FILENAME);
		saveSpotterConfig(spotterFile, getSpotterConfig(spotterFile));
	}

	/**
	 * Loads and returns properties from the given file or <code>null</code> if
	 * properties could not be loaded.
	 * 
	 * @param file
	 *            the file to load from
	 * @return the properties loaded from the given file or <code>null</code> if
	 *         an error occurred
	 * @throws UICoreException
	 *             when properties could not be loaded
	 */
	public static Properties loadPropertiesFile(IFile file) throws UICoreException {
		Properties properties = null;
		if (file.exists()) {
			InputStream fileContents = null;
			try {
				// will throw CoreException on failure
				fileContents = file.getContents();

				Properties tmpProp = new Properties();
				// will throw IOException on failure
				tmpProp.load(fileContents);
				// successfully loaded properties
				properties = tmpProp;
			} catch (CoreException | IOException e) {
				throw new UICoreException(ERR_LOAD_PROPERTIES, e);
			}
			if (fileContents != null) {
				// close stream as it remains open after load operation
				try {
					fileContents.close();
				} catch (IOException e) {
					LOGGER.error(ERR_CLOSE_STREAM);
				}
			}
		}
		return properties;
	}

	/**
	 * Retrieves the Spotter configuration from file or creates default one. If
	 * the file does not exist a default properties object will be used instead.
	 * Removes general properties from the loaded properties. This method always
	 * returns a well initialized <code>Properties</code> object.
	 * 
	 * @param file
	 *            the source file
	 * @return the filtered properties loaded from file or a new
	 *         <code>Properties</code> object initialized with default Spotter
	 *         properties on failure
	 * @throws UICoreException
	 *             when an error occurred during the lookup or creation of the
	 *             Spotter Config
	 */
	public static Properties getSpotterConfig(IFile file) throws UICoreException {
		Properties properties = loadPropertiesFile(file);
		if (properties == null) {
			return createDefaultSpotterProperties(file.getName());
		}
		// remove project specific constant values
		for (String key : ALL_KEYS) {
			properties.remove(key);
		}
		return properties;
	}

	/**
	 * Create general Spotter properties for the given project.
	 * 
	 * @param project
	 *            the project the properties should be set for
	 * @return the general Spotter properties for the given project
	 */
	public static Properties createGeneralSpotterProperties(IProject project) {
		IPath projectPath = project.getLocation();
		Properties properties = new Properties();

		properties.setProperty(ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, projectPath.append(HIERARCHY_FILENAME)
				.toString());
		properties.setProperty(ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, projectPath.append(ENVIRONMENT_FILENAME)
				.toString());
		properties.setProperty(ConfigKeys.RESULT_DIR, projectPath.append(DEFAULT_RESULTS_DIR_NAME).toString());

		return properties;
	}

	/**
	 * Creates default Spotter properties.
	 * 
	 * @param projectName
	 *            The name of the project the properties are retrieved for
	 * @return default Spotter properties
	 * @throws UICoreException
	 *             when default spotter properties could not be created
	 */
	public static Properties createDefaultSpotterProperties(String projectName) throws UICoreException {
		Properties properties = new Properties();
		ServiceClientWrapper client = Activator.getDefault().getClient(projectName);
		Set<ConfigParameterDescription> parameters = client.getConfigurationParameters();
		if (parameters == null) {
			throw new UICoreException(ERR_GET_SPOTTER_CONFIG_PARAMS, null);
		}
		for (ConfigParameterDescription desc : parameters) {
			if (desc.isMandatory() && desc.getName() != null) {
				String val = desc.getDefaultValue() == null ? "" : desc.getDefaultValue();
				properties.put(desc.getName(), val);
			}
		}
		return properties;
	}

	/**
	 * Deletes project preferences for the given project in the plugin's
	 * preference scope if there exist any.
	 * 
	 * @param projectName
	 *            The name of the project whose preferences should be deleted
	 */
	public static void deleteProjectPreferences(String projectName) {
		IEclipsePreferences pluginPrefsRoot = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		try {
			if (pluginPrefsRoot.nodeExists(projectName)) {
				pluginPrefsRoot.node(projectName).removeNode();
				pluginPrefsRoot.flush();
			}
		} catch (BackingStoreException e) {
			LOGGER.error("Error during removal of project preferences. Cause: {}", e);
		}
	}

	/**
	 * Returns the project preferences for the given project.
	 * 
	 * @param projectName
	 *            The name of the project whose preferences are requested
	 * @return the project preferences for the given project
	 */
	public static Preferences getProjectPreferences(String projectName) {
		IEclipsePreferences pluginPrefsRoot = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		return pluginPrefsRoot.node(projectName);
	}

	/**
	 * Create a basic project.
	 * 
	 * @param projectName
	 *            name of the project
	 * @param location
	 *            location of the project
	 * @return handle to the new project
	 * @throws UICoreException
	 *             when base project could not be created
	 */
	private static IProject createBaseProject(String projectName, URI location) throws UICoreException {
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		if (!newProject.exists()) {
			URI projectLocation = location;
			IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
			if (location != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) {
				projectLocation = null;
			}

			desc.setLocationURI(projectLocation);
			try {
				newProject.create(desc, null);
				if (!newProject.isOpen()) {
					newProject.open(null);
				}
			} catch (CoreException e) {
				throw new UICoreException(ERR_CREATE_PROJECT, e);
			}
		}

		return newProject;
	}

	private static void createFolder(IFolder folder) throws CoreException {
		IContainer parent = folder.getParent();
		if (parent instanceof IFolder) {
			createFolder((IFolder) parent);
		}
		if (!folder.exists()) {
			folder.create(false, true, null);
		}
	}

	/**
	 * Create a folder structure with a parent root.
	 * 
	 * @param newProject
	 *            the new project
	 * @param paths
	 *            the paths that shall be created
	 * @throws CoreException
	 */
	private static void addToProjectStructure(IProject newProject, String[] paths) throws CoreException {
		for (String path : paths) {
			IFolder etcFolders = newProject.getFolder(path);
			createFolder(etcFolders);
		}
	}

	private static void addNature(IProject project) throws CoreException {
		if (!project.hasNature(ProjectNature.NATURE_ID)) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = ProjectNature.NATURE_ID;
			description.setNatureIds(newNatures);

			IProgressMonitor monitor = null;
			project.setDescription(description, monitor);
		}
	}

	private static void addEnvironmentXMLConfiguration(IProject project) throws UICoreException {
		XMeasurementEnvironment env = MeasurementEnvironmentFactory.getInstance().createMeasurementEnvironment();
		saveEnvironment(project.getFile(ENVIRONMENT_FILENAME), env);
	}

	private static void addHierarchyXMLConfiguration(IProject project) throws UICoreException {
		XPerformanceProblem problem = HierarchyFactory.getInstance().createProblemHierarchyRoot();
		saveHierarchy(project.getFile(HIERARCHY_FILENAME), problem);
	}

	private static void addSpotterConfig(IProject project) throws UICoreException {
		saveSpotterConfig(project.getFile(SPOTTER_CONFIG_FILENAME), createDefaultSpotterProperties(project.getName()));
	}

	private static String createSpotterConfigFileString(String projectName, Properties general, Properties properties) {
		StringBuilder sb = new StringBuilder();
		writeHeading(sb, "GENERAL");
		writeKeyValuePair(sb, general, ConfigKeys.CONF_PROBLEM_HIERARCHY_FILE, KEY_HIERARCHY_FILE_DESC);
		writeKeyValuePair(sb, general, ConfigKeys.MEASUREMENT_ENVIRONMENT_FILE, KEY_ENVIRONMENT_FILE_DESC);
		writeKeyValuePair(sb, general, ConfigKeys.RESULT_DIR, KEY_RESULTS_DIR_DESC);
		sb.append("\r\n\r\n");
		writeHeading(sb, "SPECIFIED SETTINGS");

		ServiceClientWrapper client = Activator.getDefault().getClient(projectName);
		for (String key : properties.stringPropertyNames()) {
			ConfigParameterDescription desc = client.getSpotterConfigParam(key);
			String comment = desc == null ? null : desc.getDescription();
			writeKeyValuePair(sb, properties, key, comment);
		}
		return sb.toString();
	}

	private static void writeHeading(StringBuilder sb, String heading) {
		sb.append("####################################\r\n### " + heading
				+ "\r\n####################################\r\n");
	}

	private static void writeKeyValuePair(StringBuilder sb, Properties prop, String key, String comment) {
		sb.append("\r\n");
		if (comment != null) {
			sb.append("# " + comment + "\r\n");
		}
		sb.append(key + " = " + prop.getProperty(key) + "\r\n");
	}

}
