package org.spotter.shared.result.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A problem occurrence stores the root cause location of a problem and
 * additional resources. It may contain further information.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ProblemOccurrence {

	private final List<String> resourceFiles;
	private final Object rootCauseLocation;
	private String message;

	/**
	 * Creates a new occurrence with a root cause location and an optional
	 * message.
	 * 
	 * @param rootCauseLocation
	 *            An object representing the root cause location for this
	 *            occurrence. This might be of any kind e.g. a method, a class
	 *            or a stack trace element. Must not be <code>null</code>.
	 * @param message
	 *            An optional message describing the occurrence.
	 */
	public ProblemOccurrence(Object rootCauseLocation, String message) {
		if (rootCauseLocation == null) {
			throw new IllegalArgumentException("the root cause location must not be null");
		}
		this.resourceFiles = new ArrayList<>();
		this.rootCauseLocation = rootCauseLocation;
		this.message = message;
	}

	/**
	 * Creates a new occurrence with a root cause location and an empty message.
	 * 
	 * @param rootCauseLocation
	 *            An object representing the root cause location for this
	 *            occurrence. This might be of any kind e.g. a method, a class
	 *            or a stack trace element.
	 */
	public ProblemOccurrence(Object rootCauseLocation) {
		this(rootCauseLocation, "");
	}

	/**
	 * Adds the path to an additional resource.
	 * 
	 * @param pathToFile
	 *            path to file
	 */
	public void addResourceFile(String pathToFile) {
		getResourceFiles().add(pathToFile);
	}

	/**
	 * @return the resourceFiles
	 */
	public List<String> getResourceFiles() {
		return resourceFiles;
	}

	/**
	 * @return the root cause location
	 */
	public Object getRootCauseLocation() {
		return rootCauseLocation;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 * 
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
