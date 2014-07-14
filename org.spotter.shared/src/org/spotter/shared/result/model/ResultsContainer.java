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

public class ResultsContainer implements Serializable {

	private static final long serialVersionUID = 3192866585664021357L;
	
	private Map<String, SpotterResult> resultsMap;
	private XPerformanceProblem rootProblem;
	
	public Map<String, SpotterResult> getResultsMap() {
		return resultsMap;
	}
	
	public void setResultsMap(Map<String, SpotterResult> resultsMap) {
		this.resultsMap = resultsMap;
	}
	
	public XPerformanceProblem getRootProblem() {
		return rootProblem;
	}
	
	public void setRootProblem(XPerformanceProblem rootProblem) {
		this.rootProblem = rootProblem;
	}
	
	
}
