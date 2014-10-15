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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.lpe.common.util.LpeFileUtils;
import org.lpe.common.util.LpeStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.jobs.JobsContainer;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.shared.configuration.FileManager;

/**
 * An element that represents the results node.
 * 
 * @author Denis Knoepfle
 * 
 */
public class SpotterProjectResults implements ISpotterProjectElement {

	public static final String IMAGE_PATH = "icons/results.gif"; //$NON-NLS-1$

	private static final Logger LOGGER = LoggerFactory.getLogger(SpotterProjectResults.class);

	private static final String ELEMENT_NAME = "Results";
	private static final String EMPTY_SUFFIX = " (empty)";
	private static final String LOADING_SUFFIX = " (loading...)";

	private ISpotterProjectElement parent;
	private ISpotterProjectElement[] children;
	private Image image;
	private boolean initialLoad;

	/**
	 * Creates a new instance of this element.
	 * 
	 * @param parent
	 *            the parent element
	 */
	public SpotterProjectResults(ISpotterProjectElement parent) {
		this.parent = parent;
		this.initialLoad = false;
	}

	@Override
	public String getText() {
		String suffix;
		if (children == null) {
			initialDeferredLoad();
			suffix = LOADING_SUFFIX;
		} else {
			suffix = hasChildren() ? "" : EMPTY_SUFFIX;
		}
		return ELEMENT_NAME + suffix;
	}

	@Override
	public Image getImage() {
		if (image == null) {
			image = Activator.getImage(IMAGE_PATH);
		}

		return image;
	}

	@Override
	public ISpotterProjectElement[] getChildren() {
		if (children == null) {
			initialDeferredLoad();
			return SpotterProjectParent.NO_CHILDREN;
		}
		// else the children are just fine

		return children;
	}

	@Override
	public boolean hasChildren() {
		if (children == null) {
			initialDeferredLoad();
			return false;
		}
		// else we have already initialized them

		return children.length > 0;
	}

	@Override
	public Object getParent() {
		return parent;
	}

	@Override
	public IProject getProject() {
		return parent.getProject();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SpotterProjectResults)) {
			return false;
		}
		SpotterProjectResults other = (SpotterProjectResults) obj;
		return getProject().equals(other.getProject());
	}

	@Override
	public int hashCode() {
		return getProject().getName().hashCode();
	}

	/**
	 * Recreates the children nodes.
	 */
	public void refreshChildren() {
		children = initializeChildren(getProject());
	}

	private synchronized void initialDeferredLoad() {
		if (initialLoad) {
			return;
		}
		initialLoad = true;
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				refreshChildren();
				Activator.getDefault().getNavigatorViewer().refresh(SpotterProjectResults.this);
			}
		});
	}

	private ISpotterProjectElement[] initializeChildren(IProject iProject) {
		IFolder resDir = iProject.getFolder(FileManager.DEFAULT_RESULTS_DIR_NAME);

		if (!resDir.isSynchronized(IResource.DEPTH_INFINITE)) {
			try {
				resDir.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				String msg = "Failed to refresh results directory.";
				LOGGER.warn(msg);
				DialogUtils.openWarning(msg);
				return SpotterProjectParent.NO_CHILDREN;
			}
		}

		return synchronizeRunResults(iProject, resDir.getLocation().toString());
	}

	private ISpotterProjectElement[] synchronizeRunResults(IProject iProject, String resultsLocation) {
		File res = new File(resultsLocation);
		List<ISpotterProjectElement> elements = new ArrayList<>();
		ServiceClientWrapper client = Activator.getDefault().getClient(iProject.getName());

		if (!res.isDirectory()) {
			DialogUtils.openWarning("The project's '" + FileManager.DEFAULT_RESULTS_DIR_NAME
					+ "' folder is missing or corrupted!");
		} else {
			boolean connected = client.testConnection(false);
			if (!connected) {
				DialogUtils
						.openAsyncWarning("No connection to DS service! New results cannot be fetched from the server.");
			}
			JobsContainer jobsContainer = JobsContainer.readJobsContainer(iProject);

			for (Long jobId : jobsContainer.getJobIds()) {
				SpotterProjectRunResult runResult = processJobId(jobId, connected, client, jobsContainer,
						resultsLocation, iProject);
				if (runResult != null) {
					elements.add(runResult);
				}
			}
		}

		return elements.toArray(new ISpotterProjectElement[elements.size()]);
	}

	private SpotterProjectRunResult processJobId(Long jobId, boolean connected, ServiceClientWrapper client,
			JobsContainer jobsContainer, String resultsLocation, IProject iProject) {
		if (connected && client.isRunning(true) && jobId.equals(client.getCurrentJobId())) {
			LOGGER.debug("Ignore job " + jobId + " because it is currently running");
			return null;
		}

		Long timestamp = jobsContainer.getTimestamp(jobId);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd_HH-mm-ss-SSS");
		String formattedTimestamp = dateFormat.format(new Date(timestamp));

		String fileName = resultsLocation + "/" + formattedTimestamp;
		File file = new File(fileName);
		boolean success = file.exists();
		if (!success && connected) {
			// try to fetch data from server
			InputStream resultsZipStream = client.requestResults(jobId.toString());
			if (resultsZipStream != null && file.mkdir()) {
				String zipFileName = fileName + "/" + jobId + ".zip";

				try {
					FileOutputStream fos = new FileOutputStream(zipFileName);
					LpeStreamUtils.pipe(resultsZipStream, fos);

					resultsZipStream.close();

					File zipFile = new File(zipFileName);
					LpeFileUtils.unzip(zipFile, new File(fileName));
					zipFile.delete();
					success = true;
				} catch (Exception e) {
					String msg = "Error while saving fetched results for job " + jobId + "!";
					DialogUtils.openError(msg);
					LOGGER.error(msg + " Cause: {}", e.toString());
				}
			}
		}

		if (success) {
			IFolder runResultFolder = iProject.getFolder(FileManager.DEFAULT_RESULTS_DIR_NAME + "/"
					+ formattedTimestamp);
			return new SpotterProjectRunResult(this, jobId, timestamp, runResultFolder);
		} else {
			return null;
		}
	}

}
