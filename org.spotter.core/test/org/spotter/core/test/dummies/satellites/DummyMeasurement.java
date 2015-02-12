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
package org.spotter.core.test.dummies.satellites;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.aim.api.exceptions.MeasurementException;
import org.aim.api.measurement.AbstractRecord;
import org.aim.api.measurement.MeasurementData;
import org.aim.artifacts.records.CPUUtilizationRecord;
import org.aim.artifacts.records.ResponseTimeRecord;
import org.aim.description.InstrumentationDescription;
import org.lpe.common.extension.IExtension;
import org.spotter.core.measurement.AbstractMeasurementAdapter;

public class DummyMeasurement extends AbstractMeasurementAdapter{

	public static final int NUM_RECORDS = 60;
	
	public boolean initialized = false;
	public boolean enabled = false;
	public boolean reportStored = false;
	
	public DummyMeasurement(IExtension<?> provider) {
		super(provider);
	}


	@Override
	public void enableMonitoring() throws MeasurementException {
		reportStored=false;
		enabled=true;
	}

	@Override
	public void disableMonitoring() throws MeasurementException {
		enabled=false;
	}

	@Override
	public MeasurementData getMeasurementData() throws MeasurementException {
		List<AbstractRecord> records = new ArrayList<>();
		Random rand = new Random();

		for (long i = 0; i < NUM_RECORDS/3; i++) {
			ResponseTimeRecord rtRecord = new ResponseTimeRecord(System.currentTimeMillis() + i * 10L, "operation-"
					+ (i % 5), (long) (rand.nextDouble() * 100L));
			CPUUtilizationRecord cpuRecord = new CPUUtilizationRecord(System.currentTimeMillis() + i * 10L, "CPU-"
					+ (i % 2), rand.nextDouble());
			CPUUtilizationRecord cpuRecordAgg = new CPUUtilizationRecord(System.currentTimeMillis() + i * 10L, CPUUtilizationRecord.RES_CPU_AGGREGATED, rand.nextDouble());
			records.add(rtRecord);
			records.add(cpuRecord);

			records.add(cpuRecordAgg);

		}

		MeasurementData mData = new MeasurementData();
		mData.setRecords(records);

		return mData;
	}

	@Override
	public void pipeToOutputStream(OutputStream oStream) throws MeasurementException {
		BufferedWriter writer = null;
		try {

			List<AbstractRecord> recordList = getMeasurementData().getRecords();
			writer = new BufferedWriter(new OutputStreamWriter(oStream), 1024);

			for (AbstractRecord rec : recordList) {
				writer.write(rec.toString());
				writer.newLine();
			}

		} catch (IOException e) {
			throw new MeasurementException(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					throw new MeasurementException(e);
				}
			}
		}

	}

	@Override
	public void initialize() throws MeasurementException {
		initialized = true;
	}

	@Override
	public long getCurrentTime() {
		return System.currentTimeMillis();
	}

	@Override
	public void storeReport(String path) throws MeasurementException {
		reportStored=true;
	}


	@Override
	public void prepareMonitoring(InstrumentationDescription monitoringDescription) throws MeasurementException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void resetMonitoring() throws MeasurementException {
		// TODO Auto-generated method stub
		
	}

}
