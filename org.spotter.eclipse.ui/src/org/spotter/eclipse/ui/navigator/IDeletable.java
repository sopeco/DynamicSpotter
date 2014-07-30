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
package org.spotter.eclipse.ui.navigator;

/**
 * An interface for elements of the Spotter Project Navigator that can be
 * deleted.
 * 
 * @author Denis Knoepfle
 * 
 */
public interface IDeletable {

	/**
	 * Deletes this element.
	 */
	void delete();

	/**
	 * Returns the name of this element type that should be used within the
	 * label for the delete command.
	 * 
	 * @return The name of this element type
	 */
	String getElementTypeName();

}
