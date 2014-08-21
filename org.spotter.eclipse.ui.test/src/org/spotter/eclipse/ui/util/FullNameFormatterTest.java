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

import junit.framework.Assert;

import org.junit.Test;

public class FullNameFormatterTest {

	private static final String FORMATTER_NAME = "full name";
	
	@Test
	public void testFormat() {
		FullNameFormatter formatter = new FullNameFormatter();
		
		final String fullName = "org.spotter.satellite.host";
		Assert.assertEquals(fullName, formatter.format(fullName));
	}

	@Test
	public void testGetFormatterName() {
		FullNameFormatter formatter = new FullNameFormatter();
		
		Assert.assertEquals(FORMATTER_NAME, formatter.getFormatterName());
	}

	@Test
	public void testToString() {
		FullNameFormatter formatter = new FullNameFormatter();
		
		Assert.assertEquals(FORMATTER_NAME, formatter.toString());
	}

}
