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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public final class DialogUtils {

	public static final String DEFAULT_DLG_TITLE = "Spotter";
	
	private static final String MSG_NO_FURTHER_INFO = "No further information provided.";

	private DialogUtils() {
	}

	public static void errorMessage(String message, String detailMessage) {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		MessageDialog.openError(shell, DEFAULT_DLG_TITLE,
				String.format(message, detailMessage == null ? MSG_NO_FURTHER_INFO : detailMessage));
	}
	
	public static void warningMessage(String message) {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		MessageDialog.openWarning(shell, DEFAULT_DLG_TITLE, message);
	}

}
