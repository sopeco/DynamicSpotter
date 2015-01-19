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
package org.spotter.shared.result.model;

import java.io.Serializable;
import java.util.Map;

import org.spotter.shared.hierarchy.model.XPerformanceProblem;

/**
 * Container for analysis result of dynamic spotter.
 * 
 * @author Alexander Wert
 * 
 */
public class ResultsContainer implements Serializable {

	private static final long serialVersionUID = 4604809431038023866L;

	private Map<String, SpotterResult> resultsMap;
	private XPerformanceProblem rootProblem;
	private String report;
	private String label;
	private String annotation;

	/**
	 * Returns a map from problemIds to spotter results.
	 * 
	 * @return Returns a map from problemIds to spotter results.
	 */
	public Map<String, SpotterResult> getResultsMap() {
		return resultsMap;
	}

	/**
	 * Set a results map.
	 * 
	 * @param resultsMap
	 *            map of results.
	 */
	public void setResultsMap(Map<String, SpotterResult> resultsMap) {
		this.resultsMap = resultsMap;
	}

	/**
	 * 
	 * @return returns the root problem.
	 */
	public XPerformanceProblem getRootProblem() {
		return rootProblem;
	}

	/**
	 * 
	 * @param rootProblem
	 *            the root problem to set
	 */
	public void setRootProblem(XPerformanceProblem rootProblem) {
		this.rootProblem = rootProblem;
	}

	/**
	 * 
	 * @return the report text
	 */
	public String getReport() {
		return report;
	}

	/**
	 * Sets the report text.
	 * 
	 * @param report
	 *            the report text to set
	 */
	public void setReport(String report) {
		this.report = report;
	}

	/**
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label. This is an additional short info which characterizes the
	 * results of the run.
	 * 
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * 
	 * @return the annotation text
	 */
	public String getAnnotation() {
		return annotation;
	}

	/**
	 * Sets the annotation text.
	 * 
	 * @param annotation
	 *            the annotation to set
	 */
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	/**
	 * Resets all data.
	 */
	public void reset() {
		resultsMap = null;
		rootProblem = null;
		report = null;
	}

}
