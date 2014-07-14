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
package org.spotter.detection.highmem;

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
import org.aim.api.measurement.dataset.Dataset;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.api.measurement.dataset.ParameterSelection;
import org.aim.api.measurement.utils.MeasurementDataUtils;
import org.aim.artifacts.probes.MemoryFootprintProbe;
import org.aim.artifacts.probes.ThreadTracingProbe;
import org.aim.artifacts.records.GCSamplingStatsRecord;
import org.aim.artifacts.records.MemoryFootprintRecord;
import org.aim.artifacts.records.ThreadTracingRecord;
import org.aim.artifacts.sampler.GarbageCollectionSampler;
import org.aim.artifacts.scopes.ServletScope;
import org.lpe.common.extension.IExtension;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.SpotterResult;

/**
 * Searches for a high memory footprint in a call tree.
 * 
 * @author Alexander Wert
 * 
 */
public class HighMemFootprintDetectionController extends AbstractDetectionController {
	private static final long NANO_TO_MILLI = 1000000L;
	private long gcSamplingDelay;
	private static final int NUM_EXPERIMENTS = 1;

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *            extension provider
	 */
	public HighMemFootprintDetectionController(IExtension<IDetectionController> provider) {
		super(provider);
	}

	@Override
	public void loadProperties() {
		String gcSamplingDelayStr = getProblemDetectionConfiguration().getProperty(
				HighMemFootprintExtension.GC_SAMPLING_DELAY_KEY);
		gcSamplingDelay = gcSamplingDelayStr != null ? Long.parseLong(gcSamplingDelayStr)
				: HighMemFootprintExtension.GC_SAMPLING_DELAY_DEFAULT;

	}

	@Override
	protected void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException {
		instrumentApplication(getInstrumentationDescription());
		runExperiment(HighMemFootprintDetectionController.class, 1);
		uninstrumentApplication();
	}

	private InstrumentationDescription getInstrumentationDescription() {

		InstrumentationDescriptionBuilder idBuilder = new InstrumentationDescriptionBuilder();
		return idBuilder.addFullTraceInstrumentation().addRootAPI(ServletScope.class)
				.addProbe(ThreadTracingProbe.class).addProbe(MemoryFootprintProbe.class).entityDone()
				.addSamplingInstruction(GarbageCollectionSampler.class, gcSamplingDelay).build();
	}

	@Override
	protected SpotterResult analyze(DatasetCollection data) {
		SpotterResult result = new SpotterResult();

		Dataset threadTracingDataset = data.getDataSet(ThreadTracingRecord.class);

		if (threadTracingDataset == null || threadTracingDataset.size() == 0) {
			result.addMessage("No trace records found!");
			result.setDetected(false);
			return result;
		}

		Dataset memoryFootprintDataset = data.getDataSet(MemoryFootprintRecord.class);

		if (memoryFootprintDataset == null || memoryFootprintDataset.size() == 0) {
			result.addMessage("No memory footprint records found!");
			result.setDetected(false);
			return result;
		}

		Dataset gcStats = data.getDataSet(GCSamplingStatsRecord.class);

		if (gcStats == null || gcStats.size() == 0) {
			result.addMessage("No gcStats records found!");
			result.setDetected(false);
			return result;
		}

		List<Trace> traces = extractTraces(threadTracingDataset, memoryFootprintDataset, gcStats);

		writeTracesToFile(result, traces, "traces");

		List<AggTrace> aggregatedTraces = aggregateTraces(traces);

		writeTracesToFile(result, aggregatedTraces, "traces-agg");

		return result;
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
			aggregateTraceGroup(representative, traceGrouping.get(representative));
			aggregatedTraces.add(AggTrace.fromTrace(representative));
		}

		return aggregatedTraces;

	}

	private void aggregateTraceGroup(Trace reprTrace, List<Trace> list) {
		List<Iterator<Trace>> iterators = new ArrayList<>();
		for (Trace tr : list) {
			iterators.add(tr.iterator());
		}
		Iterator<Trace> itMaster = reprTrace.iterator();
		while (itMaster.hasNext()) {
			reprTrace = itMaster.next();
			long sum = 0;
			for (Iterator<Trace> it : iterators) {
				Trace subTrace = it.next();
				sum += subTrace.getTotalMemoryFootprint();
			}
			reprTrace.setTotalMemoryFootprint(sum / list.size());

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

	private List<Trace> extractTraces(Dataset threadTracingDataset, Dataset memoryFootprintDataset, Dataset gcStats) {
		List<Trace> traces = new ArrayList<>();
		for (Long threadId : threadTracingDataset.getValueSet(ThreadTracingRecord.PAR_THREAD_ID, Long.class)) {
			List<ThreadTracingRecord> threadRecords = ParameterSelection.newSelection()
					.select(ThreadTracingRecord.PAR_THREAD_ID, threadId).applyTo(threadTracingDataset)
					.getRecords(ThreadTracingRecord.class);
			MeasurementDataUtils.sortRecordsAscending(threadRecords, ThreadTracingRecord.PAR_CALL_ID);

			Trace trace = null;
			ParameterSelection memoryFPSelection = new ParameterSelection();
			long nextValidTimestamp = Long.MIN_VALUE;
			Trace previousTraceRoot = null;
			for (ThreadTracingRecord ttRecord : threadRecords) {
				long durationMs = (ttRecord.getExitNanoTime() - ttRecord.getEnterNanoTime()) / NANO_TO_MILLI;
				if (ttRecord.getTimeStamp() < nextValidTimestamp) {
					continue;
				}
				String operation = ttRecord.getOperation();

				if (trace == null) {
					if (gcHappendDuringTraceExecution(ttRecord, gcStats, durationMs)) {
						nextValidTimestamp = ttRecord.getTimeStamp() + durationMs;
						continue;
					}
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
				memoryFPSelection.select(MemoryFootprintRecord.PAR_OPERATION, operation).select(
						MemoryFootprintRecord.PAR_CALL_ID, ttRecord.getCallId());
				MemoryFootprintRecord fpRecord = memoryFPSelection.applyTo(memoryFootprintDataset)
						.getRecords(MemoryFootprintRecord.class).get(0);
				long footprint = fpRecord.getMemoryUsedAfter() - fpRecord.getMemoryUsedBefore();
				trace.setStartTime(ttRecord.getEnterNanoTime());
				trace.setExitTime(ttRecord.getExitNanoTime());
				trace.setTotalMemoryFootprint(footprint);
				if (footprint < 0) {
					previousTraceRoot = null;
				}
			}
			if (previousTraceRoot != null) {
				traces.add(previousTraceRoot);
			}

		}

		return traces;
	}

	private boolean gcHappendDuringTraceExecution(ThreadTracingRecord ttRecord, Dataset gcStats, long durationMs) {

		ParameterSelection gcStatsSelection;
		Dataset selectedGcStats;
		List<GCSamplingStatsRecord> gcRecords;

		// get before trace DC statistics
		gcStatsSelection = ParameterSelection.newSelection().smallerOrEquals(GCSamplingStatsRecord.PAR_TIMESTAMP,
				ttRecord.getTimeStamp());
		selectedGcStats = gcStatsSelection.applyTo(gcStats);
		if (selectedGcStats == null || gcStats.size() == 0) {
			return true;
		}
		gcRecords = gcStatsSelection.applyTo(gcStats).getRecords(GCSamplingStatsRecord.class);
		MeasurementDataUtils.sortRecordsDescending(gcRecords, GCSamplingStatsRecord.PAR_TIMESTAMP);
		long newGenCountBefore = gcRecords.get(0).getGcNewGenCount();
		long oldGenCountBefore = gcRecords.get(0).getGcOldGenCount();

		// get after trace GC statistics
		gcStatsSelection = ParameterSelection.newSelection().largerOrEquals(GCSamplingStatsRecord.PAR_TIMESTAMP,
				ttRecord.getTimeStamp() + durationMs);
		selectedGcStats = gcStatsSelection.applyTo(gcStats);
		if (selectedGcStats == null || gcStats.size() == 0) {
			return true;
		}
		gcRecords = gcStatsSelection.applyTo(gcStats).getRecords(GCSamplingStatsRecord.class);
		MeasurementDataUtils.sortRecordsAscending(gcRecords, GCSamplingStatsRecord.PAR_TIMESTAMP);
		long newGenCountAfter = gcRecords.get(0).getGcNewGenCount();
		long oldGenCountAfter = gcRecords.get(0).getGcOldGenCount();
		if (newGenCountAfter > newGenCountBefore || oldGenCountAfter > oldGenCountBefore) {
			return true;
		}
		return false;
	}

	@Override
	protected int getNumOfExperiments() {
		return NUM_EXPERIMENTS;
	}
}
