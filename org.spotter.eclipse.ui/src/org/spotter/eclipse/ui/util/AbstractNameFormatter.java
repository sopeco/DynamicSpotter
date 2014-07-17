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

/**
 * An abstract string formatter that formats a given name. This base implementation does no
 * modifications on the given input. Subclasses must implement {@link #getFormatterName} and may
 * override {@link #format(String)}.
 * 
 * @author Denis Knoepfle
 * 
 */
public abstract class AbstractNameFormatter {

	/**
	 * Formats the given name and returns it. Subclasses should override this method and return
	 * their formatted name. This base implementation just returns the given name unchanged.
	 * 
	 * @param name
	 *            the original name
	 * @return the formatted name
	 */
	public String format(String name) {
		return name;
	}

	/**
	 * @return the name of this formatter
	 */
	public abstract String getFormatterName();

	@Override
	public String toString() {
		return getFormatterName();
	}

}
