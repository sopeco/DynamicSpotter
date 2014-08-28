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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.junit.Test;

public class WidgetUtilsTest {
	
	@Test
	public void testCreateGridLayout() {
		final int numColumns = 7;
		GridLayout gridLayout = WidgetUtils.createGridLayout(numColumns);
		Assert.assertEquals(false, gridLayout.makeColumnsEqualWidth);
		Assert.assertEquals(numColumns, gridLayout.numColumns);
		assertDefaultGridLayout(gridLayout);
		
		gridLayout = WidgetUtils.createGridLayout(numColumns, true);
		Assert.assertEquals(true, gridLayout.makeColumnsEqualWidth);
		Assert.assertEquals(numColumns, gridLayout.numColumns);
		assertDefaultGridLayout(gridLayout);
	}

	@Test
	public void testCreateFillLayout() {
		FillLayout fillLayout = WidgetUtils.createFillLayout(SWT.HORIZONTAL);
		Assert.assertEquals(SWT.HORIZONTAL, fillLayout.type);
		assertDefaultFillLayout(fillLayout);

		fillLayout = WidgetUtils.createFillLayout(SWT.VERTICAL);
		Assert.assertEquals(SWT.VERTICAL, fillLayout.type);
		assertDefaultFillLayout(fillLayout);
	}

	private void assertDefaultGridLayout(GridLayout gridLayout) {
		Assert.assertEquals(WidgetUtils.DEFAULT_MARGIN_WIDTH, gridLayout.marginWidth);
		Assert.assertEquals(WidgetUtils.DEFAULT_MARGIN_HEIGHT, gridLayout.marginHeight);
		Assert.assertEquals(WidgetUtils.DEFAULT_VERTICAL_SPACING, gridLayout.verticalSpacing);
		Assert.assertEquals(WidgetUtils.DEFAULT_HORIZONTAL_SPACING, gridLayout.horizontalSpacing);
		Assert.assertEquals(0, gridLayout.marginBottom);
		Assert.assertEquals(0, gridLayout.marginTop);
		Assert.assertEquals(0, gridLayout.marginLeft);
		Assert.assertEquals(0, gridLayout.marginRight);
	}

	private void assertDefaultFillLayout(FillLayout fillLayout) {
		Assert.assertEquals(WidgetUtils.DEFAULT_MARGIN_WIDTH, fillLayout.marginWidth);
		Assert.assertEquals(WidgetUtils.DEFAULT_MARGIN_HEIGHT, fillLayout.marginHeight);
		Assert.assertEquals(WidgetUtils.DEFAULT_VERTICAL_SPACING, fillLayout.spacing);
	}

}
