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
package org.spotter.eclipse.ui.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A view to display the progress of the current diagnosis run of
 * DynamicSpotter.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ActiveRunView extends ViewPart {

	public static final String VIEW_ID = "org.spotter.eclipse.ui.view.activeRunView";

	private static final Logger LOGGER = LoggerFactory.getLogger(ActiveRunView.class);

	private static final String ACTIVE_RUN_VIEW_TITLE = "Active Run";
	private static final String ACTIVE_RUN_EMPTY_CONTENT_DESC = "No active run.";
	private static final String ACTIVE_RUN_CONTENT_DESC_TEMPLATE = "DynamicSpotter diagnosis of project '%s'";

	/**
	 * The constructor.
	 */
	public ActiveRunView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		setPartName(ACTIVE_RUN_VIEW_TITLE);
		setContentDescription(ACTIVE_RUN_EMPTY_CONTENT_DESC);

		// ensure that the parent's layout is a FillLayout
		if (!(parent.getLayout() instanceof FillLayout)) {
			parent.setLayout(new FillLayout());
		}

		Label label = new Label(parent, SWT.NONE);
		label.setText("Currently no running diagnosis.");
	}

	@Override
	public void setFocus() {
		// TODO: give focus to main control
	}

}
