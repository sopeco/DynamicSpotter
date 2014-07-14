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

/**
 * An exception that is thrown whenever a problem arises which the UI needs to
 * recover from or handle accordingly.
 */
public class UICoreException extends Exception {

	private static final long serialVersionUID = -7528743470447912878L;

	/**
	 * Creates an empty exception.
	 */
	public UICoreException() {
	}

	/**
	 * Creates a new exception with the given message.
	 * 
	 * @param message
	 *            The exception message.
	 */
	public UICoreException(String message) {
		super(message);
	}

	/**
	 * Creates a new exception with the given message and cause.
	 * 
	 * @param message
	 *            The exception message.
	 * @param cause
	 *            The cause of this exception.
	 */
	public UICoreException(String message, Throwable cause) {
		super(message, cause);
	}

}
