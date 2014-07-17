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
package org.spotter.detection.emptysemitrucks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.instrumentation.description.InstrumentationDescription;
import org.aim.api.instrumentation.description.InstrumentationDescriptionBuilder;
import org.aim.api.measurement.AbstractRecord;
import org.aim.api.measurement.dataset.Dataset;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.api.measurement.dataset.ParameterSelection;
import org.aim.api.measurement.utils.MeasurementDataUtils;
import org.aim.artifacts.probes.JmsCommunicationProbe;
import org.aim.artifacts.probes.JmsMessageSizeProbe;
import org.aim.artifacts.probes.ThreadTracingProbe;
import org.aim.artifacts.records.JmsMessageSizeRecord;
import org.aim.artifacts.records.JmsRecord;
import org.aim.artifacts.records.ThreadTracingRecord;
import org.aim.artifacts.scopes.JmsScope;
import org.aim.artifacts.scopes.ServletScope;
import org.lpe.common.extension.IExtension;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.SpotterResult;

public class EmptySemiTrucksDetectionController extends AbstractDetectionController {
	private static final long NANO_TO_MILLI = 1000000L;
	private static final int NUM_EXPERIMENTS = 1;

	public EmptySemiTrucksDetectionController(IExtension<IDetectionController> provider) {
		super(provider);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadProperties() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException {
		instrumentApplication(getInstrumentationDescription());
		runExperiment(EmptySemiTrucksDetectionController.class, 1);
		uninstrumentApplication();
	}

	private InstrumentationDescription getInstrumentationDescription() {
		InstrumentationDescriptionBuilder idBuilder = new InstrumentationDescriptionBuilder();
		return idBuilder.addFullTraceInstrumentation().addRootAPI(ServletScope.class)
				.addProbe(ThreadTracingProbe.class).entityDone().addAPIInstrumentation(JmsScope.class)
				.addProbe(JmsMessageSizeProbe.class).addProbe(JmsCommunicationProbe.class).entityDone().build();
	}

	@Override
	protected SpotterResult analyze(DatasetCollection data) {
		SpotterResult result = new SpotterResult();
		result.setDetected(false);

		Dataset threadTracingDataset = data.getDataSet(ThreadTracingRecord.class);
		Dataset messagingDataset = data.getDataSet(JmsRecord.class);
		Dataset messageSizesDataset = data.getDataSet(JmsMessageSizeRecord.class);

		if (threadTracingDataset == null || threadTracingDataset.size() == 0) {
			result.addMessage("No trace records found!");
			result.setDetected(false);
			return result;
		}

		if (messageSizesDataset == null || messageSizesDataset.size() == 0) {
			result.addMessage("No message size records found!");
			result.setDetected(false);
			return result;
		}

		if (messagingDataset == null || messagingDataset.size() == 0) {
			result.addMessage("No messaging records found!");
			result.setDetected(false);
			return result;
		}

		for (String processId : messagingDataset.getValueSet(AbstractRecord.PAR_PROCESS_ID, String.class)) {
			Dataset processRelatedTraceDataset = ParameterSelection.newSelection()
					.select(AbstractRecord.PAR_PROCESS_ID, processId).applyTo(threadTracingDataset);

			Dataset processRelatedMessagingDataset = ParameterSelection.newSelection()
					.select(AbstractRecord.PAR_PROCESS_ID, processId).applyTo(messagingDataset);

			if (processRelatedTraceDataset == null || processRelatedTraceDataset.size() == 0) {
				result.addMessage("No trace records found for processId " + processId + "!");
				continue;
			}

			if (processRelatedMessagingDataset == null || processRelatedMessagingDataset.size() == 0) {
				result.addMessage("No messaging records found for processId " + processId + "!");
				continue;
			}

			List<Trace> traces = extractTraces(processRelatedTraceDataset, processRelatedMessagingDataset,
					messageSizesDataset);

			writeTracesToFile(result, traces, "traces");

			List<AggTrace> aggregatedTraces = aggregateTraces(traces);

			writeTracesToFile(result, aggregatedTraces, "traces-agg-" + processId.substring(processId.indexOf("@") + 1));
			List<ESTCandidate> estCandidates = new ArrayList<>();
			for (AggTrace aggTrace : aggregatedTraces) {
				findESTCandidates(estCandidates, aggTrace, 1);
			}

			for (ESTCandidate candidate : estCandidates) {
				result.setDetected(true);
				double savingPotential = candidate.getAggTrace().getOverhead() * (candidate.getLoopCount() - 1);
				double transmittedBytes = (candidate.getAggTrace().getPayload() + candidate.getAggTrace().getOverhead())
						* candidate.getLoopCount();
				result.addMessage("*************************************************************");
				result.addMessage("*************************************************************");
				result.addMessage("** Empty Semi Trucks Candidate **");
				result.addMessage("Avg Payload: " + candidate.getAggTrace().getPayload() + " Bytes");
				result.addMessage("Avg Messaging Overhead: " + candidate.getAggTrace().getOverhead() + " Bytes");
				result.addMessage("Loop count: " + candidate.getLoopCount());
				result.addMessage("Saving potential: " + savingPotential + " Bytes");
				result.addMessage("Saving potential %: " + (100.0 * savingPotential / transmittedBytes) + " %");

				result.addMessage("TRACE: ");
				result.addMessage(candidate.getAggTrace().getPathToParentString());
				result.addMessage("*************************************************************");
				result.addMessage("*************************************************************");
			}

		}

		// search for loop

		return result;

	}

	private void findESTCandidates(List<ESTCandidate> candidates, AggTrace aggTrace, int loopCount) {
		if (aggTrace.isSendMethod() && loopCount > 1) {
			ESTCandidate candidate = new ESTCandidate();
			candidate.setAggTrace(aggTrace);
			candidate.setLoopCount(loopCount);
			candidates.add(candidate);
		} else {
			if (aggTrace.isLoop()) {
				loopCount *= aggTrace.getLoopCount();
			}

			for (AggTrace subTrace : aggTrace.getSubTraces()) {
				findESTCandidates(candidates, subTrace, loopCount);
			}

		}
	}

	private List<AggTrace> aggregateTraces(List<Trace> traces) {
		Map<Trace, List<Trace>> traceGrouping = new HashMap<Trace, List<Trace>>();
		for (Trace rootTrace : traces) {
			List<Trace> groupTraces = null;
			if (!traceGrouping.containsKey(rootTrace)) {
				groupTraces = new ArrayList<>();
				traceGrouping.put(rootTrace, groupTraces);
			} else {
				groupTraces = traceGrouping.get(rootTrace);
			}

			groupTraces.add(rootTrace);

		}
		List<AggTrace> aggregatedTraces = new ArrayList<>();

		for (Trace representative : traceGrouping.keySet()) {

			calculateAverageTrace(representative, traceGrouping.get(representative));

			aggregatedTraces.add(AggTrace.fromTrace(representative));
		}

		return aggregatedTraces;

	}

	private void calculateAverageTrace(Trace reprTrace, List<Trace> tracesList) {
		List<Iterator<Trace>> iterators = new ArrayList<>();
		for (Trace tr : tracesList) {
			iterators.add(tr.iterator());
		}

		Iterator<Trace> itMaster = reprTrace.iterator();
		long size = tracesList.size();
		while (itMaster.hasNext()) {
			reprTrace = itMaster.next();
			long avgPayload = 0;
			long avgOverhead = 0;
			for (Iterator<Trace> it : iterators) {
				Trace subTrace = it.next();
				avgPayload += subTrace.getPayload();
				avgOverhead += subTrace.getOverhead();
			}
			reprTrace.setPayload(avgPayload / size);
			reprTrace.setOverhead(avgOverhead / size);

		}
	}

	private void writeTracesToFile(SpotterResult result, List<?> traces, String fileName) {
		PipedOutputStream outStream = null;
		PipedInputStream inStream = null;
		try {
			outStream = new PipedOutputStream();
			inStream = new PipedInputStream(outStream);
			storeTextResource(fileName, result, inStream);
			BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(outStream));
			for (Object trace : traces) {
				bWriter.write(trace.toString());
				bWriter.newLine();
				bWriter.newLine();
			}
			bWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<Trace> extractTraces(Dataset threadTracingDataset, Dataset messagingDataset,
			Dataset messageSizesDataset) {
		List<Trace> traces = new ArrayList<>();
		for (Long threadId : threadTracingDataset.getValueSet(ThreadTracingRecord.PAR_THREAD_ID, Long.class)) {
			List<ThreadTracingRecord> threadRecords = ParameterSelection.newSelection()
					.select(ThreadTracingRecord.PAR_THREAD_ID, threadId).applyTo(threadTracingDataset)
					.getRecords(ThreadTracingRecord.class);
			MeasurementDataUtils.sortRecordsAscending(threadRecords, ThreadTracingRecord.PAR_CALL_ID);

			Trace trace = null;
			long nextValidTimestamp = Long.MIN_VALUE;
			Trace previousTraceRoot = null;
			ThreadTracingRecord sendMethodRecord = null;
			for (ThreadTracingRecord ttRecord : threadRecords) {
				if (sendMethodRecord == null || sendMethodRecord.getExitNanoTime() <= ttRecord.getEnterNanoTime()) {
					sendMethodRecord = null;

					long durationMs = (ttRecord.getExitNanoTime() - ttRecord.getEnterNanoTime()) / NANO_TO_MILLI;
					if (ttRecord.getTimeStamp() < nextValidTimestamp) {
						continue;
					}
					String operation = ttRecord.getOperation();
					long callId = ttRecord.getCallId();
					String processId = ttRecord.getProcessId();

					if (trace == null) {
						trace = new Trace(operation);
						previousTraceRoot = trace;
					} else if (trace.getExitTime() >= ttRecord.getExitNanoTime()
							&& trace.getExitTime() >= ttRecord.getEnterNanoTime()) {
						// sub-method
						trace = new Trace(trace, operation);
					} else {

						while (trace != null && trace.getExitTime() <= ttRecord.getEnterNanoTime()) {
							trace = trace.getParent();
						}
						Trace parent = trace;
						trace = new Trace(parent, operation);
						if (parent == null) {
							if (previousTraceRoot != null) {
								traces.add(previousTraceRoot);
							}
							previousTraceRoot = trace;
						}

					}
					if (operation.endsWith("send(javax.jms.Message)")) {
						sendMethodRecord = ttRecord;
					}
					setPayloadSizes(trace, operation, processId, callId, messagingDataset, messageSizesDataset);
					trace.setStartTime(ttRecord.getEnterNanoTime());
					trace.setExitTime(ttRecord.getExitNanoTime());
				}
			}
			if (previousTraceRoot != null) {
				traces.add(previousTraceRoot);
			}

		}

		return traces;
	}

	private void setPayloadSizes(Trace trace, String operation, String processId, long callId,
			Dataset messagingDataset, Dataset messageSizesDataset) {

		if (operation.endsWith("send(javax.jms.Message)")) {
			JmsMessageSizeRecord mSizeRecord = getMessageSizeRecord(processId, callId, messagingDataset,
					messageSizesDataset);
			if (mSizeRecord != null) {
				trace.setSendMethod(true);
				trace.setPayload(mSizeRecord.getBodySize());
				trace.setOverhead(mSizeRecord.getSize() - mSizeRecord.getBodySize());
			}

		}

	}

	private JmsMessageSizeRecord getMessageSizeRecord(String processId, long callId, Dataset messagingDataset,
			Dataset messageSizesDataset) {
		ParameterSelection messageCorrelationSelection = ParameterSelection.newSelection().select(
				AbstractRecord.PAR_CALL_ID, callId);
		Dataset messageCorrelationDataset = messageCorrelationSelection.applyTo(messagingDataset);

		if (messageCorrelationDataset == null || messageCorrelationDataset.size() == 0) {
			return null;
		}
		String correlationId = messageCorrelationDataset.getValues(JmsRecord.PAR_MSG_CORRELATION_HASH, String.class)
				.get(0);

		ParameterSelection messageSizeSelection = ParameterSelection.newSelection().select(
				JmsMessageSizeRecord.PAR_MSG_CORRELATION_HASH, correlationId);
		Dataset mSizeDataset = messageSizeSelection.applyTo(messageSizesDataset);

		if (mSizeDataset == null || mSizeDataset.size() == 0) {
			return null;
		}

		return mSizeDataset.getRecords(JmsMessageSizeRecord.class).get(0);

	}

	@Override
	protected int getNumOfExperiments() {
		return NUM_EXPERIMENTS;
	}

}
