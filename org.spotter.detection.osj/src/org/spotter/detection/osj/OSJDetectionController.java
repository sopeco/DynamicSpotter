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
//package org.spotter.detection.osj;
//
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Properties;
//import java.util.Set;
//
//import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
//import org.lpe.common.config.GlobalConfiguration;
//import org.lpe.common.extension.IExtension;
//import org.lpe.common.instrumentation.InstrumentationException;
//import org.lpe.common.instrumentation.description.InstrumentationDescription;
//import org.lpe.common.instrumentation.description.InstrumentationScope;
//import org.lpe.common.instrumentation.description.ScopeInstDescription;
//import org.lpe.common.measurement.data.MeasurementProbeType;
//import org.lpe.common.measurement.data.records.CPUUtilizationRecord;
//import org.lpe.common.measurement.data.records.ResponseTimeRecord;
//import org.lpe.common.measurement.dataset.Dataset;
//import org.lpe.common.measurement.dataset.DatasetCollection;
//import org.lpe.common.measurement.dataset.Parameter;
//import org.lpe.common.measurement.dataset.ParameterSelection;
//import org.lpe.common.measurement.exceptions.MeasurementException;
//import org.lpe.common.util.LpeStringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.spotter.core.config.ConfigKeys;
//import org.spotter.core.detection.AbstractDetectionController;
//import org.spotter.core.detection.IDetectionController;
//import org.spotter.core.result.SpotterResult;
//import org.spotter.core.workload.IWorkloadAdapter;
//import org.spotter.exceptions.WorkloadException;
//
//
///**
// * Detection controller for the Operating System Jobs and Burst Calculations
// * (OSJ) problem.
// * 
// * @author Le-Huan Stefan Tran
// */
//public class OSJDetectionController extends AbstractDetectionController {
//
//	private static final int MILLIS_IN_MINUTE = 60000;
//
//	private static final int MILLIS_IN_SECOND = 1000;
//
//	private static final Logger LOGGER = LoggerFactory.getLogger(OSJDetectionController.class);
//
//	private static final String OSJ_SCOPE_KEY = "osj.scope";
//	private static final String NUMBER_OF_USERS = "numUsers";
//	private static final int EXPERIMENT_STEPS = 5;
//
//	private static final double HICCUP_OUTLIER_DEVIATION_FACTOR = 1.0;
//	private static final double MAX_CPU_UTILIZATION = 0.9;
//
//	private InstrumentationScope scope;
//	
//	/**
//	 * Constructor.
//	 * 
//	 * @param provider
//	 *            extension provider.
//	 */
//	public OSJDetectionController(IExtension<IDetectionController> provider) {
//		super(provider);
//
//	}
//
//	@Override
//	protected void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException {
//		scope = InstrumentationScope.valueOf(getProblemDetectionConfiguration().getProperty(OSJ_SCOPE_KEY)
//				.toUpperCase());
//		double minUsers = 1;
//		double maxUsers = Double.parseDouble(LpeStringUtils.getPropertyOrFail(GlobalConfiguration.getInstance()
//				.getProperties(), ConfigKeys.WORKLOAD_MAXUSERS, null));
//		double step = (maxUsers - minUsers) / (double) EXPERIMENT_STEPS;
//		if (step < 1.0) {
//			step = 1.0;
//		}
//
//		double experimentDuration = Double.parseDouble(LpeStringUtils.getPropertyOrFail(GlobalConfiguration
//				.getInstance().getProperties(), ConfigKeys.EXPERIMENT_DURATION, null)) * MILLIS_IN_SECOND;
//		double experimentRampUpTime = Double.parseDouble(LpeStringUtils.getPropertyOrFail(GlobalConfiguration
//				.getInstance().getProperties(), ConfigKeys.EXPERIMENT_RAMP_UP_TIME, null)) * MILLIS_IN_SECOND;
//		double experimentCoolDownTime = Double.parseDouble(LpeStringUtils.getPropertyOrFail(GlobalConfiguration
//				.getInstance().getProperties(), ConfigKeys.EXPERIMENT_COOL_DOWN_TIME, null)) * MILLIS_IN_SECOND;
//		long measurementDuration = (long) (experimentRampUpTime + experimentDuration + experimentCoolDownTime + MILLIS_IN_MINUTE);
//
//		// TODO Alex: Make all target application classes to be
//		// instrumented available for instrumentation
//		// Solution for now: Run workload for target application once.
//		for (double dUsers = minUsers; dUsers <= maxUsers; dUsers += step) {
//			int numUsers = new Long(Math.round(dUsers)).intValue();
//			LOGGER.info("Starting experiment with {} users ...", numUsers);
//
//			Properties wlProperties = new Properties();
//			wlProperties.put(IWorkloadAdapter.NUMBER_CURRENT_USERS, "" + numUsers);
//
//			// Instrument target application at the beginning of the measurement
//			// series
//			if (numUsers == minUsers) {
//				instrumentApplication();
//			}
//
//			workloadAdapter.startLoad(wlProperties);
//
//			// Measurement phase (including warm-up phase)
//			measurementController.enableMonitoring();
//			try {
//				Thread.sleep(measurementDuration);
//				// Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			measurementController.disableMonitoring();
//
//			workloadAdapter.waitForFinishedLoad();
//
//			// Uninstrument application under test at the end of measurement
//			// series
//			if (numUsers == maxUsers) {
//				uninstrumentApplication();
//			}
//
//
//
//			LOGGER.info("Storing data ...");
//			Parameter numOfUsersParameter = new Parameter(NUMBER_OF_USERS, numUsers);
//			Set<Parameter> parameters = new HashSet<Parameter>();
//			parameters.add(numOfUsersParameter);
//			storeResults(parameters);
//			LOGGER.info("Data stored!");
//		}
//	}
//
//	private void instrumentApplication() throws InstrumentationException {
//		InstrumentationDescription instDescription = new InstrumentationDescription();
//
//		
//
//		// Specify instrumentation
//		instDescription.addScopeInstrumentation(new ScopeInstDescription(scope, MeasurementProbeType.RESPONSE_TIME));
//
//		instrumentApplication(instDescription);
//	}
//
//	@Override
//	protected SpotterResult analyze(DatasetCollection data) {
//		LOGGER.info("Analyzing operating system jobs and calculation bursts...");
//		SpotterResult result = new SpotterResult();
//		result.addMessage("Scope: " + scope);
//
//		Dataset rtDataSet = data.getDataSets(ResponseTimeRecord.class).get(0);
//
//		Dataset cpuDataSet = data.getDataSets(CPUUtilizationRecord.class).get(0);
//
//		// Find response time hiccups and high CPU records
//		List<List<Double>> hiccups = findRTHiccups(rtDataSet);
//		List<Long> highCPURecords = findHighCPUTimes(cpuDataSet);
//
//		// Sort hiccups by timestamp
//		final int idxHiccupTimestamp = 0;
//		Collections.sort(hiccups, new Comparator<List<Double>>() {
//			@Override
//			public int compare(List<Double> o1, List<Double> o2) {
//				return o1.get(idxHiccupTimestamp).compareTo(o2.get(idxHiccupTimestamp));
//			}
//		});
//
//		// Get first hiccup and first high CPU time
//		Iterator<List<Double>> iterHiccups = hiccups.iterator();
//		Iterator<Long> iterHighCPUTimes = highCPURecords.iterator();
//		int idxHicStartTimestamp = 0;
//		int idxHicRT = 1;
//
//		if (iterHiccups.hasNext() && iterHighCPUTimes.hasNext()) {
//			List<Double> hiccup = iterHiccups.next();
//			double hicStartTimestamp = hiccup.get(idxHicStartTimestamp);
//			double hicRT = hiccup.get(idxHicRT);
//			double hicEndTimestamp = hicStartTimestamp + hicRT;
//			double highCPUTime = iterHighCPUTimes.next();
//
//			// Investigate if hiccup is caused by high CPU utilization
//			boolean finished = false;
//
//			while (!finished) {
//				if (highCPUTime < hicStartTimestamp) {
//					if (iterHighCPUTimes.hasNext()) {
//						highCPUTime = iterHighCPUTimes.next();
//					} else {
//						finished = true;
//					}
//				} else if (hicEndTimestamp < highCPUTime) {
//					if (iterHiccups.hasNext()) {
//						hiccup = iterHiccups.next();
//						hicStartTimestamp = hiccup.get(idxHicStartTimestamp);
//						hicRT = hiccup.get(idxHicRT);
//						hicEndTimestamp = hicStartTimestamp + hicRT;
//					} else {
//						finished = true;
//					}
//				} else {
//					LOGGER.info("OSJ problem: Operating jobs or calculation bursts detected within response time hiccup at "
//							+ highCPUTime + "ms!");
//					LOGGER.info("Reponse time hiccups of " + hicRT + "ms starting at " + hicStartTimestamp + "ms");
//
//					// Update Spotter results
//					result.setDetected(true);
//					result.addMessage("OSJ problem: High CPU utilization within response time hiccup at " + highCPUTime
//							+ "ms");
//
//					// Get next hiccup and CPU record
//					if (iterHighCPUTimes.hasNext()) {
//						highCPUTime = iterHighCPUTimes.next();
//					} else {
//						finished = true;
//					}
//				}
//			}
//		}
//
//		return result;
//	}
//
//	private List<List<Double>> findRTHiccups(Dataset rtDataSet) {
//		List<List<Double>> hiccups = new LinkedList<List<Double>>();
//
//		// Loop through each instrumented operation
//		for (ParameterSelection parSelection : rtDataSet.getAllParameterConfigurations()) {
//			Dataset selectedDataSet = parSelection.applyTo(rtDataSet);
//			Iterator<Double> iterRT = selectedDataSet.getValues(ResponseTimeRecord.PAR_RESPONSE_TIME, Double.class)
//					.iterator();
//
//			DescriptiveStatistics rtStatistics = new DescriptiveStatistics();
//
//			while (iterRT.hasNext()) {
//				double rt = (Double) iterRT.next();
//				rtStatistics.addValue(rt);
//			}
//
//			// Calculate response time statistics
//			double rtMean = rtStatistics.getMean();
//			double rtStdDev = rtStatistics.getStandardDeviation();
//			double rtOutlierDeviation = HICCUP_OUTLIER_DEVIATION_FACTOR * rtStdDev;
//
//			// Loop through each response time
//			Iterator<Double> iterRTStartTimestamp = selectedDataSet.getValues(ResponseTimeRecord.PAR_TIMESTAMP,
//					Double.class).iterator();
//			iterRT = selectedDataSet.getValues(ResponseTimeRecord.PAR_RESPONSE_TIME, Double.class).iterator();
//
//			while (iterRT.hasNext()) {
//				double rtStartTimestamp = (Double) iterRTStartTimestamp.next();
//				double rt = (Double) iterRT.next();
//				double rtDeviation = rt - rtMean;
//
//				// Save hiccup
//				if (rtDeviation > rtOutlierDeviation) {
//					List<Double> hiccup = new LinkedList<Double>();
//					hiccup.add(rtStartTimestamp);
//					hiccup.add(rt);
//					hiccups.add(hiccup);
//				}
//			}
//		}
//
//		return hiccups;
//	}
//
//	private List<Long> findHighCPUTimes(Dataset cpuDataSet) {
//
//		List<Long> highCPUTimes = new LinkedList<Long>();
//
//		// Loop through each record set
//		for (ParameterSelection parSelection : cpuDataSet.getAllParameterConfigurations()) {
//			Dataset selectedDataSet = parSelection.applyTo(cpuDataSet);
//			Iterator<Long> iterCPUTimestamp = selectedDataSet.getValues(CPUUtilizationRecord.PAR_TIMESTAMP,
//					Long.class).iterator();
//			Iterator<Long> iterCPUUtilization = selectedDataSet.getValues(CPUUtilizationRecord.RES_CPU_AGGREGATED,
//					Long.class).iterator();
//			// Loop through each CPU record
//
//			while (iterCPUTimestamp.hasNext()) {
//				long cpuTimestamp = iterCPUTimestamp.next();
//				long cpuUtilization =  iterCPUUtilization.next();
//
//				// Check if CPU utilization is high
//				if (cpuUtilization > MAX_CPU_UTILIZATION) {
//					highCPUTimes.add(cpuTimestamp);
//				}
//			}
//		}
//
//		return highCPUTimes;
//	}
//
//	@Override
//	public void loadProperties() {
//		// TODO Auto-generated method stub
//		
//	}
//
//}
