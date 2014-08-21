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

public class OmitPrefixNameFormatterTest {

	private static final String FORMATTER_NAME = "prefix omitted";
	
	@Test
	public void testFormat() {
		OmitPrefixNameFormatter formatter = new OmitPrefixNameFormatter();
		
		String fullName = "org.spotter.satellite.host";
		String expectedName = "satellite.host";
		Assert.assertEquals(expectedName, formatter.format(fullName));
		
		fullName = "org.spotter.host";
		expectedName = "host";
		Assert.assertEquals(expectedName, formatter.format(fullName));
		
		fullName = "orgs.spotter.satellite.host";
		expectedName = "orgs.spotter.satellite.host";
		Assert.assertEquals(expectedName, formatter.format(fullName));
	}

	@Test
	public void testGetFormatterName() {
		OmitPrefixNameFormatter formatter = new OmitPrefixNameFormatter();
		
		Assert.assertEquals(FORMATTER_NAME, formatter.getFormatterName());
	}

	@Test
	public void testToString() {
		OmitPrefixNameFormatter formatter = new OmitPrefixNameFormatter();
		
		Assert.assertEquals(FORMATTER_NAME, formatter.toString());
	}

}