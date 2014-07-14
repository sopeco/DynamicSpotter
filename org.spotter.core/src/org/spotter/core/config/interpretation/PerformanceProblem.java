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
package org.spotter.core.config.interpretation;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;

/**
 * A {@link PerformanceProblem} instance represents a performance problem node
 * from the hierarchy.
 * 
 * @author Alexander Wert
 * 
 */
public class PerformanceProblem {
	private static final int _1237 = 1237;

	private static final int _1231 = 1231;

	private List<PerformanceProblem> children;

	private Properties configuration;

	private IDetectionController detectionController;

	private String problemName = null;

	private Boolean detectable = null;
	
	private String uniqueId;

	/**
	 * Create a new instance with the given unique id.
	 * 
	 * @param id unique id of this problem
	 */
	public PerformanceProblem(String uniqueId) {
		this.uniqueId = uniqueId;
		this.children = new ArrayList<PerformanceProblem>();
		setConfiguration(new Properties());
	}



	/**
	 * Returns the detection controller ({@link AbstractDetectionController}) to
	 * be used for detection of this problem.
	 * 
	 * @return the detection controller ({@link AbstractDetectionController}) to
	 *         be used for detection of this problem.
	 */
	public IDetectionController getDetectionController() {
		return detectionController;
	}

	/**
	 * Sets the detection controller ({@link AbstractDetectionController}) to be
	 * used for detection of this problem.
	 * 
	 * @param detectionController
	 *            detection controller to be used.
	 */
	public void setDetectionController(IDetectionController detectionController) {
		this.detectionController = detectionController;
	}

	/**
	 * Returns the unique id of this problem.
	 * 
	 * @return the unique id of this problem.
	 */
	public String getUniqueId() {
		return uniqueId;
	}
	
	/**
	 * Returns all child performance problems as described by the performance
	 * problem hierarchy.
	 * 
	 * @return all child performance problems.
	 */
	public List<PerformanceProblem> getChildren() {
		return children;
	}

	/**
	 * Indicates whether this problem is detectable or not. A non-detectable
	 * performance problem is an abstract problem which comprises different
	 * concrete problems.
	 * 
	 * @return true, if problem is detectable
	 */
	public boolean isDetectable() {
		if (detectable == null) {
			detectable = Boolean.parseBoolean(getConfiguration().getProperty(
					AbstractDetectionController.DETECTABLE_KEY, "true"));
		}

		return detectable;
	}

	/**
	 * 
	 * @return Returns a list of all descending problems including itself.
	 */
	public List<PerformanceProblem> getAllDEscendingProblems() {
		List<PerformanceProblem> problems = new ArrayList<PerformanceProblem>();
		problems.add(this);
		int i = 0;
		while (i < problems.size()) {
			problems.addAll(problems.get(i).getChildren());
			i++;
		}
		return problems;
	}

	/**
	 * 
	 * @return Configuration
	 */
	public Properties getConfiguration() {
		return configuration;
	}

	/**
	 * Sets the configuration.
	 * 
	 * @param configuration
	 *            new configuration
	 */
	public void setConfiguration(Properties configuration) {
		this.configuration = configuration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		result = prime * result + (isDetectable() ? _1231 : _1237);
		result = prime * result + ((getProblemName() == null) ? 0 : problemName.hashCode());
		result = prime * result + ((getUniqueId() == null) ? 0 : uniqueId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PerformanceProblem other = (PerformanceProblem) obj;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		if (detectable == null) {
			if (other.detectable != null)
				return false;
		} else if (!detectable.equals(other.detectable))
			return false;
		if (problemName == null) {
			if (other.problemName != null)
				return false;
		} else if (!problemName.equals(other.problemName))
			return false;
		if (uniqueId == null) {
			if (other.uniqueId != null)
				return false;
		} else if (!uniqueId.equals(other.uniqueId))
			return false;
		return true;
	}

	/**
	 * @return the problemName
	 */
	public String getProblemName() {
		return problemName;
	}



	/**
	 * @param problemName the problemName to set
	 */
	public void setProblemName(String problemName) {
		this.problemName = problemName;
	}

}
