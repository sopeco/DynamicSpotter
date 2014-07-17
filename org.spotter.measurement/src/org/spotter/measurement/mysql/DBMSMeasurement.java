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
package org.spotter.measurement.mysql;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.aim.api.exceptions.MeasurementException;
import org.aim.api.measurement.MeasurementData;
import org.aim.api.measurement.collector.AbstractDataSource;
import org.aim.api.measurement.collector.CollectorFactory;
import org.aim.artifacts.measurement.collector.FileDataSource;
import org.aim.artifacts.records.DBStatisticsRecrod;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.extension.IExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.core.measurement.AbstractMeasurementController;

/**
 * Measurement adapter for sampling status of a Database Server. TODO: this
 * class is specific
 * 
 * @author Alexander Wert
 * 
 */
public class DBMSMeasurement extends AbstractMeasurementController implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(DBMSMeasurement.class);
	public static final String SAMPLING_DELAY = "org.spotter.sampling.delay";
	public static final String CONNECTION_STRING = "org.spotter.sampling.mysql.connectionString";
	public static final String COLLECTOR_TYPE_KEY = "org.spotter.sampling.mysql.collectorType";
	public static Integer instanceId = 1;
	private AbstractDataSource dataSource;
	private Thread thread;

	private Connection jdbcConnection;
	private PreparedStatement sqlStatement;
	private boolean running;

	private long delay;
	private String dbConnectionString;
	protected static final long DEFAULT_DELAY = 500;

	/**
	 * Query string for Database MS status. TODO: it is specific yet,
	 * externalize
	 */
	private static final String SQL_QUERY = "SHOW STATUS WHERE Variable_name like "
			+ "'Innodb_row_lock_waits' OR Variable_name like 'Queries' OR "
			+ "Variable_name like 'Innodb_row_lock_time';";

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *            extension provider
	 */
	public DBMSMeasurement(IExtension<?> provider) {
		super(provider);
	}

	@Override
	public void enableMonitoring() throws MeasurementException {

		try {
			jdbcConnection = DriverManager.getConnection(dbConnectionString);
			sqlStatement = jdbcConnection.prepareStatement(SQL_QUERY);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void disableMonitoring() throws MeasurementException {
		try {
			running = false;
			thread.join(0);
			if (sqlStatement != null) {
				sqlStatement.close();
			}
			if (jdbcConnection != null) {
				jdbcConnection.close();
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MeasurementData getMeasurementData() throws MeasurementException {
		return dataSource.read();
	}

	@Override
	public void pipeToOutputStream(OutputStream oStream) throws MeasurementException {
		try {
			dataSource.pipeToOutputStream(oStream);
		} catch (MeasurementException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void initialize() throws MeasurementException {
		if (getProperties().containsKey(SAMPLING_DELAY)) {
			delay = Long.valueOf(getProperties().getProperty(SAMPLING_DELAY));
		} else {
			delay = DEFAULT_DELAY;
		}

		if (getProperties().containsKey(CONNECTION_STRING)) {
			dbConnectionString = getProperties().getProperty(CONNECTION_STRING);
		} else {
			throw new RuntimeException("Connection String to database has not been specified!");
		}

		if (getProperties().containsKey(SAMPLING_DELAY)) {
			delay = Long.valueOf(getProperties().getProperty(SAMPLING_DELAY));
		} else {
			delay = DEFAULT_DELAY;
		}
		Properties collectorProperties = GlobalConfiguration.getInstance().getProperties();
		synchronized (instanceId) {
			collectorProperties.setProperty(FileDataSource.ADDITIONAL_FILE_PREFIX_KEY, "MySQLSampler-" + instanceId);
			instanceId++;
		}

		dataSource = CollectorFactory.createDataSource(getProperties().getProperty(COLLECTOR_TYPE_KEY),
				collectorProperties);
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public long getCurrentTime() {
		return System.currentTimeMillis();
	}

	@Override
	public void run() {
		running = true;
		try {
			dataSource.enable();
			long counter = 0;
			while (running) {
				sampleMySQLStatistics(counter);
				counter++;
				try {
					Thread.sleep(delay);
				} catch (InterruptedException ie) {
					LOGGER.debug("Sleeptime interrupted.");
					running = false;
				}
			}

			dataSource.disable();
		} catch (MeasurementException e) {
			throw new RuntimeException(e);
		}
	}

	private void sampleMySQLStatistics(long ownNumQueries) {

		try {
			ResultSet resultSet = sqlStatement.executeQuery();
			String name = "";
			long numQueueries = 0;
			long numLockWaits = 0;
			long lockTime = 0;
			while (resultSet.next()) {
				name = resultSet.getString("Variable_name");

				if ("Queries".equals(name)) {
					numQueueries = resultSet.getLong("Value") - ownNumQueries;
				}

				if ("Innodb_row_lock_waits".equals(name)) {
					numLockWaits = resultSet.getLong("Value");
				}

				if ("Innodb_row_lock_time".equals(name)) {
					lockTime = resultSet.getLong("Value");
				}

				continue;
			}
			resultSet.close();
			DBStatisticsRecrod record = new DBStatisticsRecrod();
			record.setTimeStamp(System.currentTimeMillis());
			record.setNumQueueries(numQueueries);
			record.setProcessId(dbConnectionString);
			record.setNumLockWaits(numLockWaits);
			record.setLockTime(lockTime);
			dataSource.newRecord(record);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void storeReport(String path) throws MeasurementException {
		// nothing to do here
	}
}
