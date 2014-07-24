package org.spotter.core.satellites;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.instrumentation.description.InstrumentationDescriptionBuilder;
import org.aim.api.measurement.MeasurementData;
import org.aim.artifacts.measurement.collector.StreamReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lpe.common.util.system.LpeSystemUtils;
import org.spotter.core.AbstractSpotterSatelliteExtension;
import org.spotter.core.instrumentation.AbstractSpotterInstrumentation;
import org.spotter.core.instrumentation.ISpotterInstrumentation;
import org.spotter.core.instrumentation.InstrumentationBroker;
import org.spotter.core.measurement.AbstractMeasurementController;
import org.spotter.core.measurement.IMeasurementController;
import org.spotter.core.measurement.MeasurementBroker;
import org.spotter.core.test.dummies.satellites.DummyInstrumentation;
import org.spotter.core.test.dummies.satellites.DummyMeasurement;
import org.spotter.core.test.dummies.satellites.DummyWorkload;
import org.spotter.core.workload.AbstractWorkloadAdapter;
import org.spotter.core.workload.IWorkloadAdapter;
import org.spotter.core.workload.WorkloadAdapterBroker;
import org.spotter.exceptions.WorkloadException;

public class SatellitesTest {
	public static Properties brokerProperties;
	public static Properties testProperties;
	public static Properties testProperties_2;
	public static Properties testProperties_3;

	@BeforeClass
	public static void initProperties() {
		testProperties = new Properties();
		testProperties.setProperty(AbstractSpotterSatelliteExtension.NAME_KEY, "testName");
		testProperties.setProperty(AbstractSpotterSatelliteExtension.HOST_KEY, "testHost");
		testProperties.setProperty(AbstractSpotterSatelliteExtension.PORT_KEY, "testPort");
		testProperties.setProperty("anotherProperty", "testProperty");
		testProperties.setProperty(ISpotterInstrumentation.INSTRUMENTATION_INCLUDES, "org.test");
		testProperties.setProperty(ISpotterInstrumentation.INSTRUMENTATION_EXCLUDES, "org.another.test");

		testProperties_2 = new Properties();
		testProperties_2.setProperty(AbstractSpotterSatelliteExtension.NAME_KEY, "testName_2");
		testProperties_2.setProperty(AbstractSpotterSatelliteExtension.HOST_KEY, "testHost_2");
		testProperties_2.setProperty(AbstractSpotterSatelliteExtension.PORT_KEY, "testPort_2");
		testProperties_2.setProperty("anotherProperty", "testProperty_2");

		testProperties_3 = new Properties();
		testProperties_3.setProperty(AbstractSpotterSatelliteExtension.NAME_KEY, "testName_3");
		testProperties_3.setProperty(AbstractSpotterSatelliteExtension.HOST_KEY, "testHost_3");
		testProperties_3.setProperty(AbstractSpotterSatelliteExtension.PORT_KEY, "testPort_3");
		testProperties_3.setProperty("anotherProperty", "testProperty_3");
		testProperties_3.setProperty("uniqueProperty", "testUniqueProperty");

		brokerProperties = new Properties();
		brokerProperties.setProperty(AbstractSpotterSatelliteExtension.NAME_KEY, "testName_broker");
		brokerProperties.setProperty(AbstractSpotterSatelliteExtension.HOST_KEY, "testHost_broker");
		brokerProperties.setProperty(AbstractSpotterSatelliteExtension.PORT_KEY, "testPort_broker");
		brokerProperties.setProperty("anotherProperty", "testPropertyBroker");
	}

	@Test
	public void testAbstractSpotterInstrumentation() {
		AbstractSpotterInstrumentation asInst = new DummyInstrumentation(null);

		asInst.setProperties(testProperties);
		Assert.assertEquals("testName", asInst.getName());
		Assert.assertEquals("testHost", asInst.getHost());
		Assert.assertEquals("testPort", asInst.getPort());
		Assert.assertEquals("testProperty", asInst.getProperties().get("anotherProperty"));
		Assert.assertNull(asInst.getProvider());

	}

	@Test
	public void testAbstractMeasurementController() {
		AbstractMeasurementController measController = new DummyMeasurement(null);

		measController.setProperties(testProperties);
		long time = System.currentTimeMillis();
		measController.setControllerRelativeTime(time);
		Assert.assertEquals("testName", measController.getName());
		Assert.assertEquals("testHost", measController.getHost());
		Assert.assertEquals("testPort", measController.getPort());
		Assert.assertEquals("testProperty", measController.getProperties().get("anotherProperty"));
		Assert.assertEquals(time, measController.getControllerRelativeTime());
		Assert.assertNull(measController.getProvider());

	}

	@Test
	public void testAbstractWorkloadAdapter() {
		AbstractWorkloadAdapter awAdapter = new DummyWorkload(null);

		awAdapter.setProperties(testProperties);
		Assert.assertEquals("testName", awAdapter.getName());
		Assert.assertEquals("testHost", awAdapter.getHost());
		Assert.assertEquals("testPort", awAdapter.getPort());
		Assert.assertEquals("testProperty", awAdapter.getProperties().get("anotherProperty"));
		Assert.assertNull(awAdapter.getProvider());

	}

	@Test
	public void testInstrumentationBroker() throws InstrumentationException {
		DummyInstrumentation inst_1 = new DummyInstrumentation(null);

		inst_1.setProperties(testProperties);

		DummyInstrumentation inst_2 = new DummyInstrumentation(null);

		inst_2.setProperties(testProperties_2);

		DummyInstrumentation inst_3 = new DummyInstrumentation(null);

		inst_3.setProperties(testProperties_3);

		List<ISpotterInstrumentation> instControllers = new ArrayList<>();
		instControllers.add(inst_1);
		instControllers.add(inst_2);
		instControllers.add(inst_3);

		InstrumentationBroker instBroker = InstrumentationBroker.getInstance();
		instBroker = InstrumentationBroker.getInstance();
		Assert.assertNull(instBroker.getProvider());
		instBroker.setControllers(instControllers);
		Assert.assertEquals("Instrumentation Broker", instBroker.getName());
		Assert.assertEquals("localhost", instBroker.getHost());
		Assert.assertEquals("NA", instBroker.getPort());
		Assert.assertTrue(instControllers.containsAll(instBroker
				.getInstrumentationControllers(ISpotterInstrumentation.class)));
		Assert.assertTrue(instBroker.getInstrumentationControllers(ISpotterInstrumentation.class).containsAll(
				instControllers));
		Assert.assertTrue(instControllers.containsAll(instBroker
				.getInstrumentationControllers(DummyInstrumentation.class)));
		Assert.assertTrue(instBroker.getInstrumentationControllers(DummyInstrumentation.class).containsAll(
				instControllers));
		instBroker.setProperties(brokerProperties);
		Assert.assertEquals("testUniqueProperty", instBroker.getProperties().get("uniqueProperty"));

		// Test initialization
		Assert.assertFalse(inst_1.initialized);
		Assert.assertFalse(inst_2.initialized);
		Assert.assertFalse(inst_3.initialized);
		instBroker.initialize();
		Assert.assertTrue(inst_1.initialized);
		Assert.assertTrue(inst_2.initialized);
		Assert.assertTrue(inst_3.initialized);

		// Test instrumentation
		InstrumentationDescriptionBuilder idBuilder = new InstrumentationDescriptionBuilder();

		Assert.assertFalse(inst_1.instrumented);
		Assert.assertFalse(inst_2.instrumented);
		Assert.assertFalse(inst_3.instrumented);
		instBroker.instrument(idBuilder.build());
		Assert.assertTrue(inst_1.instrumented);
		Assert.assertTrue(inst_2.instrumented);
		Assert.assertTrue(inst_3.instrumented);

		// Test uninstrumentation
		Assert.assertTrue(inst_1.instrumented);
		Assert.assertTrue(inst_2.instrumented);
		Assert.assertTrue(inst_3.instrumented);
		instBroker.uninstrument();
		Assert.assertFalse(inst_1.instrumented);
		Assert.assertFalse(inst_2.instrumented);
		Assert.assertFalse(inst_3.instrumented);

	}

	@Test(expected = InstrumentationException.class)
	public void testInvalidInstrumentationDescription() throws InstrumentationException {
		InstrumentationBroker instBroker = InstrumentationBroker.getInstance();
		instBroker.instrument(null);
	}

	@Test
	public void testMeasurementBroker() throws MeasurementException, IOException {
		DummyMeasurement meas_1 = new DummyMeasurement(null);

		meas_1.setProperties(testProperties);

		DummyMeasurement meas_2 = new DummyMeasurement(null);

		meas_2.setProperties(testProperties_2);

		DummyMeasurement meas_3 = new DummyMeasurement(null);

		meas_3.setProperties(testProperties_3);

		List<IMeasurementController> measControllers = new ArrayList<>();
		measControllers.add(meas_1);
		measControllers.add(meas_2);
		measControllers.add(meas_3);

		final MeasurementBroker measBroker = MeasurementBroker.getInstance();
		MeasurementBroker.getInstance();
		Assert.assertNull(measBroker.getProvider());
		measBroker.setControllers(measControllers);
		Assert.assertEquals("Measurement Broker", measBroker.getName());
		Assert.assertEquals("localhost", measBroker.getHost());
		Assert.assertEquals("NA", measBroker.getPort());

		measBroker.setProperties(brokerProperties);
		Assert.assertEquals("testUniqueProperty", measBroker.getProperties().get("uniqueProperty"));

		// Test initialization
		Assert.assertFalse(meas_1.initialized);
		Assert.assertFalse(meas_2.initialized);
		Assert.assertFalse(meas_3.initialized);
		measBroker.initialize();
		Assert.assertTrue(meas_1.initialized);
		Assert.assertTrue(meas_2.initialized);
		Assert.assertTrue(meas_3.initialized);

		// Test enable
		Assert.assertFalse(meas_1.enabled);
		Assert.assertFalse(meas_2.enabled);
		Assert.assertFalse(meas_3.enabled);
		measBroker.enableMonitoring();
		Assert.assertTrue(meas_1.enabled);
		Assert.assertTrue(meas_2.enabled);
		Assert.assertTrue(meas_3.enabled);

		// Test disable
		Assert.assertTrue(meas_1.enabled);
		Assert.assertTrue(meas_2.enabled);
		Assert.assertTrue(meas_3.enabled);
		measBroker.disableMonitoring();
		Assert.assertFalse(meas_1.enabled);
		Assert.assertFalse(meas_2.enabled);
		Assert.assertFalse(meas_3.enabled);

		// Test store report
		Assert.assertFalse(meas_1.reportStored);
		Assert.assertFalse(meas_2.reportStored);
		Assert.assertFalse(meas_3.reportStored);
		measBroker.storeReport("");
		Assert.assertTrue(meas_1.reportStored);
		Assert.assertTrue(meas_2.reportStored);
		Assert.assertTrue(meas_3.reportStored);

		long time = System.currentTimeMillis();
		measBroker.setControllerRelativeTime(time);
		Assert.assertEquals(time, measBroker.getControllerRelativeTime());
		time = System.currentTimeMillis();
		long cTime = measBroker.getCurrentTime();
		Assert.assertTrue(time <= cTime && cTime <= System.currentTimeMillis());

		// Test getData
		MeasurementData data = measBroker.getMeasurementData();
		Assert.assertEquals(meas_1.getMeasurementData().getRecords().size() * 3, data.getRecords().size());

		PipedInputStream pis = new PipedInputStream();
		final PipedOutputStream pos = new PipedOutputStream(pis);
		LpeSystemUtils.submitTask(new Runnable() {

			@Override
			public void run() {
				try {
					measBroker.pipeToOutputStream(pos);
				} catch (MeasurementException e) {
					e.printStackTrace();
				}

			}
		});

		StreamReader sReader = new StreamReader();
		sReader.setSource(pis);
		data = sReader.read();

	}

	@Test
	public void testWorkloadAdapterBroker() throws WorkloadException {
		DummyWorkload wl_1 = new DummyWorkload(null);

		wl_1.setProperties(testProperties);

		DummyWorkload wl_2 = new DummyWorkload(null);

		wl_2.setProperties(testProperties_2);

		DummyWorkload wl_3 = new DummyWorkload(null);

		wl_3.setProperties(testProperties_3);

		List<IWorkloadAdapter> wlControllers = new ArrayList<>();
		wlControllers.add(wl_1);
		wlControllers.add(wl_2);
		wlControllers.add(wl_3);

		WorkloadAdapterBroker wlBroker = WorkloadAdapterBroker.getInstance();
		wlBroker = WorkloadAdapterBroker.getInstance();
		Assert.assertNull(wlBroker.getProvider());
		wlBroker.setControllers(wlControllers);
		Assert.assertEquals("Workload Adapter Broker", wlBroker.getName());
		Assert.assertEquals("localhost", wlBroker.getHost());
		Assert.assertEquals("NA", wlBroker.getPort());

		wlBroker.setProperties(brokerProperties);
		Assert.assertEquals("testUniqueProperty", wlBroker.getProperties().get("uniqueProperty"));

		// Test initialization
		Assert.assertFalse(wl_1.initialized);
		Assert.assertFalse(wl_2.initialized);
		Assert.assertFalse(wl_3.initialized);
		wlBroker.initialize();
		Assert.assertTrue(wl_1.initialized);
		Assert.assertTrue(wl_2.initialized);
		Assert.assertTrue(wl_3.initialized);

		// Test startLoad
		Assert.assertFalse(wl_1.loadStarted);
		Assert.assertFalse(wl_2.loadStarted);
		Assert.assertFalse(wl_3.loadStarted);
		wlBroker.startLoad(null);
		Assert.assertTrue(wl_1.loadStarted);
		Assert.assertTrue(wl_2.loadStarted);
		Assert.assertTrue(wl_3.loadStarted);

		// Test startLoad
		Assert.assertFalse(wl_1.warmUpTerminated);
		Assert.assertFalse(wl_2.warmUpTerminated);
		Assert.assertFalse(wl_3.warmUpTerminated);
		wlBroker.waitForWarmupPhaseTermination();
		Assert.assertTrue(wl_1.warmUpTerminated);
		Assert.assertTrue(wl_2.warmUpTerminated);
		Assert.assertTrue(wl_3.warmUpTerminated);

		// Test startLoad
		Assert.assertFalse(wl_1.experimentTerminated);
		Assert.assertFalse(wl_2.experimentTerminated);
		Assert.assertFalse(wl_3.experimentTerminated);
		wlBroker.waitForExperimentPhaseTermination();
		Assert.assertTrue(wl_1.experimentTerminated);
		Assert.assertTrue(wl_2.experimentTerminated);
		Assert.assertTrue(wl_3.experimentTerminated);

		// Test waitForLoadFinished
		Assert.assertTrue(wl_1.loadStarted);
		Assert.assertTrue(wl_2.loadStarted);
		Assert.assertTrue(wl_3.loadStarted);
		wlBroker.waitForFinishedLoad();
		Assert.assertFalse(wl_1.loadStarted);
		Assert.assertFalse(wl_2.loadStarted);
		Assert.assertFalse(wl_3.loadStarted);

	}
}
