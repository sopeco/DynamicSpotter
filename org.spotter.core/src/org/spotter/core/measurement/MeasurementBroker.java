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

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.aim.aiminterface.description.instrumentation.InstrumentationDescription;
import org.aim.aiminterface.entities.measurements.AbstractRecord;
import org.aim.aiminterface.entities.measurements.MeasurementData;
import org.aim.aiminterface.exceptions.MeasurementException;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.system.LpeSystemUtils;

/**
 * A wrapper (or delegator) class around, a set of measurement controller which
 * form the measurement environment.
 * 
 * @author Alexander Wert
 * 
 */
public final class MeasurementBroker implements IMeasurementAdapter {

	private static MeasurementBroker instance;

	/**
	 * 
	 * @return singleton instance
	 */
	public static synchronized MeasurementBroker getInstance() {
		if (instance == null) {
			instance = new MeasurementBroker();
		}
		return instance;
	}

	private final List<IMeasurementAdapter> controllers;

	private long controllerRelativeTime = 0;

	private boolean dataPipeliningFinished = false;

	/**
	 * Constructor.
	 * 
	 * @param instrumentationControllers
	 *            instrumentation controllers to manage
	 */
	private MeasurementBroker() {
		this.controllers = new ArrayList<IMeasurementAdapter>();

	}

	/**
	 * sets measurement controllers.
	 * 
	 * @param instrumentationControllers
	 *            collection of measurement controllers
	 */
	public void setControllers(final Collection<IMeasurementAdapter> instrumentationControllers) {
		this.controllers.clear();
		this.controllers.addAll(instrumentationControllers);
	}

	private static final long TWO = 2L;

	@Override
	public IExtension getProvider() {
		return null;
	}

	@Override
	public void enableMonitoring() throws MeasurementException {
		long controllerTime;
		long startRequestTime;
		long endRequestTime;
		final long triggerTime = System.currentTimeMillis();
		for (final IMeasurementAdapter controller : controllers) {
			startRequestTime = System.currentTimeMillis();
			controllerTime = controller.getCurrentTime();
			endRequestTime = System.currentTimeMillis();
			controller.setControllerRelativeTime(controllerTime - ((endRequestTime - startRequestTime) / TWO)
					- (startRequestTime - triggerTime));
		}

		for (final IMeasurementAdapter controller : controllers) {
			controller.enableMonitoring();
		}

	}

	@Override
	public void disableMonitoring() throws MeasurementException {
		for (final IMeasurementAdapter controller : controllers) {
			controller.disableMonitoring();
		}
	}

	@Override
	public MeasurementData getMeasurementData() throws MeasurementException {

		try {
			final List<Future<?>> tasks = new ArrayList<>();

			final LinkedBlockingQueue<AbstractRecord> records = new LinkedBlockingQueue<AbstractRecord>();

			for (final IMeasurementAdapter mController : controllers) {
				tasks.add(LpeSystemUtils.submitTask(new PipeDataTask(mController, records)));
			}
			final MeasurementData result = new MeasurementData();
			dataPipeliningFinished = false;

			final Future<?> terminationListeningTask = asyncListenForTermination(tasks);

			while (!(records.isEmpty() && dataPipeliningFinished)) {
				final AbstractRecord record = records.poll(1, TimeUnit.SECONDS);
				if (record != null) {
					result.getRecords().add(record);
				}
			}

			try {
				terminationListeningTask.get();
			} catch (final ExecutionException e) {
				throw new MeasurementException(e);
			}

			return result;

		} catch (final InterruptedException e) {
			throw new MeasurementException(e);
		}
	}

	private Future<?> asyncListenForTermination(final List<Future<?>> tasks) {
		final Future<?> terminationListeningTask = LpeSystemUtils.submitTask(new Runnable() {

			@Override
			public void run() {
				for (final Future<?> task : tasks) {
					try {
						task.get();
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e);
					}
				}
				dataPipeliningFinished = true;

			}
		});
		return terminationListeningTask;
	}

	@Override
	public void initialize() throws MeasurementException {
		for (final IMeasurementAdapter controller : controllers) {
			controller.initialize();
		}

	}

	@Override
	public long getCurrentTime() {
		return System.currentTimeMillis();
	}

	@Override
	public void pipeToOutputStream(final OutputStream oStream) throws MeasurementException {

		final List<Future<?>> tasks = new ArrayList<>();

		final LinkedBlockingQueue<AbstractRecord> records = new LinkedBlockingQueue<AbstractRecord>();

		for (final IMeasurementAdapter mController : controllers) {
			tasks.add(LpeSystemUtils.submitTask(new PipeDataTask(mController, records)));
		}
		dataPipeliningFinished = false;

		final Future<?> terminationListeningTask = asyncListenForTermination(tasks);

		try (BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(oStream))) {
			while (!(records.isEmpty() && dataPipeliningFinished)) {
				final AbstractRecord record = records.poll(1, TimeUnit.SECONDS);
				if (record != null) {

					bWriter.write(record.toString());

					bWriter.newLine();
				}
			}

			terminationListeningTask.get();

		} catch (final Exception e) {
			throw new MeasurementException(e);
		}

	}

	@Override
	public Properties getProperties() {
		final Properties props = new Properties();
		for (final IMeasurementAdapter controller : controllers) {
			props.putAll(controller.getProperties());
		}
		return props;
	}

	@Override
	public String getName() {
		return "Measurement Broker";
	}

	@Override
	public String getPort() {
		return "NA";
	}

	@Override
	public String getHost() {
		return "localhost";
	}

	@Override
	public void setProperties(final Properties properties) {
		// nothing to do
	}

	@Override
	public long getControllerRelativeTime() {
		return controllerRelativeTime;
	}

	@Override
	public void setControllerRelativeTime(final long relativeTime) {
		controllerRelativeTime = relativeTime;
	}

	@Override
	public void storeReport(final String path) throws MeasurementException {
		for (final IMeasurementAdapter controller : controllers) {
			controller.storeReport(path);
		}
	}

	@Override
	public void prepareMonitoring(final InstrumentationDescription monitoringDescription) throws MeasurementException {
		for (final IMeasurementAdapter controller : controllers) {
			controller.prepareMonitoring(monitoringDescription);
		}

	}

	@Override
	public void resetMonitoring() throws MeasurementException {
		for (final IMeasurementAdapter controller : controllers) {
			controller.resetMonitoring();
		}
	}

}
