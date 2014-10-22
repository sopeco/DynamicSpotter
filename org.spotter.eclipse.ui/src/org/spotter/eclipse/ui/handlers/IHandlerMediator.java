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
package org.spotter.eclipse.ui.handlers;

/**
 * An interface for a handler mediator which manages an object's available
 * handlers.
 * 
 * @author Denis Knoepfle
 * 
 */
public interface IHandlerMediator {

	/**
	 * Returns whether the given command can be handled.
	 * 
	 * @param commandId
	 *            the id of the command to check for an existing handler
	 * @return <code>true</code> if the given command can be handled,
	 *         <code>false</code> otherwise
	 */
	boolean canHandle(String commandId);

	/**
	 * Returns a handler for the command specified by the given id.
	 * 
	 * @param commandId
	 *            the id of the command to return the handler for
	 * @return the handler associated with the given id
	 */
	Object getHandler(String commandId);

	/**
	 * Add the given handler for the specified command id.
	 * 
	 * @param commandId
	 *            the id the handler refers to
	 * @param handler
	 *            the handler to add
	 */
	void addHandler(String commandId, Object handler);

	/**
	 * Removes the handler that is associated with the given command id.
	 * 
	 * @param commandId
	 *            the id whose corresponding handler will be removed
	 */
	void removeHandler(String commandId);

}
