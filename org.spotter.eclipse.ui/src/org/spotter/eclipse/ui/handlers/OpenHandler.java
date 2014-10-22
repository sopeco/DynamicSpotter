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
package org.spotter.eclipse.ui.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.spotter.eclipse.ui.menu.IOpenable;
import org.spotter.eclipse.ui.util.SpotterUtils;

/**
 * An open handler for the open command which opens the selected elements.
 * 
 * @author Denis Knoepfle
 * 
 */
public class OpenHandler extends AbstractHandler {

	/**
	 * The id of the open command.
	 */
	public static final String OPEN_COMMAND_ID = "org.spotter.eclipse.ui.commands.open";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Iterator<?> iter = SpotterUtils.getActiveWindowStructuredSelectionIterator();
		if (iter == null) {
			return null;
		}

		while (iter.hasNext()) {
			SpotterUtils.openElement(iter.next());
		}

		return null;
	}

	/**
	 * Returns <code>true</code> if only elements are selected that are
	 * openable.
	 * 
	 * @return <code>true</code> if only elements are selected that are openable
	 */
	@Override
	public boolean isEnabled() {
		Iterator<?> iter = SpotterUtils.getActiveWindowStructuredSelectionIterator();
		if (iter == null) {
			return false;
		}

		while (iter.hasNext()) {
			if (!(iter.next() instanceof IOpenable)) {
				return false;
			}
		}

		return true;
	}

}
