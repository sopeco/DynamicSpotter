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
package org.spotter.demo.app.problems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Garbage B.
 * 
 * @author Alexander Wert
 * 
 */
public class GarbageObjectB {
	private static final int NUM_ELEMENTS_PER_LIST = 10;
	private static final int NUM_LISTS = 50;
	List<String> stringList = new ArrayList<>();
	Map<String, List<String>> aMap = new HashMap<String, List<String>>();

	/**
	 * Constructor.
	 */
	public GarbageObjectB() {
		for (int i = 0; i < NUM_LISTS; i++) {
			stringList.add(UUID.randomUUID().toString());
			List<String> anotherList = new ArrayList<>();
			for (int j = 0; j < NUM_ELEMENTS_PER_LIST; j++) {
				anotherList.add(UUID.randomUUID().toString());
			}
			aMap.put(UUID.randomUUID().toString(), anotherList);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		for (String s : aMap.keySet()) {
			aMap.get(s).clear();
			if (s.startsWith("a")) {
				break;
			}
		}
	}
}
