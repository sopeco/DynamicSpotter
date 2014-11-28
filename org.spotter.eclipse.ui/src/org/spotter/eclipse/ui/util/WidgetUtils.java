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

import org.eclipse.swt.SWTException;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An utility class for widgets and layouts in the DynamicSpotter Eclipse UI.
 * 
 * @author Denis Knoepfle
 * 
 */
public final class WidgetUtils {

	public static final int DEFAULT_MARGIN_WIDTH = 10;
	public static final int DEFAULT_MARGIN_HEIGHT = 10;
	public static final int DEFAULT_VERTICAL_SPACING = 6;
	public static final int DEFAULT_HORIZONTAL_SPACING = 6;

	private static final Logger LOGGER = LoggerFactory.getLogger(WidgetUtils.class);

	private WidgetUtils() {
	}

	/**
	 * Creates a GridLayout with default settings for margin width, margin
	 * height, vertical spacing and horizontal spacing for the Spotter UI.
	 * Columns do not necessarily have the same width.
	 * 
	 * @param columns
	 *            the number of columns
	 * @return the grid layout
	 */
	public static GridLayout createGridLayout(int columns) {
		return createGridLayout(columns, false);
	}

	/**
	 * Creates a GridLayout with default settings for margin width, margin
	 * height, vertical spacing and horizontal spacing for the DynamicSpotter
	 * Eclipse UI.
	 * 
	 * @param columns
	 *            the number of columns
	 * @param makeColumnsEqualWidth
	 *            whether the columns have equal width
	 * @return the grid layout
	 */
	public static GridLayout createGridLayout(int columns, boolean makeColumnsEqualWidth) {
		GridLayout gridLayout = new GridLayout(columns, makeColumnsEqualWidth);

		gridLayout.marginWidth = DEFAULT_MARGIN_WIDTH;
		gridLayout.marginHeight = DEFAULT_MARGIN_HEIGHT;
		gridLayout.verticalSpacing = DEFAULT_VERTICAL_SPACING;
		gridLayout.horizontalSpacing = DEFAULT_HORIZONTAL_SPACING;

		return gridLayout;
	}

	/**
	 * Creates a FillLayout with default settings for margin and spacing for the
	 * DynamicSpotter Eclipse UI.
	 * 
	 * @param type
	 *            the type of the layout. May be <code>SWT.HORIZONTAL</code> or
	 *            <code>SWT.VERTICAL</code>.
	 * @return the fill layout
	 */
	public static FillLayout createFillLayout(int type) {
		FillLayout fillLayout = new FillLayout(type);
		fillLayout.marginWidth = DEFAULT_MARGIN_WIDTH;
		fillLayout.marginHeight = DEFAULT_MARGIN_HEIGHT;
		fillLayout.spacing = DEFAULT_VERTICAL_SPACING;

		return fillLayout;
	}

	/**
	 * Causes the <code>run()</code> method of the runnable to be invoked
	 * synchronously on the UI-thread using the main control's display but only
	 * if neither the main control nor the attached display are disposed. Any
	 * SWTException in the runnable will terminate the runnable but further will
	 * only be logged.
	 * 
	 * @param controlWidget
	 *            the main control widget whose display will be used
	 * @param runnable
	 *            the runnable to invoke
	 */
	public static void submitSyncExecIgnoreDisposed(final Widget controlWidget, final Runnable runnable) {
		final Display display = controlWidget != null ? controlWidget.getDisplay() : null;
		Runnable safeRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					boolean isControlValid = controlWidget != null && !controlWidget.isDisposed();
					boolean isDisplayValid = !display.isDisposed();

					boolean isSafeToRun = isDisplayValid && isControlValid;

					if (isSafeToRun) {
						runnable.run();
					}
				} catch (SWTException e) {
					LOGGER.debug("runnable terminated due to SWTException", e);
				}
			}
		};
		if (display != null && !display.isDisposed()) {
			display.syncExec(safeRunnable);
		}
	}

}
