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

public class DialogUtilsTest {
	
	@Test
	public void testAppendCause() {
		String message = null;
		Assert.assertNull(DialogUtils.appendCause(null, ""));
		
		message = "Some message.";
		final String cause = "Error xyz occured!";
		String expected = message + " " + "Cause: " + cause;
		Assert.assertEquals(expected, DialogUtils.appendCause(message, cause, false));
		
		expected = message + "\n\n" + "Cause: " + cause;
		Assert.assertEquals(expected, DialogUtils.appendCause(message, cause, true));
	}

}
