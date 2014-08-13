package org.spotter.core.workload;

/**
 * Wraps the specification of workload.
 * 
 * @author Alexander Wert
 * 
 */
public final class LoadConfig {
	private int numUsers;
	private int rampUpIntervalLength;
	private int rampUpUsersPerInterval;
	private int coolDownIntervalLength;
	private int coolDownUsersPerInterval;
	private int experimentDuration;

	/**
	 * @return the numUsers
	 */
	public int getNumUsers() {
		return numUsers;
	}

	/**
	 * @param numUsers
	 *            the numUsers to set
	 */
	public void setNumUsers(int numUsers) {
		this.numUsers = numUsers;
	}

	/**
	 * @return the rampUpIntervalLength
	 */
	public int getRampUpIntervalLength() {
		return rampUpIntervalLength;
	}

	/**
	 * @param rampUpIntervalLength
	 *            the rampUpIntervalLength to set
	 */
	public void setRampUpIntervalLength(int rampUpIntervalLength) {
		this.rampUpIntervalLength = rampUpIntervalLength;
	}

	/**
	 * @return the rampUpUsersPerInterval
	 */
	public int getRampUpUsersPerInterval() {
		return rampUpUsersPerInterval;
	}

	/**
	 * @param rampUpUsersPerInterval
	 *            the rampUpUsersPerInterval to set
	 */
	public void setRampUpUsersPerInterval(int rampUpUsersPerInterval) {
		this.rampUpUsersPerInterval = rampUpUsersPerInterval;
	}

	/**
	 * @return the coolDownIntervalLength
	 */
	public int getCoolDownIntervalLength() {
		return coolDownIntervalLength;
	}

	/**
	 * @param coolDownIntervalLength
	 *            the coolDownIntervalLength to set
	 */
	public void setCoolDownIntervalLength(int coolDownIntervalLength) {
		this.coolDownIntervalLength = coolDownIntervalLength;
	}

	/**
	 * @return the coolDownUsersPerInterval
	 */
	public int getCoolDownUsersPerInterval() {
		return coolDownUsersPerInterval;
	}

	/**
	 * @param coolDownUsersPerInterval
	 *            the coolDownUsersPerInterval to set
	 */
	public void setCoolDownUsersPerInterval(int coolDownUsersPerInterval) {
		this.coolDownUsersPerInterval = coolDownUsersPerInterval;
	}

	/**
	 * @return the experimentDuration
	 */
	public int getExperimentDuration() {
		return experimentDuration;
	}

	/**
	 * @param experimentDuration
	 *            the experimentDuration to set
	 */
	public void setExperimentDuration(int experimentDuration) {
		this.experimentDuration = experimentDuration;
	}

}
