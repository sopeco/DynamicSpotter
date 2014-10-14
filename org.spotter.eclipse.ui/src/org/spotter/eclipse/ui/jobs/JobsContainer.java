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
package org.spotter.eclipse.ui.jobs;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.lpe.common.util.LpeFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.shared.configuration.FileManager;

/**
 * A container for all requested jobs of a DS project.
 * 
 * @author Denis Knoepfle
 * 
 */
public class JobsContainer implements Serializable {

	private static final Object jobMonitor = new Object();

	private static final Logger LOGGER = LoggerFactory.getLogger(JobsContainer.class);

	private static final long serialVersionUID = 2437447304864394397L;

	private final Set<Long> jobIds = new HashSet<>();
	private final Map<Long, Long> timestamps = new HashMap<>();

	/**
	 * Returns <code>true</code> if the id is included, otherwise
	 * <code>false</code>.
	 * 
	 * @param jobId
	 *            the id to lookup
	 * @return <code>true</code> if the id is included, <code>false</code>
	 *         otherwise
	 */
	public boolean hasJobId(Long jobId) {
		return jobIds.contains(jobId);
	}

	/**
	 * Returns the timestamp that corresponds to the given job id.
	 * 
	 * @param jobId
	 *            the job id the timestamp shall be returned for
	 * @return the corresponding timestamp
	 */
	public Long getTimestamp(Long jobId) {
		if (!jobIds.contains(jobId)) {
			throw new IllegalArgumentException("The given job id is not registered");
		}
		return timestamps.get(jobId);
	}

	/**
	 * Returns an array of all stored job ids.
	 * 
	 * @return an array of all stored job ids
	 */
	public Long[] getJobIds() {
		return jobIds.toArray(new Long[count()]);
	}

	/**
	 * Returns the amount of stored job ids.
	 * 
	 * @return the amount of stored job ids
	 */
	public int count() {
		return jobIds.size();
	}

	/**
	 * Adds the given job id.
	 * 
	 * @param jobId
	 *            the id to add
	 * @param timestamp
	 *            the corresponding timestamp
	 */
	public void addJobId(Long jobId, Long timestamp) {
		jobIds.add(jobId);
		timestamps.put(jobId, timestamp);
	}

	/**
	 * Removes the given job id.
	 * 
	 * @param jobId
	 *            the id to remove
	 */
	public void removeJobId(Long jobId) {
		timestamps.remove(jobId);
		jobIds.remove(jobId);
	}

	/**
	 * Clears all job ids.
	 */
	public void reset() {
		timestamps.clear();
		jobIds.clear();
	}

	/**
	 * Registers the given job id for the project.
	 * 
	 * @param project
	 *            the project the id belongs to
	 * @param jobId
	 *            the id to register
	 * @param timestamp
	 *            the corresponding timestamp
	 * @return <code>true</code> on success, otherwise <code>false</code>
	 */
	public static boolean registerJobId(IProject project, long jobId, long timestamp) {
		boolean success = false;
		synchronized (JobsContainer.jobMonitor) {
			JobsContainer jobsContainer = readJobsContainer(project);
			jobsContainer.addJobId(jobId, timestamp);
			success = writeJobsContainer(project, jobsContainer);
		}
		return success;
	}

	/**
	 * Removes the given job id for the project.
	 * 
	 * @param project
	 *            the project the id belongs to
	 * @param jobId
	 *            the id to remove
	 * @return <code>true</code> on success, otherwise <code>false</code>
	 */
	public static boolean removeJobId(IProject project, long jobId) {
		boolean success = false;
		synchronized (JobsContainer.jobMonitor) {
			JobsContainer jobsContainer = readJobsContainer(project);
			jobsContainer.removeJobId(jobId);
			success = writeJobsContainer(project, jobsContainer);
		}
		return success;
	}

	/**
	 * Retrieves the current job container for the given project. In case the
	 * file does not exist or an error occurs while reading it an empty
	 * container is returned.
	 * 
	 * @param project
	 *            the project the container should be retrieved for
	 * @return the corresponding job container or an empty one
	 */
	public static JobsContainer readJobsContainer(IProject project) {
		String fileName = project.getFile(FileManager.JOBS_CONTAINER_FILENAME).getLocation().toString();
		File file = new File(fileName);
		JobsContainer jobsContainer = new JobsContainer();
		if (file.exists()) {
			try {
				jobsContainer = (JobsContainer) LpeFileUtils.readObject(file);
			} catch (ClassNotFoundException | IOException e) {
				LOGGER.warn("JobsContainer {} corrupted, ignoring file contents. Error: {}", fileName, e.getMessage());
			}
		}
		return jobsContainer;
	}

	private static boolean writeJobsContainer(IProject project, JobsContainer jobsContainer) {
		String fileName = project.getFile(FileManager.JOBS_CONTAINER_FILENAME).getLocation().toString();
		try {
			LpeFileUtils.writeObject(fileName, jobsContainer);
			return true;
		} catch (IOException e) {
			LOGGER.error("Error while writing JobsContainer. Cause: {}", e.toString());
		}
		return false;
	}

}
