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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.spotter.shared.environment.model.XMConfiguration;

/**
 * An utility class for the Spotter UI.
 * 
 * @author Denis Knoepfle
 * 
 */
public final class SpotterUtils {

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

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
	 * @throws CoreException
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

}
