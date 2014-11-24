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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.util.DialogUtils;

/**
 * A handler for the clear cache command which clears the cache containing data
 * retrieved from the DS service.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ClearCacheHandler extends AbstractHandler implements ISelectionChangedListener {

	/**
	 * The id of the corresponding clear cache command.
	 */
	public static final String CLEAR_CACHE_COMMAND_ID = "org.spotter.eclipse.ui.commands.clearCache";

	private boolean isEnabled;

	/**
	 * Constructor.
	 */
	public ClearCacheHandler() {
		super();
		selectionChanged(null);
		Activator.getDefault().addProjectSelectionListener(this);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator activator = Activator.getDefault();
		for (IProject project : activator.getSelectedProjects()) {
			String projectName = project.getName();
			activator.getClient(projectName).clearCache();
		}

		DialogUtils.openInformation("Cleared the data cache! Be aware that already "
				+ "configured extensions may get into an invalid state in case the "
				+ "extensions change on the server side.");

		return null;
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public void dispose() {
		Activator.getDefault().removeProjectSelectionListener(this);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		this.isEnabled = !Activator.getDefault().getSelectedProjects().isEmpty();
	}

}
