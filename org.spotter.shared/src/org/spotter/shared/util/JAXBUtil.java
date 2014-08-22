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
package org.spotter.shared.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * A utility class providing methods to write to and read from XML files using
 * JAXB.
 * 
 * @author Denis Knoepfle
 * 
 */
public final class JAXBUtil {

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private JAXBUtil() {
	}

	/**
	 * Writes the given JAXBElement to the specified file. If the file does not
	 * exist yet, it will be created.
	 * 
	 * @param file
	 *            the destination file
	 * @param jaxbElement
	 *            the JAXB element to marshal
	 * @throws JAXBException
	 *             if an error with JAXB occurs
	 */
	public static void writeElementToFile(File file, Object jaxbElement) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(jaxbElement.getClass());
		Marshaller jaxbMarshaller = createJAXBMarshaller(jaxbContext);

		jaxbMarshaller.marshal(jaxbElement, file);
	}

	/**
	 * Creates an input stream from the given JAXB element. First the element is
	 * marshaled and then the resulting output stream is handed over to an input
	 * stream.
	 * 
	 * @param jaxbElement
	 *            the JAXB element to marshal
	 * @return the resulting input stream
	 * @throws JAXBException
	 *             if an error with JAXB occurs
	 */
	public static InputStream createInputStreamFromElement(Object jaxbElement) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(jaxbElement.getClass());
		Marshaller jaxbMarshaller = createJAXBMarshaller(jaxbContext);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		jaxbMarshaller.marshal(jaxbElement, outputStream);
		InputStream source = new ByteArrayInputStream(outputStream.toByteArray());

		return source;
	}

	private static Marshaller createJAXBMarshaller(JAXBContext jaxbContext) throws JAXBException {
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		// configure JAXB
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		jaxbMarshaller.setProperty("com.sun.xml.bind.xmlHeaders", XML_HEADER);

		return jaxbMarshaller;
	}

	/**
	 * Parses the given XML file. Under the provided context path an object
	 * factory for JAXB is expected. Thus the context path is most likely
	 * <code>ObjectFactory.class.getPackage().getName()</code> whereas the
	 * <code>ObjectFactory</code> must reside in the same package as the
	 * expected return type in this case.
	 * 
	 * @param <T>
	 *            the expected return type
	 * @param fileName
	 *            the name of the file to parse
	 * @param contextPath
	 *            the context path for JAXB
	 * @return the root of the parsed document as the type specified
	 * @throws FileNotFoundException
	 *             if file not found
	 * @throws JAXBException
	 *             if parsing failed
	 */
	public static <T> T parseXMLFile(String fileName, String contextPath) throws FileNotFoundException, JAXBException {
		FileReader fileReader = new FileReader(fileName);
		JAXBContext jc = JAXBContext.newInstance(contextPath);
		Unmarshaller u = jc.createUnmarshaller();

		@SuppressWarnings("unchecked")
		T xRoot = ((JAXBElement<T>) u.unmarshal(fileReader)).getValue();

		return xRoot;
	}

}
