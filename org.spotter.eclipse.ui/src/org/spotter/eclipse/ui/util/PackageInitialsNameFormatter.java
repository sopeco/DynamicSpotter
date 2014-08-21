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
 * A string formatter that returns the name whereas package names within the name are replaced by
 * their initials.
 * 
 * @author Denis Knoepfle
 * 
 */
public class PackageInitialsNameFormatter extends FullNameFormatter {

	private static final String FORMATTER_NAME = "package initials";
	private static final String PACKAGE_SEPARATOR = ".";
	private static final String PACKAGE_SEPARATOR_REGEX = "\\" + PACKAGE_SEPARATOR;

	@Override
	public String format(String name) {
		String[] parts = name.split(PACKAGE_SEPARATOR_REGEX);
		if (parts.length == 0) {
			return name;
		}
		if (parts.length == 1 && name.endsWith(PACKAGE_SEPARATOR)) {
			return parts[0].charAt(0) + PACKAGE_SEPARATOR;
		}
		StringBuilder sb = new StringBuilder();
		int countPackages = parts.length - 1;
		for (int i = 0; i < countPackages; ++i) {
			sb.append(parts[i].charAt(0) + PACKAGE_SEPARATOR);
		}
		sb.append(parts[countPackages]);
		return sb.toString();
	}

	@Override
	public String getFormatterName() {
		return FORMATTER_NAME;
	}

}
