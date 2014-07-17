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
package org.spotter.detection.dboverhead;

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
import org.aim.api.instrumentation.description.InstrumentationDescriptionBuilder;
import org.aim.api.measurement.dataset.Dataset;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.api.measurement.dataset.ParameterSelection;
import org.aim.api.measurement.utils.MeasurementDataUtils;
import org.aim.artifacts.probes.ResponsetimeProbe;
import org.aim.artifacts.records.DBStatisticsRecrod;
import org.aim.artifacts.records.ResponseTimeRecord;
import org.aim.artifacts.scopes.ServletScope;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.LpeNumericUtils;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.SpotterResult;

public class DBOverheadDetectionController extends AbstractDetectionController {

	private static final int NUM_EXPERIMENTS = 1;

	public DBOverheadDetectionController(IExtension<IDetectionController> provider) {
		super(provider);
		// TODO Auto-generated constructor stub
	}

	public void loadProperties() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException {
		instrumentApplication(getInstrumentationDescription());
		runExperiment(DBOverheadDetectionController.class, 1);
		uninstrumentApplication();

	}

	private InstrumentationDescription getInstrumentationDescription() {
		InstrumentationDescriptionBuilder idBuilder = new InstrumentationDescriptionBuilder();
		return idBuilder.addAPIInstrumentation(ServletScope.class).addProbe(ResponsetimeProbe.class).entityDone()
				.build();

	}

	@Override
	protected SpotterResult analyze(DatasetCollection data) {
		SpotterResult result = new SpotterResult();
		// singleUserAnalysis(data, result);
		// multiUserAnalysis(data, result);

		calculateDBLocks(data, result);

		// TODO Auto-generated method stub
		return result;
	}

	private void calculateDBLocks(DatasetCollection data, SpotterResult result) {
		Dataset dbDataset = data.getDataSet(DBStatisticsRecrod.class);
		Dataset rtDataset = data.getDataSet(ResponseTimeRecord.class);
		List<Integer> keys = new ArrayList<Integer>(dbDataset.getValueSet(NUMBER_OF_USERS, Integer.class));
		Collections.sort(keys);

		for (String node : dbDataset.getValueSet(DBStatisticsRecrod.PAR_PROCESS_ID, String.class)) {
			String fileName = node.substring(13, 26);
			List<Double> lockWaitsValues = new ArrayList<>();
			for (Integer numUsers : keys) {

				List<Long> utils = ParameterSelection.newSelection().select(NUMBER_OF_USERS, numUsers)
						.select(DBStatisticsRecrod.PAR_PROCESS_ID, node).applyTo(dbDataset)
						.getValues(DBStatisticsRecrod.PAR_NUM_LOCK_WAITS, Long.class);
				double cpuUtilMean = LpeNumericUtils.average(utils);
				lockWaitsValues.add(cpuUtilMean);
			}

			writeDBLocksToFile(result, keys, lockWaitsValues, "LockWaits-" + fileName);

			List<Double> lockTimesValues = new ArrayList<>();
			for (Integer numUsers : keys) {

				Integer numRequests = ParameterSelection.newSelection().select(NUMBER_OF_USERS, numUsers)
						.unequal(ResponseTimeRecord.PAR_OPERATION, "Action_Transaction")
						.unequal(ResponseTimeRecord.PAR_OPERATION, "vuser_init_Transaction")
						.unequal(ResponseTimeRecord.PAR_OPERATION, "vuser_end_Transaction").applyTo(rtDataset).size();

				List<Long> lockTimes = ParameterSelection.newSelection().select(NUMBER_OF_USERS, numUsers)
						.select(DBStatisticsRecrod.PAR_PROCESS_ID, node).applyTo(dbDataset)
						.getValues(DBStatisticsRecrod.PAR_LOCK_TIME, Long.class);
				List<Long> lockWaits = ParameterSelection.newSelection().select(NUMBER_OF_USERS, numUsers)
						.select(DBStatisticsRecrod.PAR_PROCESS_ID, node).applyTo(dbDataset)
						.getValues(DBStatisticsRecrod.PAR_NUM_LOCK_WAITS, Long.class);

				Collections.sort(lockTimes);

				List<Long> lockTimeDiffs = new ArrayList<>();
				Long prevLockTime = -1L;
				for (Long lockTime : lockTimes) {
					if (prevLockTime == -1L) {
						prevLockTime = lockTime;
						continue;
					}
					lockTimeDiffs.add(lockTime - prevLockTime);
				}

				double lockTimeMean = LpeNumericUtils.average(lockTimeDiffs);

				double lockTime = LpeNumericUtils.max(lockTimes) - LpeNumericUtils.min(lockTimes);

				double numLocks = LpeNumericUtils.max(lockWaits) - LpeNumericUtils.min(lockWaits);

				lockTimesValues.add(lockTime / numLocks);
			}

			writeDBLocktimesToFile(result, keys, lockTimesValues, "LockTimes-" + fileName);
		}
	}

	private void writeDBLocksToFile(SpotterResult result, List<Integer> numUsers, List<Double> lockWaits,
			String fileName) {
		PipedOutputStream outStream = null;
		PipedInputStream inStream = null;
		try {
			outStream = new PipedOutputStream();
			inStream = new PipedInputStream(outStream);
			storeTextResource(fileName, result, inStream);
			BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(outStream));
			bWriter.write("NumUsers;LockWaits");
			bWriter.newLine();
			for (int i = 0; i < numUsers.size(); i++) {
				bWriter.write(numUsers.get(i) + ";" + lockWaits.get(i));
				bWriter.newLine();
			}

			bWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeDBLocktimesToFile(SpotterResult result, List<Integer> numUsers, List<Double> lockWaits,
			String fileName) {
		PipedOutputStream outStream = null;
		PipedInputStream inStream = null;
		try {
			outStream = new PipedOutputStream();
			inStream = new PipedInputStream(outStream);
			storeTextResource(fileName, result, inStream);
			BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(outStream));
			bWriter.write("NumUsers;LockTime");
			bWriter.newLine();
			for (int i = 0; i < numUsers.size(); i++) {
				bWriter.write(numUsers.get(i) + ";" + lockWaits.get(i));
				bWriter.newLine();
			}

			bWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void singleUserAnalysis(DatasetCollection data, SpotterResult result) {
		Dataset rtDataset = data.getDataSet(ResponseTimeRecord.class);
		Dataset dbDataset = data.getDataSet(DBStatisticsRecrod.class);

		Map<String, List<Long>> operationRequestAmountMapping = new HashMap<String, List<Long>>();

		for (String processId : dbDataset.getValueSet(DBStatisticsRecrod.PAR_PROCESS_ID, String.class)) {

			List<ResponseTimeRecord> rtRecords = ParameterSelection
					.newSelection()
					.unequal(ResponseTimeRecord.PAR_OPERATION,
							"javax.servlet.http.HttpServlet.service(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)")
					.unequal(ResponseTimeRecord.PAR_OPERATION,
							"javax.servlet.http.HttpServlet.service(javax.servlet.ServletRequest,javax.servlet.ServletResponse)")
					.unequal(ResponseTimeRecord.PAR_OPERATION, "browseCategory")
					.unequal(ResponseTimeRecord.PAR_OPERATION, "searchProductEmptyResult")
					.unequal(ResponseTimeRecord.PAR_OPERATION, "home")
					.unequal(ResponseTimeRecord.PAR_OPERATION, "viewProductDetails")
					.unequal(ResponseTimeRecord.PAR_OPERATION, "purchase")
					.unequal(ResponseTimeRecord.PAR_OPERATION, "login")
					.unequal(ResponseTimeRecord.PAR_OPERATION, "addToBasket")
					.unequal(ResponseTimeRecord.PAR_OPERATION, "checkout")
					.unequal(ResponseTimeRecord.PAR_OPERATION, "searchProduct").applyTo(rtDataset)
					.getRecords(ResponseTimeRecord.class);
			List<DBStatisticsRecrod> dbRecords = ParameterSelection.newSelection()
					.select(DBStatisticsRecrod.PAR_PROCESS_ID, processId).applyTo(dbDataset)
					.getRecords(DBStatisticsRecrod.class);

			MeasurementDataUtils.sortRecordsAscending(rtRecords, ResponseTimeRecord.PAR_TIMESTAMP);
			MeasurementDataUtils.sortRecordsAscending(dbRecords, DBStatisticsRecrod.PAR_TIMESTAMP);

			int dbRecIx = 0;
			for (int i = 1; i < rtRecords.size() - 1; i++) {

				// if (dbRecords.size() >= dbRecords.size()) {
				// break;
				// }
				long prevEndTime = rtRecords.get(i - 1).getTimeStamp() + rtRecords.get(i - 1).getResponseTime();
				long rtStartTime = rtRecords.get(i).getTimeStamp();
				long rtEndTime = rtRecords.get(i).getTimeStamp() + rtRecords.get(i).getResponseTime();
				long followingStartTime = rtRecords.get(i + 1).getTimeStamp();

				// find DB stats record with a timestamp which is smaller than
				// the
				// start timestamp of the current request
				// (but greater than the end timestamp of the previous request)
				while (dbRecIx < dbRecords.size() && dbRecords.get(dbRecIx).getTimeStamp() < rtStartTime) {
					dbRecIx++;
				}
				if (dbRecIx == 0) {
					continue;
				}
				dbRecIx--;
				int beforeDBIndex = dbRecIx;
				if (prevEndTime >= dbRecords.get(beforeDBIndex).getTimeStamp()) {
					continue;
				}

				// find DB stats record with a timestamp which is greater than
				// the
				// end timestamp of the current request
				// (but smaller than the start timestamp of the following
				// request)
				while (dbRecIx < dbRecords.size() && dbRecords.get(dbRecIx).getTimeStamp() < rtEndTime) {
					dbRecIx++;
				}
				if (dbRecIx >= dbRecords.size()) {
					break;
				}
				int afterDBIndex = dbRecIx;
				if (followingStartTime <= dbRecords.get(afterDBIndex).getTimeStamp()) {
					continue;
				}

				long dbRequestCount = dbRecords.get(afterDBIndex).getNumQueueries()
						- dbRecords.get(beforeDBIndex).getNumQueueries();
				if (dbRequestCount > 0) {
					if (!operationRequestAmountMapping.containsKey(rtRecords.get(i).getOperation())) {
						operationRequestAmountMapping.put(rtRecords.get(i).getOperation(), new ArrayList<Long>());
					}

					operationRequestAmountMapping.get(rtRecords.get(i).getOperation()).add(dbRequestCount);
				}

			}

		}

		result.setDetected(false);
		for (String operation : operationRequestAmountMapping.keySet()) {
			double meanQueriesPerTransaction = LpeNumericUtils.average(operationRequestAmountMapping.get(operation));
			long minNumQueueries = LpeNumericUtils.min(operationRequestAmountMapping.get(operation));
			long maxNumQueueries = LpeNumericUtils.max(operationRequestAmountMapping.get(operation));
			long range = maxNumQueueries - minNumQueueries;

			if (meanQueriesPerTransaction >= 3 || range >= 3) {
				result.setDetected(true);
				result.addMessage("******************************************************");
				result.addMessage("Transaciton: " + operation);
				result.addMessage("Queries per Transaction: " + meanQueriesPerTransaction);
				result.addMessage("Range of Number of Queueries: " + minNumQueueries + " to " + maxNumQueueries);
			} else {
				result.addMessage("******************************************************");
				result.addMessage("Transaciton: " + operation);
				result.addMessage("Queries per Transaction: " + meanQueriesPerTransaction);
				result.addMessage("Range of Number of Queueries: " + minNumQueueries + " to " + maxNumQueueries);
			}
		}
	}

	private void multiUserAnalysis(DatasetCollection data, SpotterResult result) {
		Dataset responseTimes = data.getDataSet(ResponseTimeRecord.class);

		List<ResponseTimeRecord> rtRecords = responseTimes.getRecords(ResponseTimeRecord.class);
		MeasurementDataUtils.sortRecordsAscending(rtRecords, ResponseTimeRecord.PAR_TIMESTAMP);

		int numRecords = rtRecords.size();

		long rtStartTime = rtRecords.get(0).getTimeStamp();
		long rtEndTime = rtRecords.get(rtRecords.size() - 1).getTimeStamp();

		long timeSpan = rtEndTime - rtStartTime;

		double clientRequestThroughput = (double) (numRecords / timeSpan) * 1000.0;

		Dataset dbDataset = data.getDataSet(DBStatisticsRecrod.class);

		List<DBStatisticsRecrod> dbRecords = dbDataset.getRecords(DBStatisticsRecrod.class);

		MeasurementDataUtils.sortRecordsAscending(dbRecords, DBStatisticsRecrod.PAR_TIMESTAMP);

		long dbStartTime = -1;
		long dbEndTime = -1;
		long dbStartRequestCount = -1;
		long dbEndRequestCount = -1;

		if (dbRecords.get(0).getTimeStamp() >= rtStartTime) {
			dbStartTime = dbRecords.get(0).getTimeStamp();
			dbStartRequestCount = dbRecords.get(0).getNumQueueries();
		} else {
			for (DBStatisticsRecrod dbRec : dbRecords) {
				if (dbRec.getTimeStamp() > rtStartTime) {
					dbStartTime = dbRec.getTimeStamp();
					dbStartRequestCount = dbRec.getNumQueueries();
					break;
				}
			}
		}

		if (dbRecords.get(dbRecords.size() - 1).getTimeStamp() <= rtEndTime) {
			dbEndTime = dbRecords.get(dbRecords.size() - 1).getTimeStamp();
			dbEndRequestCount = dbRecords.get(dbRecords.size() - 1).getNumQueueries();
		} else {
			for (int i = dbRecords.size() - 1; i >= 0; i--) {
				if (dbRecords.get(i).getTimeStamp() < rtEndTime) {
					dbEndTime = dbRecords.get(i).getTimeStamp();
					dbEndRequestCount = dbRecords.get(i).getNumQueueries();
					break;
				}
			}
		}

		double dbRequestThroughput = (double) ((dbEndRequestCount - dbStartRequestCount) / (dbEndTime - dbStartTime)) * 1000.0;

		if (dbRequestThroughput >= 3 * clientRequestThroughput) {
			result.setDetected(true);
			result.addMessage("DB throughput is " + (double) (dbRequestThroughput / clientRequestThroughput)
					+ " times higher than the user request throughput!");
		} else {
			result.setDetected(false);
		}
	}

	@Override
	protected int getNumOfExperiments() {
		return NUM_EXPERIMENTS;
	}

}
