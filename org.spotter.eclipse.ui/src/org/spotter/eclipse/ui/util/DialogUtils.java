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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * An utility class for showing different kinds of message dialogs in the
 * DynamicSpotter UI.
 * <p>
 * The methods to display dialogs are thread-access safe regarding the problem
 * that only the UI thread is allowed to access and manipulate SWT components.
 * </p>
 * 
 * @author Denis Knoepfle
 * 
 */
public final class DialogUtils {

	public static final String DEFAULT_DLG_TITLE = "DynamicSpotter";

	private DialogUtils() {
	}

	/**
	 * Returns the message string with the appended cause. If cause was
	 * <code>null</code> or empty the message is returned unchanged.
	 * 
	 * @param message
	 *            The message to append to.
	 * @param cause
	 *            The cause to append. May be <code>null</code>.
	 * @return the message appended by the cause
	 */
	public static String appendCause(String message, String cause) {
		return appendCause(message, cause, false);
	}

	/**
	 * Returns the message string with the appended cause. If cause was
	 * <code>null</code> or empty the message is returned unchanged.
	 * 
	 * @param message
	 *            The message to append to.
	 * @param cause
	 *            The cause to append. May be <code>null</code>.
	 * @param insertBlankLine
	 *            <code>true</code> to insert a blank line before the cause.
	 * @return the message appended by the cause
	 */
	public static String appendCause(String message, String cause, boolean insertBlankLine) {
		if (cause == null || cause.isEmpty()) {
			return message;
		}
		String blank = insertBlankLine ? "\n\n" : " ";
		String formattedCause = blank + "Cause: " + cause;
		return message.concat(formattedCause);
	}

	/**
	 * Returns the display of the workbench. Can be called from any thread.
	 * 
	 * @return The display of the workbench.
	 */
	public static Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}

	/**
	 * Returns the shell of the active window in the workbench. Must be called
	 * from the UI thread, otherwise returns <code>null</code>.
	 * 
	 * @return The active shell or <code>null</code> if none available.
	 */
	public static Shell getShell() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		return window != null ? window.getShell() : null;
	}

	/**
	 * Opens an information dialog with the given title and message. It is
	 * ensured that the dialog is opened on the UI thread.
	 * 
	 * @param title
	 *            The title of the dialog
	 * @param message
	 *            The message of the dialog
	 */
	public static void openInformation(final String title, final String message) {
		Display display = getDisplay();
		// check if operating on UI thread
		if (Thread.currentThread() == display.getThread()) {
			// already executing on the UI thread
			MessageDialog.openInformation(getShell(), title, message);
		} else {
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(getShell(), title, message);
				}
			});
		}
	}

	/**
	 * Opens an information dialog with the given message and a default title.
	 * It is ensured that the dialog is opened on the UI thread.
	 * 
	 * @param message
	 *            The message of the dialog
	 */
	public static void openInformation(final String message) {
		openInformation(DEFAULT_DLG_TITLE, message);
	}

	/**
	 * Opens a warning dialog with the given title and message. It is ensured
	 * that the dialog is opened on the UI thread.
	 * 
	 * @param title
	 *            The title of the dialog
	 * @param message
	 *            The message of the dialog
	 */
	public static void openWarning(final String title, final String message) {
		Display display = getDisplay();
		// check if operating on UI thread
		if (Thread.currentThread() == display.getThread()) {
			// already executing on the UI thread
			MessageDialog.openWarning(getShell(), title, message);
		} else {
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(getShell(), title, message);
				}
			});
		}
	}

	/**
	 * Opens a warning dialog with the given message and a default title. It is
	 * ensured that the dialog is opened on the UI thread.
	 * 
	 * @param message
	 *            The message of the dialog
	 */
	public static void openWarning(final String message) {
		openWarning(DEFAULT_DLG_TITLE, message);
	}

	/**
	 * Opens an error dialog with the given title and message. It is ensured
	 * that the dialog is opened on the UI thread.
	 * 
	 * @param title
	 *            The title of the dialog
	 * @param message
	 *            The message of the dialog
	 */
	public static void openError(final String title, final String message) {
		Display display = getDisplay();
		// check if operating on UI thread
		if (Thread.currentThread() == display.getThread()) {
			// already executing on the UI thread
			MessageDialog.openError(getShell(), title, message);
		} else {
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(getShell(), title, message);
				}
			});
		}
	}

	/**
	 * Opens an error dialog with the given message and a default title. It is
	 * ensured that the dialog is opened on the UI thread.
	 * 
	 * @param message
	 *            The message of the dialog
	 */
	public static void openError(final String message) {
		openError(DEFAULT_DLG_TITLE, message);
	}

}
