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
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
public class SpotterProjectResults extends AbstractProjectElement {

	public static final String IMAGE_PATH = "icons/results.gif"; //$NON-NLS-1$

	private static final Logger LOGGER = LoggerFactory.getLogger(SpotterProjectResults.class);

	private static final String ELEMENT_NAME = "Results";
	private static final String EMPTY_SUFFIX = " (empty)";
	private static final String PENDING_SUFFIX = " (pending...)";

	private ISpotterProjectElement parent;
	private boolean initialLoad;
	private final Map<Long, SpotterProjectRunResult> jobIdToResultMapping;

	/**
	 * Creates a new instance of this element.
	 * 
	 * @param parent
	 *            the parent element
	 */
	public SpotterProjectResults(ISpotterProjectElement parent) {
		super(IMAGE_PATH);
		this.parent = parent;
		this.initialLoad = false;
		this.jobIdToResultMapping = new HashMap<>();
	}

	@Override
	public String getText() {
		String suffix;
		if (children == null) {
			initialDeferredLoad();
			suffix = PENDING_SUFFIX;
		} else {
			suffix = hasChildren() ? "" : EMPTY_SUFFIX;
		}
		return ELEMENT_NAME + suffix;
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

	/**
	 * Returns the corresponding run result item for the given job id.
	 * 
	 * @param jobId
	 *            the job id the requested item refers to
	 * @return the item corresponding to the given job id
	 */
	public SpotterProjectRunResult getRunResultForJobId(long jobId) {
		return jobIdToResultMapping.get(jobId);
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

	@Override
	protected ISpotterProjectElement[] initializeChildren(IProject iProject) {
		IFolder resDir = iProject.getFolder(FileManager.DEFAULT_RESULTS_DIR_NAME);

		if (!resDir.isSynchronized(IResource.DEPTH_INFINITE)) {
			try {
				resDir.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				String message = "Failed to refresh results directory. Cannot proceed to create child nodes!";
				DialogUtils.handleError(message, e);
				return SpotterProjectParent.NO_CHILDREN;
			}
		}

		return synchronizeRunResults(iProject, resDir.getLocation().toString());
	}

	private ISpotterProjectElement[] synchronizeRunResults(IProject iProject, String resultsLocation) {
		jobIdToResultMapping.clear();
		File res = new File(resultsLocation);
		List<ISpotterProjectElement> elements = new ArrayList<>();
		ServiceClientWrapper client = Activator.getDefault().getClient(iProject.getName());

		if (!res.isDirectory()) {
			DialogUtils.openWarning("The project's '" + FileManager.DEFAULT_RESULTS_DIR_NAME
					+ "' folder is missing or corrupted!");
		} else {
			boolean connected = client.testConnection(false);
			JobsContainer jobsContainer = JobsContainer.readJobsContainer(iProject);

			Long[] jobIds = jobsContainer.getJobIds();
			if (!connected && jobIds.length > 0) {
				DialogUtils
						.openAsyncWarning("No connection to DS service! New results cannot be fetched from the server.");
			}

			for (Long jobId : jobIds) {
				SpotterProjectRunResult runResult = processJobId(jobId, connected, client, jobsContainer,
						resultsLocation, iProject);
				if (runResult != null) {
					elements.add(runResult);
					jobIdToResultMapping.put(jobId, runResult);
				}
			}
		}

		return elements.toArray(new ISpotterProjectElement[elements.size()]);
	}

	private SpotterProjectRunResult processJobId(Long jobId, boolean connected, ServiceClientWrapper client,
			JobsContainer jobsContainer, String resultsLocation, IProject project) {
		if (connected && client.isRunning(true) && jobId.equals(client.getCurrentJobId())) {
			// Ignore this job because it is currently running
			return null;
		}

		Long timestamp = jobsContainer.getTimestamp(jobId);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd_HH-mm-ss-SSS");
		String formattedTimestamp = dateFormat.format(new Date(timestamp));

		String runFolderName = resultsLocation + "/" + formattedTimestamp;
		File runFolder = new File(runFolderName);

		// if the folder already exists, assume data has already been fetched
		boolean success = runFolder.exists();

		if (!success && connected) {
			// as the data is missing try to fetch it again from the server
			InputStream resultsZipStream = fetchResultsZipStream(client, project, jobId);

			if (resultsZipStream != null) {
				String zipFileName = runFolderName + "/" + jobId + ".tmp";
				success = unpackFromInputStream(jobId.toString(), runFolder, zipFileName, resultsZipStream);
			}
		}

		SpotterProjectRunResult runResult = null;

		if (success) {
			String projectRelativePath = FileManager.DEFAULT_RESULTS_DIR_NAME + "/" + formattedTimestamp;
			IFolder folder = project.getFolder(projectRelativePath);
			try {
				if (!folder.isSynchronized(IResource.DEPTH_INFINITE)) {
					folder.refreshLocal(IResource.DEPTH_INFINITE, null);
				}
				if (folder.members().length != 0) {
					runResult = new SpotterProjectRunResult(this, jobId, timestamp, folder);
				} else {
					// there are no files after successful fetch, so remove job
					// id completely
					JobsContainer.removeJobId(project, jobId);
				}
			} catch (CoreException e1) {
				LOGGER.warn("An error occured while looking up folder members", e1);
				try {
					// in this case delete the folder so it can be fetched
					// normally again next time
					folder.delete(true, null);
				} catch (CoreException e2) {
					LOGGER.warn("An error occured while deleting folder", e2);
				}
			}
		}

		return runResult;
	}

	/*
	 * Unpacks the data from the input stream to the given run folder.
	 */
	private boolean unpackFromInputStream(String jobId, File runFolder, String zipFileName, InputStream resultsZipStream) {
		try {
			runFolder.mkdir();
			FileOutputStream fos = new FileOutputStream(zipFileName);
			LpeStreamUtils.pipe(resultsZipStream, fos);

			resultsZipStream.close();

			File zipFile = new File(zipFileName);
			LpeFileUtils.unzip(zipFile, runFolder);
			zipFile.delete();

			return true;
		} catch (Exception e) {
			String message = "Error while saving fetched results for job " + jobId + "!";
			DialogUtils.handleError(message, e);
			LOGGER.error(message, e);
		}

		return false;
	}

	/*
	 * Tries to fetch the input stream from the server, but returns null and
	 * removes the job if it's empty.
	 */
	private InputStream fetchResultsZipStream(ServiceClientWrapper client, IProject project, Long jobId) {
		InputStream resultsZipStream = client.requestResults(jobId.toString());
		boolean isEmptyStream = true;

		try {
			if (resultsZipStream != null && resultsZipStream.available() > 0) {
				isEmptyStream = false;
			} else {
				String msg = "Received empty input stream for job " + jobId + ", removing job!";
				JobsContainer.removeJobId(project, jobId);
				DialogUtils.openWarning(msg);
			}
		} catch (IOException e) {
			resultsZipStream = null;
			String message = "An error occured while reading from input stream for job " + jobId + ", skipping job!";
			DialogUtils.handleError(message, e);
			LOGGER.error(message, e);
		}

		return isEmptyStream ? null : resultsZipStream;
	}

}
