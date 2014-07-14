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
 * A string formatter that returns the name with the common package name "org.spotter." omitted.
 */
public class OmitPrefixNameFormatter extends AbstractNameFormatter {

	/**
	 * This prefix will be omitted in the name.
	 */
	public static final String COMMON_PACKAGE_NAME = "org.spotter.";

	private static final String FORMATTER_NAME = "prefix omitted";

	@Override
	public String format(String name) {
		if (name == null) {
			return null;
		}
		int indexOf = name.indexOf(COMMON_PACKAGE_NAME);
		if (indexOf == -1) {
			return name;
		}
		int beginIndex = indexOf + COMMON_PACKAGE_NAME.length();
		return name.substring(beginIndex);
	}

	@Override
	public String getFormatterName() {
		return FORMATTER_NAME;
	}

}
