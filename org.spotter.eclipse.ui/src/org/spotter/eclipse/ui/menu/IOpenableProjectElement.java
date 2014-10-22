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
package org.spotter.eclipse.ui.menu;

import org.spotter.eclipse.ui.navigator.ISpotterProjectElement;

/**
 * An extension interface for elements of the DynamicSpotter Project Navigator
 * that can be opened.
 * 
 * @author Denis Knoepfle
 * 
 */
public interface IOpenableProjectElement extends ISpotterProjectElement {

	/**
	 * Opens this element in an appropriate view.
	 */
	void open();

	/**
	 * Returns the id of the editor or view that is necessary to open this
	 * element.
	 * 
	 * @return the necessary editor or view id
	 */
	String getOpenId();

}
