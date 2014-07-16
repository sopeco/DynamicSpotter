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
package org.spotter.eclipse.ui.navigator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * An implementation of a viewer comparator that sorts the elements in a
 * predetermined order. Elements not included in this predefinition are sorted
 * using the default comparator, thus are sorted in lexicographical order.
 * 
 * @author Denis Knoepfle
 * 
 */
public class FixedOrderViewerComparator extends ViewerComparator {

	// The order in which the elements appear in the navigator viewer. Elements
	// which are not listed here receive the default category id 0 and are
	// sorted using the default comparator (lexicographically).
	private static final Class<?>[] ELEMENT_ORDER = { SpotterProjectParent.class, SpotterProjectConfig.class,
			SpotterProjectHierarchy.class, SpotterProjectResults.class, SpotterProjectConfigFile.class,
			SpotterProjectConfigInstrumentation.class, SpotterProjectConfigMeasurement.class,
			SpotterProjectConfigWorkload.class };

	private static final Map<String, Integer> CATEGORY_MAP = initCategories();

	@Override
	public int category(Object element) {
		Integer cat = CATEGORY_MAP.get(element.getClass().getName());
		return cat == null ? 0 : cat;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int cat1 = category(e1);
		int cat2 = category(e2);

		if (cat1 != cat2) {
			return cat1 - cat2;
		}

		String name1 = getLabel(viewer, e1);
		String name2 = getLabel(viewer, e2);

		// use the comparator to compare the strings
		return getComparator().compare(name1, name2);
	}

	private String getLabel(Viewer viewer, Object e1) {
		String name1;
		if (viewer == null || !(viewer instanceof ContentViewer)) {
			name1 = e1.toString();
		} else {
			IBaseLabelProvider prov = ((ContentViewer) viewer).getLabelProvider();
			if (prov instanceof ILabelProvider) {
				ILabelProvider lprov = (ILabelProvider) prov;
				// TODO: convert between timestamp and nice date representation
				// for SpotterProjectRunResult elements
				name1 = lprov.getText(e1);
			} else {
				name1 = e1.toString();
			}
		}
		if (name1 == null) {
			name1 = "";//$NON-NLS-1$
		}
		return name1;
	}

	private static Map<String, Integer> initCategories() {
		Map<String, Integer> map = new HashMap<>();
		int category = 0;
		for (Class<?> clazz : ELEMENT_ORDER) {
			map.put(clazz.getName(), category++);
		}

		return map;
	}

}
