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

import org.eclipse.jface.dialogs.MessageDialog;

public final class DialogUtils {

	private static final String DEFAULT_TITLE_ERR_DIALOG = "Spotter";
	private static final String MSG_NO_FURTHER_INFO = "No further information provided.";

	private DialogUtils() {
	}

	public static void errorMessage(String message) {
		errorMessage(message, null);
	}
	
	public static void errorMessage(String message, String detailMessage) {
		MessageDialog.openError(null, DEFAULT_TITLE_ERR_DIALOG,
				String.format(message, detailMessage == null ? MSG_NO_FURTHER_INFO : detailMessage));
	}

}
