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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.lpe.common.util.LpeFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.handlers.DeleteHandler;
import org.spotter.eclipse.ui.handlers.DuplicateHandler;
import org.spotter.eclipse.ui.handlers.IHandlerMediator;
import org.spotter.eclipse.ui.handlers.OpenHandler;
import org.spotter.eclipse.ui.menu.IDeletable;
import org.spotter.eclipse.ui.menu.IDuplicatable;
import org.spotter.eclipse.ui.menu.IOpenable;
import org.spotter.eclipse.ui.navigator.ISpotterProjectElement;
import org.spotter.eclipse.ui.navigator.SpotterProjectParent;
import org.spotter.shared.environment.model.XMConfiguration;
import org.spotter.shared.result.ResultsLocationConstants;
import org.spotter.shared.result.model.ResultsContainer;
import org.spotter.shared.util.JAXBUtil;

/**
 * An utility class for the Spotter UI.
 * 
 * @author Denis Knoepfle
 * 
 */
public final class SpotterUtils {

	private static final String ERR_MSG_OPEN = "Error while opening element '%s'!";
	private static final String ERR_MSG_DUPLICATE = "Error while duplicating element '%s'!";
	private static final String ERR_MSG_DELETE = "Error while deleting!";

	private static final Logger LOGGER = LoggerFactory.getLogger(SpotterUtils.class);

	/**
	 * Available name formatters for the displayed keys.
	 */
	public static final AbstractNameFormatter[] NAME_FORMATTERS = { new OmitPrefixNameFormatter(),
			new PackageInitialsNameFormatter(), new FullNameFormatter() };

	private SpotterUtils() {
	}

	/**
	 * Writes the given JAXBElement to the specified file. If the file does not
	 * exist yet, it will be created.
	 * 
	 * @param file
	 *            the destination file
	 * @param jaxbElement
	 *            the JAXBElement to marshal
	 * @throws JAXBException
	 *             if an error with JAXB occurs
	 * @throws CoreException
	 *             if a resource error occurs
	 */
	public static void writeElementToFile(IFile file, Object jaxbElement) throws JAXBException, CoreException {
		InputStream source = JAXBUtil.createInputStreamFromElement(jaxbElement);
		if (file.exists()) {
			file.setContents(source, true, true, null);
		} else {
			file.create(source, true, null);
		}
	}

	/**
	 * Reads the results container within the given result folder. In case of
	 * failure <code>null</code> will be returned.
	 * 
	 * @param resultFolder
	 *            the result folder to read from
	 * @return the read container or <code>null</code>
	 */
	public static ResultsContainer readResultsContainer(IFolder resultFolder) {
		IFile resFile = resultFolder.getFile(ResultsLocationConstants.RESULTS_SERIALIZATION_FILE_NAME);
		ResultsContainer resultsContainer = null;
		File containerFile = new File(resFile.getLocation().toString());
		if (containerFile.exists()) {
			try {
				resultsContainer = (ResultsContainer) LpeFileUtils.readObject(containerFile);
			} catch (ClassNotFoundException | IOException e) {
				LOGGER.debug("Cannot read results container " + containerFile);
			}
		}
		return resultsContainer;
	}

	/**
	 * Writes the container to the given result folder.
	 * 
	 * @param resultFolder
	 *            the result folder to write to
	 * @param container
	 *            the container to be written
	 * @return <code>true</code> on success, <code>false</code> otherwise
	 */
	public static boolean writeResultsContainer(IFolder resultFolder, ResultsContainer container) {
		IFile resFile = resultFolder.getFile(ResultsLocationConstants.RESULTS_SERIALIZATION_FILE_NAME);
		File containerFile = new File(resFile.getLocation().toString());
		try {
			LpeFileUtils.writeObject(containerFile.getAbsolutePath(), container);
		} catch (IOException e) {
			String message = "Error while writing results container!";
			LOGGER.error(message, e);
			DialogUtils.handleError(message, e);
			return false;
		}
		return true;
	}

	/**
	 * Copies the configuration list.
	 * 
	 * @param config
	 *            the list to copy
	 * @return a deep copy of the list
	 */
	public static List<XMConfiguration> copyConfigurationList(List<XMConfiguration> config) {
		List<XMConfiguration> configCopy = new ArrayList<>();
		if (config != null) {
			for (XMConfiguration xmConfig : config) {
				XMConfiguration xmConfigCopy = new XMConfiguration();
				xmConfigCopy.setKey(xmConfig.getKey());
				xmConfigCopy.setValue(xmConfig.getValue());
				configCopy.add(xmConfigCopy);
			}
		}
		return configCopy;
	}

	/**
	 * Checks whether the given list of config parameters contains the key.
	 * 
	 * @param config
	 *            The list to check
	 * @param key
	 *            The key to find
	 * @return <code>true</code> when the key is found, <code>false</code>
	 *         otherwise
	 */
	public static boolean hasConfigParameter(List<XMConfiguration> config, String key) {
		if (config != null) {
			for (XMConfiguration xmConfig : config) {
				if (key.equals(xmConfig.getKey())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the first element in the given selection matching the parameter
	 * or <code>null</code> if none available.
	 * 
	 * @param <T>
	 *            element type
	 * @param selection
	 *            the selection to extract from
	 * @param clazz
	 *            the class of the expected element
	 * @return the first element or <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	public static <T> T extractFirstElement(ISelection selection, Class<T> clazz) {
		T result = null;
		if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (clazz.isInstance(element)) {
				result = (T) element;
			}
		}

		return result;
	}

	/**
	 * Extracts the config parameter with the given key.
	 * 
	 * @param config
	 *            The list to check
	 * @param key
	 *            The key of the config parameter
	 * @return the config parameter matching the key or <code>null</code> if not
	 *         found
	 */
	public static String extractConfigValue(List<XMConfiguration> config, String key) {
		if (config != null && key != null) {
			for (XMConfiguration xmConfig : config) {
				if (key.equals(xmConfig.getKey())) {
					return xmConfig.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * Convenience method to set a config parameter in the given list. If the
	 * list was <code>null</code> a new one is created.
	 * 
	 * @param config
	 *            The list to modify
	 * @param key
	 *            The key of the config parameter
	 * @param value
	 *            The new value of the config parameter
	 * @return the modified list
	 */
	public static List<XMConfiguration> setConfigValue(List<XMConfiguration> config, String key, String value) {
		if (config == null) {
			config = new ArrayList<XMConfiguration>();
			XMConfiguration xmConfig = new XMConfiguration();
			xmConfig.setKey(key);
			xmConfig.setValue(value);
			config.add(xmConfig);
			return config;
		} else {
			for (XMConfiguration xmConfig : config) {
				if (key.equals(xmConfig.getKey())) {
					xmConfig.setValue(value);
					break;
				}
			}
			return config;
		}
	}

	/**
	 * Refreshes the project parent of the given project element in the
	 * navigator. If no parent can be found nothing is changed.
	 * 
	 * @param projectElement
	 *            a project element of the navigator
	 */
	public static void refreshProjectParent(ISpotterProjectElement projectElement) {
		while (projectElement.getParent() != null) {
			projectElement = (ISpotterProjectElement) projectElement.getParent();
		}

		if (projectElement instanceof SpotterProjectParent) {
			((SpotterProjectParent) projectElement).refreshChildren();
		}

		Activator.getDefault().getNavigatorViewer().refresh();
	}

	/**
	 * Returns the active workbench window's current selection if there exists
	 * an active window. The selection may be <code>null</code> or empty.
	 * <p>
	 * Using the window's <code>ISelectionService</code> this method only
	 * retrieves selections that stem from a previously set
	 * <code>ISelectionProvider</code> in any of the window's parts.
	 * </p>
	 * 
	 * @return the selection or <code>null</code> if none
	 */
	public static ISelection getActiveWindowSelection() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		return window == null ? null : window.getSelectionService().getSelection();
	}

	/**
	 * Returns an iterator over the active workbench window's current selection
	 * if there exists an active window which contains a non-empty
	 * <code>IStructuredSelection</code>.
	 * <p>
	 * Using the window's <code>ISelectionService</code> this method only
	 * retrieves selections that stem from a previously set
	 * <code>ISelectionProvider</code> in any of the window's parts.
	 * </p>
	 * 
	 * @return iterator over a non-empty selection or <code>null</code> if none
	 */
	public static Iterator<?> getActiveWindowStructuredSelectionIterator() {
		ISelection selection = getActiveWindowSelection();

		if (selection instanceof IStructuredSelection) {
			return selection.isEmpty() ? null : ((IStructuredSelection) selection).iterator();
		} else {
			return null;
		}
	}

	/**
	 * Calls the <code>open()</code> method on the given element's open handler
	 * if it has one.
	 * 
	 * @param element
	 *            the element to open
	 */
	public static void openElement(Object element) {
		IHandlerMediator mediator = toHandlerMediator(element);
		if (mediator != null) {
			Object handler = mediator.getHandler(OpenHandler.OPEN_COMMAND_ID);
			if (handler instanceof IOpenable) {
				IOpenable openable = (IOpenable) handler;
				try {
					openable.open();
				} catch (Exception e) {
					String message = String.format(ERR_MSG_OPEN, openable.getElementName());
					LOGGER.warn(message, e);
					DialogUtils.openWarning(DialogUtils.appendCause(message, e.getMessage()));
				}
			}
		}
	}

	/**
	 * Calls the <code>duplicate()</code> method on the given element's
	 * duplicate handler if it has one.
	 * 
	 * @param element
	 *            the element to duplicate
	 */
	public static void duplicateElement(Object element) {
		IHandlerMediator mediator = toHandlerMediator(element);
		if (mediator != null) {
			Object handler = mediator.getHandler(DuplicateHandler.DUPLICATE_COMMAND_ID);
			if (handler instanceof IDuplicatable) {
				IDuplicatable duplicatable = (IDuplicatable) handler;
				try {
					duplicatable.duplicate();
				} catch (Exception e) {
					String message = String.format(ERR_MSG_DUPLICATE, duplicatable.toString());
					LOGGER.error(message, e);
					DialogUtils.handleError(message, e);
				}
			}
		}
	}

	/**
	 * Calls the delete method on the given element's delete handler if it has
	 * one. Expects elements of the same type.
	 * 
	 * @param elements
	 *            the elements to delete
	 */
	public static void deleteElements(Object[] elements) {
		if (elements == null || elements.length == 0) {
			return;
		}
		IHandlerMediator mediator = toHandlerMediator(elements[0]);
		if (mediator != null) {
			Object handler = mediator.getHandler(DeleteHandler.DELETE_COMMAND_ID);
			if (handler instanceof IDeletable) {
				IDeletable deletable = (IDeletable) handler;
				if (deletable.showConfirmationDialog(elements)) {
					try {
						if (elements.length == 1) {
							deletable.delete();
						} else {
							deletable.delete(elements);
						}
					} catch (CoreException e) {
						LOGGER.error(ERR_MSG_DELETE, e);
						DialogUtils.handleError(ERR_MSG_DELETE, e);
					}
				}
			}
		}
	}

	/**
	 * Returns the object casted to a <code>IHandlerMediator</code> if
	 * supported.
	 * 
	 * @param element
	 *            the element to cast
	 * @return the object casted to <code>IHandlerMediator</code> or
	 *         <code>null</code> if not possible
	 */
	public static IHandlerMediator toHandlerMediator(Object element) {
		if (element instanceof IHandlerMediator) {
			return (IHandlerMediator) element;
		} else {
			return null;
		}
	}

	/**
	 * Extracts a concrete handler from the element's handler mediator
	 * associated with the given command id.
	 * 
	 * @param element
	 *            the element to extract from
	 * @param commandId
	 *            the command to extract
	 * @return the corresponding handler or <code>null</code>
	 */
	public static Object toConcreteHandler(Object element, String commandId) {
		IHandlerMediator mediator = toHandlerMediator(element);
		if (mediator != null) {
			Object handler = mediator.getHandler(commandId);
			if (handler != null) {
				return handler;
			}
		}
		return null;
	}

}
