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
package org.spotter.detection.highmessaging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.instrumentation.description.InstrumentationDescription;
import org.aim.api.measurement.dataset.Dataset;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.api.measurement.dataset.ParameterSelection;
import org.aim.artifacts.records.CPUUtilizationRecord;
import org.aim.artifacts.records.JmsServerRecord;
import org.aim.artifacts.records.NetworkRecord;
import org.aim.artifacts.records.ResponseTimeRecord;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.LpeNumericUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.SpotterResult;

import com.xeiam.xchart.Chart;

/**
 * Detection controller for a high messaging behavior.
 * 
 * @author Marius Oehler
 * 
 */
public class HighMessagingDetectionController extends AbstractDetectionController {

	private static final Logger LOGGER = LoggerFactory.getLogger(HighMessagingDetectionController.class);

	private static final int EXPERIMENT_STEPS = 11;
	private static final String NUMBER_OF_USERS = "numUsers";
	private static final double NETWORK_SPEED = 1000.0 * 1000.0 * 100.0 / 8.0;

	/**
	 * Constructor.
	 * 
	 * @param provider
	 */
	public HighMessagingDetectionController(IExtension<IDetectionController> provider) {
		super(provider);
	}

	@Override
	protected void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException {
		executeDefaultExperimentSeries(HighMessagingDetectionController.class, EXPERIMENT_STEPS,
				getInstrumentationDescription());
	}

	private InstrumentationDescription getInstrumentationDescription() throws InstrumentationException {
		InstrumentationDescription instDescription = new InstrumentationDescription();
		return instDescription;
	}

	// @Override
	// protected void executeExperiments() throws InstrumentationException,
	// MeasurementException, WorkloadException {
	// double minUsers = 1;
	// double maxUsers =
	// Double.parseDouble(LpeStringUtils.getPropertyOrFail(GlobalConfiguration.getInstance()
	// .getProperties(), ConfigKeys.WORKLOAD_MAXUSERS, null));
	// double step = (maxUsers - minUsers) / (double) EXPERIMENT_STEPS;
	//
	// for (double dUsers = minUsers; dUsers <= maxUsers; dUsers += step) {
	// int numUsers = new Long(Math.round(dUsers)).intValue();
	// LOGGER.info("Starting Experiment with {} / {} users ...", numUsers,
	// maxUsers);
	//
	// // Start workload
	// Properties wlProperties = new Properties();
	// wlProperties.putAll(GlobalConfiguration.getInstance().getProperties());
	// wlProperties.put(IWorkloadAdapter.NUMBER_CURRENT_USERS, "" + numUsers);
	//
	// workloadAdapter.startLoad(wlProperties);
	//
	// // Enable measurements
	// measurementController.enableMonitoring();
	//
	// workloadAdapter.waitForFinishedLoad();
	//
	// // Disable measurements
	// measurementController.disableMonitoring();
	//
	// LOGGER.info("Fetching measurement data ...");
	// MeasurementData data = measurementController.getMeasurementData();
	// LOGGER.info("Measurement data fetched!");
	//
	// LOGGER.info("Storing data ...");
	// Parameter numOfUsersParameter = new Parameter(NUMBER_OF_USERS, numUsers);
	// Set<Parameter> parameters = new HashSet<Parameter>();
	// parameters.add(numOfUsersParameter);
	// storeResults(parameters);
	// LOGGER.info("Data stored!");
	// }
	// }

	@Override
	protected SpotterResult analyze(DatasetCollection data) {
		LOGGER.debug("Analyze data for HighMessaging..");
		SpotterResult result = new SpotterResult();
		result.setDetected(false);

		// Dataset dataSet = data.getDataSet(JmsServerRecord.class);
		//
		// List<Integer> keys = new
		// ArrayList<Integer>(dataSet.getValueSet(NUMBER_OF_USERS,
		// Integer.class));
		// Collections.sort(keys);
		//
		// calculateMsgThroughputs(result, dataSet, keys);

		Dataset dataSet = data.getDataSet(ResponseTimeRecord.class);

		List<Integer> keys = new ArrayList<Integer>(dataSet.getValueSet(NUMBER_OF_USERS, Integer.class));
		Collections.sort(keys);

		calculateNetworkUtil(data, result, keys);

		// respponse times

		calculateResponseTimes(data, result, keys);

		// cpuUtil

		calculateCPUUtil(data, result, keys);

		// Calculate queue sizes:

		return result;
	}

	private void calculateMsgThroughputs(SpotterResult result, Dataset dataSet, List<Integer> keys) {
		for (String queueName : dataSet.getValueSet(JmsServerRecord.PAR_QUEUE_NAME, String.class)) {
			result.addMessage("********INVETIGATING QUEUE " + queueName + " ***************");

			List<Double> values = new ArrayList<>();
			List<Double> queueSizes = new ArrayList<>();

			for (Integer numUsers : keys) {
				ParameterSelection select = new ParameterSelection().select(NUMBER_OF_USERS, numUsers).select(
						JmsServerRecord.PAR_QUEUE_NAME, queueName);

				Dataset filterDataSet = select.applyTo(dataSet);

				Map<String, Long> tpMap = new HashMap<String, Long>(5, 1.0F);
				Map<String, Long> tpStartMap = new HashMap<String, Long>(5, 1.0F);

				Double queueSize = 0.0;
				int counter = 0;
				long firstTs = Long.MAX_VALUE;
				long lastTs = 0;

				for (JmsServerRecord record : filterDataSet.getRecords(JmsServerRecord.class)) {

					counter++;
					queueSize += record.getQueueSize();

					if (firstTs > record.getTimeStamp()) {
						firstTs = record.getTimeStamp();
					}
					if (lastTs < record.getTimeStamp()) {
						lastTs = record.getTimeStamp();
					}

					// To calculate the statistics of measurement start
					if (tpStartMap.containsKey(record.getQueueName())) {
						if (tpStartMap.get(record.getQueueName()) > record.getEnqueueCount()) {
							tpStartMap.put(record.getQueueName(), record.getEnqueueCount());
						}
					} else {
						tpStartMap.put(record.getQueueName(), record.getEnqueueCount());
					}

					// Total message enqueued
					if (tpMap.containsKey(record.getQueueName())) {
						if (tpMap.get(record.getQueueName()) < record.getEnqueueCount()) {
							tpMap.put(record.getQueueName(), record.getEnqueueCount());
						}
					} else {
						tpMap.put(record.getQueueName(), record.getEnqueueCount());
					}
				}

				long maxTp = 0;
				for (String key : tpMap.keySet()) {
					maxTp += tpMap.get(key) - tpStartMap.get(key);
				}
				double msgPS = maxTp / ((lastTs - firstTs) / 1000D);

				queueSizes.add((double) (queueSize / counter));

				values.add(msgPS);
				// result.addMessage(numUsers + "\t" + msgPS);
			}
			writeQueueSizesToFile(result, keys, queueSizes, "queueSizes-" + queueName.replaceAll("\\.", "_"));
			// Analysis
			double logThreshold = Double.parseDouble(getProblemDetectionConfiguration().getProperty("hm.logThreshold",
					"0.1"));
			double linThreshold = Double.parseDouble(getProblemDetectionConfiguration().getProperty(
					"hm.linearSlopeThreshold", "0.9"));
			writeMessageThroughputToFile(result, keys, values, "TP-" + queueName.replaceAll("\\.", "_"));
			Chart chart = ChartExporter.createRawDataChart("Msg Throughput " + queueName, "Num Users",
					"Msg Throughput [Msgs/s]", keys, values);
			storeImageChartResource(chart, "TP-" + queueName, result);

			// Analyzer a2 = new LinearAnalyzer2(result, keys, values,
			// linThreshold);
			// Analyzer a3 = new LogAnalyzer(result, keys, values,
			// logThreshold);
			// Analyzer a4 = new UpAndDownAnalyzer(result, keys, values);
			//
			// if (a2.analyze() == AnalyzeResult.POSITIVE) {
			// result.addMessage("*************************************************");
			// result.addMessage("Result: System is scaling well. No Problem.");
			//
			// } else {
			// if (a3.analyze() == AnalyzeResult.POSITIVE) {
			// result.addMessage("*************************************************");
			// result.addMessage("Result: Stagnating throuhput. Problem exists.");
			//
			// result.setDetected(true);
			// } else if (a4.analyze() == AnalyzeResult.POSITIVE) {
			// result.addMessage("*************************************************");
			// result.addMessage("Result: Throughput is decreasing. Problem exists.");
			// result.setDetected(true);
			// } else {
			// result.addMessage("*************************************************");
			// result.addMessage("Result: Increasing of throughput is linear, but too low.");
			// result.setDetected(true);
			// }
			// }
		}

	}

	private void calculateNetworkUtil(DatasetCollection data, SpotterResult result, List<Integer> keys) {
		Dataset nwDataset = data.getDataSet(NetworkRecord.class);

		for (String node : nwDataset.getValueSet(NetworkRecord.PAR_PROCESS_ID, String.class)) {
			Dataset nodeDataset = ParameterSelection.newSelection().select(NetworkRecord.PAR_PROCESS_ID, node)
					.applyTo(nwDataset);
			for (String nwInterface : nodeDataset.getValueSet(NetworkRecord.PAR_NETWORK_INTERFACE, String.class)) {
				if (!nwInterface.contains("eth0")) {
					continue;
				}
				Dataset interfaceDataset = ParameterSelection.newSelection().select(NetworkRecord.PAR_PROCESS_ID, node)
						.select(NetworkRecord.PAR_NETWORK_INTERFACE, nwInterface).applyTo(nodeDataset);

				String interfaceName = node + "-" + nwInterface;

				List<Double> sendValues = new ArrayList<>();
				List<Double> receiveValues = new ArrayList<>();

				for (Integer numUsers : keys) {
					List<NetworkRecord> nwRecords = null;
					try {
						nwRecords = ParameterSelection.newSelection().select(NUMBER_OF_USERS, numUsers)
								.applyTo(interfaceDataset).getRecords(NetworkRecord.class);

					} catch (Exception e) {
						sendValues.add(0.0);
						receiveValues.add(0.0);
						continue;
					}

					long startSend = Long.MAX_VALUE;
					long endSend = Long.MIN_VALUE;

					long minNumSend = Long.MAX_VALUE;
					long maxNumSend = Long.MIN_VALUE;
					long minNumReceived = Long.MAX_VALUE;
					long maxNumReceived = Long.MIN_VALUE;

					for (NetworkRecord nwRecord : nwRecords) {
						if (startSend > nwRecord.getTimeStamp()) {
							startSend = nwRecord.getTimeStamp();
							minNumSend = nwRecord.getTotalTransferredBytes();
							minNumReceived = nwRecord.getTotalReceivedBytes();
						}
						if (endSend < nwRecord.getTimeStamp()) {
							endSend = nwRecord.getTimeStamp();

							maxNumSend = nwRecord.getTotalTransferredBytes();
							maxNumReceived = nwRecord.getTotalReceivedBytes();
						}

					}
					double sent = (maxNumSend - minNumSend) / ((endSend - startSend) / 1000.0);
					double received = (maxNumReceived - minNumReceived) / ((endSend - startSend) / 1000.0);
					sendValues.add(sent / NETWORK_SPEED);
					receiveValues.add(received / NETWORK_SPEED);

				}

				writeNetWorkUtilsToFile(result, keys, sendValues, receiveValues, "nwUtil-" + interfaceName);
				Chart chartSent = ChartExporter.createRawDataChart("NW Util S " + interfaceName, "Num Users",
						"Util [%]", keys, sendValues);
				storeImageChartResource(chartSent, "nwUtil-SENT-" + interfaceName, result);
				Chart chartReceived = ChartExporter.createRawDataChart("NW Util R " + interfaceName, "Num Users",
						"Util [%]", keys, receiveValues);
				storeImageChartResource(chartReceived, "nwUtil-Received-" + interfaceName, result);
			}

		}
	}

	private void calculateResponseTimes(DatasetCollection data, SpotterResult result, List<Integer> keys) {
		Dataset rtDataset = ParameterSelection.newSelection()
				.unequal(ResponseTimeRecord.PAR_OPERATION, "Action_Transaction")
				.unequal(ResponseTimeRecord.PAR_OPERATION, "vuser_init_Transaction")
				.unequal(ResponseTimeRecord.PAR_OPERATION, "vuser_end_Transaction")
				.applyTo(data.getDataSet(ResponseTimeRecord.class));

		for (String operation : rtDataset.getValueSet(ResponseTimeRecord.PAR_OPERATION, String.class)) {

			List<Double> rtValues = new ArrayList<>();
			for (Integer numUsers : keys) {
				List<Long> responseTimes = ParameterSelection.newSelection().select(NUMBER_OF_USERS, numUsers)
						.select(ResponseTimeRecord.PAR_OPERATION, operation).applyTo(rtDataset)
						.getValues(ResponseTimeRecord.PAR_RESPONSE_TIME, Long.class);
				double rtMean = LpeNumericUtils.average(responseTimes);
				rtValues.add(rtMean);
			}

			writeResponseTimesToFile(result, keys, rtValues, "RT-" + operation);
			Chart chart = ChartExporter.createRawDataChart("RT-" + operation, "Num Users", " RT [ms]", keys, rtValues);
			storeImageChartResource(chart, "RT-" + operation, result);

		}

		// aggregated Response times:

		List<Double> rtValues = new ArrayList<>();
		for (Integer numUsers : keys) {
			List<Long> responseTimes = ParameterSelection.newSelection().select(NUMBER_OF_USERS, numUsers)
					.unequal(ResponseTimeRecord.PAR_OPERATION, "Action_Transaction")
					.unequal(ResponseTimeRecord.PAR_OPERATION, "vuser_init_Transaction")
					.unequal(ResponseTimeRecord.PAR_OPERATION, "vuser_end_Transaction").applyTo(rtDataset)
					.getValues(ResponseTimeRecord.PAR_RESPONSE_TIME, Long.class);
			double rtMean = LpeNumericUtils.average(responseTimes);
			rtValues.add(rtMean);
		}

		writeResponseTimesToFile(result, keys, rtValues, "RT-Aggregated");
		Chart chart = ChartExporter.createRawDataChart("RT-Aggregated", "Num Users", " RT [ms]", keys, rtValues);
		storeImageChartResource(chart, "RT-Aggregated", result);

	}

	private void calculateCPUUtil(DatasetCollection data, SpotterResult result, List<Integer> keys) {
		Dataset cpuDataset = data.getDataSet(CPUUtilizationRecord.class);

		for (String node : cpuDataset.getValueSet(CPUUtilizationRecord.PAR_PROCESS_ID, String.class)) {
			List<Double> cpuValues = new ArrayList<>();
			for (Integer numUsers : keys) {

				List<Double> utils = ParameterSelection.newSelection().select(NUMBER_OF_USERS, numUsers)
						.select(CPUUtilizationRecord.PAR_PROCESS_ID, node)
						.select(CPUUtilizationRecord.PAR_CPU_ID, CPUUtilizationRecord.RES_CPU_AGGREGATED)
						.applyTo(cpuDataset).getValues(CPUUtilizationRecord.PAR_UTILIZATION, Double.class);
				double cpuUtilMean = LpeNumericUtils.average(utils);
				cpuValues.add(cpuUtilMean);
			}

			writeCPUUtilToFile(result, keys, cpuValues, "CPU-" + node.substring(0, Math.min(15, node.length())));
			Chart chart = ChartExporter.createRawDataChart("CPU-" + node.substring(0, Math.min(15, node.length())),
					"Num Users", "Util [%]", keys, cpuValues);
			storeImageChartResource(chart, "CPU-" + node.substring(0, Math.min(15, node.length())), result);

		}
	}

	private void writeCPUUtilToFile(SpotterResult result, List<Integer> numUsers, List<Double> utils, String fileName) {
		PipedOutputStream outStream = null;
		PipedInputStream inStream = null;
		try {
			outStream = new PipedOutputStream();
			inStream = new PipedInputStream(outStream);
			storeTextResource(fileName, result, inStream);
			BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(outStream));
			bWriter.write("NumUsers;Utilization");
			bWriter.newLine();
			for (int i = 0; i < numUsers.size(); i++) {
				bWriter.write(numUsers.get(i) + ";" + utils.get(i));
				bWriter.newLine();
			}

			bWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeMessageThroughputToFile(SpotterResult result, List<Integer> numUsers, List<Double> throughputs,
			String fileName) {
		PipedOutputStream outStream = null;
		PipedInputStream inStream = null;
		try {
			outStream = new PipedOutputStream();
			inStream = new PipedInputStream(outStream);
			storeTextResource(fileName, result, inStream);
			BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(outStream));
			bWriter.write("NumUsers;throughput");
			bWriter.newLine();
			for (int i = 0; i < numUsers.size(); i++) {
				bWriter.write(numUsers.get(i) + ";" + throughputs.get(i));
				bWriter.newLine();
			}

			bWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeQueueSizesToFile(SpotterResult result, List<Integer> numUsers, List<Double> queueSizes,
			String fileName) {
		PipedOutputStream outStream = null;
		PipedInputStream inStream = null;
		try {
			outStream = new PipedOutputStream();
			inStream = new PipedInputStream(outStream);
			storeTextResource(fileName, result, inStream);
			BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(outStream));
			bWriter.write("NumUsers;QueueSize");
			bWriter.newLine();
			for (int i = 0; i < numUsers.size(); i++) {
				bWriter.write(numUsers.get(i) + ";" + queueSizes.get(i));
				bWriter.newLine();
			}

			bWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeResponseTimesToFile(SpotterResult result, List<Integer> numUsers, List<Double> respTimes,
			String fileName) {
		PipedOutputStream outStream = null;
		PipedInputStream inStream = null;
		try {
			outStream = new PipedOutputStream();
			inStream = new PipedInputStream(outStream);
			storeTextResource(fileName, result, inStream);
			BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(outStream));
			bWriter.write("NumUsers;ResponseTimes");
			bWriter.newLine();
			for (int i = 0; i < numUsers.size(); i++) {
				bWriter.write(numUsers.get(i) + ";" + respTimes.get(i));
				bWriter.newLine();
			}

			bWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeNetWorkUtilsToFile(SpotterResult result, List<Integer> numUsers, List<Double> tpSend,
			List<Double> tpReceived, String fileName) {
		PipedOutputStream outStream = null;
		PipedInputStream inStream = null;
		try {
			outStream = new PipedOutputStream();
			inStream = new PipedInputStream(outStream);
			storeTextResource(fileName, result, inStream);
			BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(outStream));
			bWriter.write("NumUsers;SendUtil;ReceiveUtil");
			bWriter.newLine();
			for (int i = 0; i < numUsers.size(); i++) {
				bWriter.write(numUsers.get(i) + ";" + tpSend.get(i) + ";" + tpReceived.get(i));
				bWriter.newLine();
			}

			bWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void loadProperties() {

	}

	@Override
	protected int getNumOfExperiments() {
		return EXPERIMENT_STEPS;
	}

}
