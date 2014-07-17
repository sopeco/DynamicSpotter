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
package org.spotter.detection.hiccup.utils;

/**
 * A hiccup is specified by its start timestamp, end timestamp, max response
 * time and average response time.
 * 
 * @author C5170547
 * 
 */
public class Hiccup {
	private long startTimestamp;
	private long endTimestamp;
	private double maxHiccupResponseTime;
	private double avgHiccupResponseTime;
	private double maxPreprocessedResponseTime;
	private double avgPreprocessedResponseTime;
	private double stdDevWithoutOutliers;
	private double avgResponeTimeWithoutOutliers;
	private double deviationThreshold;

	/**
	 * @return the startTimestamp
	 */
	public long getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * @param startTimestamp
	 *            the startTimestamp to set
	 */
	public void setStartTimestamp(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	/**
	 * 
	 * @return hiccup duration
	 */
	public long getHiccupDuration() {
		return endTimestamp - startTimestamp;
	}

	/**
	 * @return the endTimestamp
	 */
	public long getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * @param endTimestamp
	 *            the endTimestamp to set
	 */
	public void setEndTimestamp(long endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	/**
	 * @return the maxResponseTime
	 */
	public double getMaxHiccupResponseTime() {
		return maxHiccupResponseTime;
	}

	/**
	 * @param maxResponseTime
	 *            the maxResponseTime to set
	 */
	public void setMaxHiccupResponseTime(double maxResponseTime) {
		this.maxHiccupResponseTime = maxResponseTime;
	}

	/**
	 * @return the averageResponseTime
	 */
	public double getAvgHiccupResponseTime() {
		return avgHiccupResponseTime;
	}

	/**
	 * @param averageResponseTime
	 *            the averageResponseTime to set
	 */
	public void setAvgHiccupResponseTime(double averageResponseTime) {
		this.avgHiccupResponseTime = averageResponseTime;
	}

	/**
	 * @return the stdDevWithoutOutliers
	 */
	public double getStdDevWithoutOutliers() {
		return stdDevWithoutOutliers;
	}

	/**
	 * @param stdDevWithoutOutliers
	 *            the stdDevWithoutOutliers to set
	 */
	public void setStdDevWithoutOutliers(double stdDevWithoutOutliers) {
		this.stdDevWithoutOutliers = stdDevWithoutOutliers;
	}

	/**
	 * @return the avgResponeTimeWithoutOutliers
	 */
	public double getAvgResponeTimeWithoutOutliers() {
		return avgResponeTimeWithoutOutliers;
	}

	/**
	 * @param avgResponeTimeWithoutOutliers
	 *            the avgResponeTimeWithoutOutliers to set
	 */
	public void setAvgResponeTimeWithoutOutliers(double avgResponeTimeWithoutOutliers) {
		this.avgResponeTimeWithoutOutliers = avgResponeTimeWithoutOutliers;
	}

	/**
	 * @return the deviationThreshold
	 */
	public double getDeviationThreshold() {
		return deviationThreshold;
	}

	/**
	 * @param deviationThreshold
	 *            the deviationThreshold to set
	 */
	public void setDeviationThreshold(double deviationThreshold) {
		this.deviationThreshold = deviationThreshold;
	}

	/**
	 * @return the maxPreprocessedResponseTime
	 */
	public double getMaxPreprocessedResponseTime() {
		return maxPreprocessedResponseTime;
	}

	/**
	 * @param maxPreprocessedResponseTime the maxPreprocessedResponseTime to set
	 */
	public void setMaxPreprocessedResponseTime(double maxPreprocessedResponseTime) {
		this.maxPreprocessedResponseTime = maxPreprocessedResponseTime;
	}

	/**
	 * @return the avgPreprocessedResponseTime
	 */
	public double getAvgPreprocessedResponseTime() {
		return avgPreprocessedResponseTime;
	}

	/**
	 * @param avgPreprocessedResponseTime the avgPreprocessedResponseTime to set
	 */
	public void setAvgPreprocessedResponseTime(double avgPreprocessedResponseTime) {
		this.avgPreprocessedResponseTime = avgPreprocessedResponseTime;
	}

}
