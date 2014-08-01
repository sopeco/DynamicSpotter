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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.navigator.IDeletable;
import org.spotter.eclipse.ui.navigator.IDuplicatable;
import org.spotter.eclipse.ui.navigator.IOpenableProjectElement;
import org.spotter.eclipse.ui.navigator.ISpotterProjectElement;
import org.spotter.eclipse.ui.navigator.SpotterProjectParent;
import org.spotter.shared.environment.model.XMConfiguration;

/**
 * An utility class for the Spotter UI.
 * 
 * @author Denis Knoepfle
 * 
 */
public final class SpotterUtils {

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
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
		JAXBContext jaxbContext = JAXBContext.newInstance(jaxbElement.getClass());
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		// configure JAXB
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		jaxbMarshaller.setProperty("com.sun.xml.bind.xmlHeaders", XML_HEADER);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		jaxbMarshaller.marshal(jaxbElement, outputStream);
		InputStream source = new ByteArrayInputStream(outputStream.toByteArray());
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
	 * Calls the <code>open()</code> method on the given element if it is
	 * openable.
	 * 
	 * @param element
	 *            the element to open
	 */
	public static void openNavigatorElement(Object element) {
		if (element instanceof IOpenableProjectElement) {
			IOpenableProjectElement openable = (IOpenableProjectElement) element;
			try {
				openable.open();
			} catch (Exception e) {
				String msg = String.format(ERR_MSG_OPEN, openable.getText());
				LOGGER.warn(DialogUtils.appendCause(msg, e.toString()));
				DialogUtils.openWarning(DialogUtils.appendCause(msg, e.getMessage()));
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
	public static void duplicateNavigatorElement(Object element) {
		if (element instanceof IDuplicatable) {
			IDuplicatable duplicatable = (IDuplicatable) element;
			try {
				duplicatable.duplicate();
			} catch (Exception e) {
				String msg = String.format(ERR_MSG_DUPLICATE, duplicatable.toString());
				LOGGER.error(DialogUtils.appendCause(msg, e.toString()));
				DialogUtils.openError(DialogUtils.appendCause(msg, e.getMessage()));
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
	public static void deleteNavigatorElement(Object element) {
		if (element instanceof IDeletable) {
			IDeletable deletable = (IDeletable) element;
			try {
				deletable.delete();
			} catch (Exception e) {
				String msg = String.format(ERR_MSG_DELETE, deletable.toString());
				LOGGER.error(DialogUtils.appendCause(msg, e.toString()));
				DialogUtils.openError(DialogUtils.appendCause(msg, e.getMessage()));
			}
		}
	}

	/**
	 * Generates a unique 128-bit id using UUID.
	 * 
	 * @return the generated id
	 */
	public static String generateUniqueId() {
		return UUID.randomUUID().toString();
	}

}
