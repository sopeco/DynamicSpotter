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
package org.spotter.measurement.jmsserver;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.aim.api.exceptions.MeasurementException;
import org.aim.api.measurement.MeasurementData;
import org.aim.api.measurement.collector.AbstractDataSource;
import org.aim.api.measurement.collector.CollectorFactory;
import org.aim.artifacts.measurement.collector.FileDataSource;
import org.aim.artifacts.records.JmsServerRecord;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.lpe.common.config.GlobalConfiguration;
import org.lpe.common.extension.IExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.core.measurement.AbstractMeasurementController;

/**
 * Measurement adapter for sampling status of a JMS Server.
 * 
 * @author Alexander Wert
 * 
 */
public class JmsServerMeasurement extends AbstractMeasurementController implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(JmsServerMeasurement.class);
	public static final String SAMPLING_DELAY = "org.spotter.sampling.delay";
	public static final String DESTINATION_NAME = "org.spotter.measurement.jmsserver.DestinationName";
	public static final String ACTIVE_MQJMX_URL = "org.spotter.measurement.jmsserver.ActiveMQJMXUrl";
	public static final String COLLECTOR_TYPE_KEY = "org.spotter.sampling.jmsserver.collectorType";

	private AbstractDataSource dataSource;
	private Thread thread;

	private List<QueueViewMBean> queueMbeans;

	private boolean running;

	private long delay;
	protected static final long DEFAULT_DELAY = 500;

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *            extension provider
	 */
	public JmsServerMeasurement(IExtension<?> provider) {
		super(provider);
	}

	@Override
	public void enableMonitoring() throws MeasurementException {
		resetActiveMQStatistics();
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void disableMonitoring() throws MeasurementException {
		try {
			running = false;
			thread.join(0);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void resetActiveMQStatistics() {
		try {
			LOGGER.debug("purge and reset ActiveMQ server");
			for (QueueViewMBean queueMbean : queueMbeans) {
				// queueMbean.purge();
				queueMbean.resetStatistics();
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
		Properties collectorProperties = GlobalConfiguration.getInstance().getProperties();
		collectorProperties.setProperty(FileDataSource.ADDITIONAL_FILE_PREFIX_KEY, "JMSServerSampler");

		dataSource = CollectorFactory.createDataSource(getProperties().getProperty(COLLECTOR_TYPE_KEY),
				collectorProperties);

		try {
			LOGGER.debug("Connect to JMX ActiveMQ server");
			String activeMqJMX = getProperties().getProperty(ACTIVE_MQJMX_URL);

			JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(activeMqJMX));
			connector.connect();

			MBeanServerConnection connection = connector.getMBeanServerConnection();

			ObjectName mbeanName = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost");
			BrokerViewMBean mbean = MBeanServerInvocationHandler.newProxyInstance(connection, mbeanName,
					BrokerViewMBean.class, true);
			queueMbeans = new ArrayList<>();
			for (ObjectName queueName : mbean.getQueues()) {
				QueueViewMBean tempQueueMbean = (QueueViewMBean) MBeanServerInvocationHandler.newProxyInstance(
						connection, queueName, QueueViewMBean.class, true);
				queueMbeans.add(tempQueueMbean);
			}

		} catch (Exception e) {
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

			while (running) {
				sampleJmsServerStatistics();

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

	/**
	 * Samples JMS Server status
	 */
	private void sampleJmsServerStatistics() {
		for (QueueViewMBean tempBean : queueMbeans) {
			JmsServerRecord record = new JmsServerRecord();
			record.setQueueName(tempBean.getName());
			record.setTimeStamp(System.currentTimeMillis());
			record.setAverageEnqueueTime(tempBean.getAverageEnqueueTime());
			record.setDequeueCount(tempBean.getDequeueCount());
			record.setDispatchCount(tempBean.getDispatchCount());
			record.setEnqueueCount(tempBean.getEnqueueCount());
			record.setMemoryPercentUsage(tempBean.getMemoryPercentUsage());
			record.setMemoryUsage(tempBean.getMemoryUsageByteCount());
			record.setQueueSize(tempBean.getQueueSize());
			dataSource.newRecord(record);
		}
	}

	@Override
	public void storeReport(String path) throws MeasurementException {
		// nothing to do here.
	}
}
