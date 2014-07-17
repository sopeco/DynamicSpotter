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
package org.spotter.core.measurement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import org.aim.api.exceptions.MeasurementException;
import org.aim.api.measurement.AbstractRecord;

/**
 * Pipes measurement data from a measurement controller to a blocking queue.
 * This task also relativizes timestamps of the records.
 * 
 * @author Alexander Wert
 * 
 */
public class PipeDataTask implements Runnable {
	private AbstractMeasurementController mController;
	private LinkedBlockingQueue<AbstractRecord> records;
	private Semaphore semaphore;

	/**
	 * Constructor.
	 * 
	 * @param semaphore
	 *            semaphore to use to notify when piping has been completed
	 * @param mController
	 *            controller where to read data from
	 * @param records
	 *            blocking queue where to write records to
	 */
	public PipeDataTask(Semaphore semaphore, IMeasurementController mController,
			LinkedBlockingQueue<AbstractRecord> records) {
		this.mController = (AbstractMeasurementController) mController;
		this.records = records;
		this.semaphore = semaphore;

		if (mController == null || records == null || semaphore == null) {
			throw new IllegalArgumentException("At least one argument is null!");
		}
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		try {

			executeTask();

			semaphore.release();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	protected void executeTask() throws MeasurementException {

		try {
			final PipedInputStream dataToReturn = new PipedInputStream();
			PipedOutputStream dataFromController = new PipedOutputStream(dataToReturn);

			writeRecordsToQueue(dataToReturn);

			mController.pipeToOutputStream(dataFromController);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Pipes records to the queue hold by this object.
	 * 
	 * @param dataToReturn
	 *            data to pipe
	 */
	void writeRecordsToQueue(final PipedInputStream dataToReturn) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				BufferedReader bReader = new BufferedReader(new InputStreamReader(dataToReturn));

				try {
					String line = bReader.readLine();
					while (line != null) {

						AbstractRecord record = AbstractRecord.fromString(line);
						if (record != null) {
							record.relativiseTimestamps(mController.getControllerRelativeTime());
							if (record.getTimeStamp() >= 0) {
								records.offer(record);
							}

						}
						line = bReader.readLine();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					if (bReader != null) {
						try {
							bReader.close();
							dataToReturn.close();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}

			}
		}).start();
	}
}
