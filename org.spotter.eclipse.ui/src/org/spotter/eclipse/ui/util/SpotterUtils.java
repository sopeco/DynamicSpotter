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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.menu.IDeletable;
import org.spotter.eclipse.ui.menu.IDuplicatable;
import org.spotter.eclipse.ui.menu.IOpenableProjectElement;
import org.spotter.eclipse.ui.navigator.ISpotterProjectElement;
import org.spotter.eclipse.ui.navigator.SpotterProjectParent;
import org.spotter.shared.environment.model.XMConfiguration;
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
	private static final String ERR_MSG_DELETE = "Error while deleting element '%s'!";

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
		if (config != null) {
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
	 * Calls the <code>open()</code> method on the given element if it is
	 * openable.
	 * 
	 * @param element
	 *            the element to open
	 */
	public static void openElement(Object element) {
		if (element instanceof IOpenableProjectElement) {
			IOpenableProjectElement openable = (IOpenableProjectElement) element;
			try {
				openable.open();
			} catch (Exception e) {
				String message = String.format(ERR_MSG_OPEN, openable.getText());
				LOGGER.warn(message, e);
				DialogUtils.openWarning(DialogUtils.appendCause(message, e.getMessage()));
			}
		}
	}

	/**
	 * Calls the <code>duplicate()</code> method on the given element if it is
	 * duplicatable.
	 * 
	 * @param element
	 *            the element to duplicate
	 */
	public static void duplicateElement(Object element) {
		if (element instanceof IDuplicatable) {
			IDuplicatable duplicatable = (IDuplicatable) element;
			try {
				duplicatable.duplicate();
			} catch (Exception e) {
				String message = String.format(ERR_MSG_DUPLICATE, duplicatable.toString());
				LOGGER.error(message, e);
				DialogUtils.handleError(message, e);
			}
		}
	}

	/**
	 * Calls the <code>delete()</code> method on the given element if it is
	 * deletable.
	 * 
	 * @param element
	 *            the element to delete
	 */
	public static void deleteElement(Object element) {
		if (element instanceof IDeletable) {
			IDeletable deletable = (IDeletable) element;
			try {
				deletable.delete();
			} catch (Exception e) {
				String message = String.format(ERR_MSG_DELETE, deletable.toString());
				LOGGER.error(message, e);
				DialogUtils.handleError(message, e);
			}
		}
	}

}
