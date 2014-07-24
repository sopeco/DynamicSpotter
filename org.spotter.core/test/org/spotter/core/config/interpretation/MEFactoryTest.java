package org.spotter.core.config.interpretation;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.config.GlobalConfiguration;
import org.spotter.core.instrumentation.ISpotterInstrumentation;
import org.spotter.core.measurement.IMeasurementController;
import org.spotter.core.workload.IWorkloadAdapter;
import org.spotter.shared.configuration.ConfigKeys;

public class MEFactoryTest {
	@BeforeClass
	public static void initializeGlobalConfig() {
		GlobalConfiguration.initialize(new Properties());
		String dir = System.getProperty("user.dir");
		Properties properties = new Properties();
		properties.setProperty("org.lpe.common.extension.appRootDir", dir);
		properties.setProperty("org.spotter.conf.pluginDirNames", "plugins");
		GlobalConfiguration.reinitialize(properties);
	}

	@Test
	public void testMeasurementEnvironmentCreation() throws URISyntaxException {
		URL url = HierarchyTest.class.getResource("/test-env.xml");
		String envFile = url.toURI().getPath();

		// *****************************
		// INSTRUMENTATION CONTROLLERS
		// *****************************
		List<ISpotterInstrumentation> instrumentations = MeasurementEnvironmentFactory.getInstance()
				.createInstrumentationControllers(envFile);
		Assert.assertEquals(1, instrumentations.size());
		Assert.assertEquals("DummyInstrumentation", instrumentations.get(0).getProvider().getName());
		Assert.assertEquals("0", instrumentations.get(0).getPort());
		Assert.assertEquals("X", instrumentations.get(0).getHost());
		Assert.assertEquals("Y", instrumentations.get(0).getName());

		Assert.assertEquals(2, instrumentations.get(0).getProvider().getConfigParameters().size());
		ConfigParameterDescription cpDesription = null;
		for (ConfigParameterDescription cpd : instrumentations.get(0).getProvider().getConfigParameters()) {
			cpDesription = cpd;
			Assert.assertTrue(cpDesription.getName().equals("test.instrumentation.parameter")
					|| cpDesription.getName().equals(ConfigKeys.SATELLITE_ADAPTER_NAME_KEY));
		}

		// *****************************
		// MEASUREMENT CONTROLLERS
		// *****************************
		List<IMeasurementController> measurementControllers = MeasurementEnvironmentFactory.getInstance()
				.createMeasurementControllers(envFile);
		Assert.assertEquals(1, measurementControllers.size());
		Assert.assertEquals("DummyMeasurement", measurementControllers.get(0).getProvider().getName());
		Assert.assertEquals("measurement.value",
				measurementControllers.get(0).getProperties().get("org.test.measurement.key"));

		Assert.assertEquals(4, measurementControllers.get(0).getProvider().getConfigParameters().size());
		cpDesription = null;
		for (ConfigParameterDescription cpd : measurementControllers.get(0).getProvider().getConfigParameters()) {
			cpDesription = cpd;

			Assert.assertTrue(cpDesription.getName().equals("test.measurement.parameter")
					|| cpDesription.getName().equals(ConfigKeys.SATELLITE_ADAPTER_NAME_KEY)
					|| cpDesription.getName().equals(ConfigKeys.SATELLITE_HOST_KEY)
					|| cpDesription.getName().equals(ConfigKeys.SATELLITE_PORT_KEY));
		}

		// *****************************
		// WORKLOAD CONTROLLERS
		// *****************************
		List<IWorkloadAdapter> wlAdapters = MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(envFile);
		Assert.assertEquals(1, wlAdapters.size());
		Assert.assertEquals("DummyWorkload", wlAdapters.get(0).getProvider().getName());
		Assert.assertEquals("workload.value", wlAdapters.get(0).getProperties().get("org.test.workload.key"));

		Assert.assertEquals(2, wlAdapters.get(0).getProvider().getConfigParameters().size());
		cpDesription = null;
		for (ConfigParameterDescription cpd : wlAdapters.get(0).getProvider().getConfigParameters()) {
			cpDesription = cpd;
			Assert.assertTrue(cpDesription.getName().equals("test.workload.parameter")
					|| cpDesription.getName().equals("org.spotter.satellite.adapter.name"));
		}

		
		// *****************************
		// EMPTY ENVIRONMENT
		// *****************************
		URL emptyEnv_url = HierarchyTest.class.getResource("/empty-env.xml");
		String emptyEnvFile = emptyEnv_url.toURI().getPath();

		instrumentations = MeasurementEnvironmentFactory.getInstance().createInstrumentationControllers(emptyEnvFile);
		Assert.assertTrue(instrumentations.isEmpty());

		measurementControllers = MeasurementEnvironmentFactory.getInstance().createMeasurementControllers(emptyEnvFile);
		Assert.assertTrue(measurementControllers.isEmpty());

		wlAdapters = MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(emptyEnvFile);
		Assert.assertTrue(wlAdapters.isEmpty());

	}

	@Test(expected = RuntimeException.class)
	public void testInvalidInstrumentationSatellite() throws URISyntaxException {
		URL url = HierarchyTest.class.getResource("/invalid-env.xml");
		String envFile = url.toURI().getPath();

		MeasurementEnvironmentFactory.getInstance().createInstrumentationControllers(envFile);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidMeasurementSatellite() throws URISyntaxException {
		URL url = HierarchyTest.class.getResource("/invalid-env.xml");
		String envFile = url.toURI().getPath();

		MeasurementEnvironmentFactory.getInstance().createMeasurementControllers(envFile);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidWorkloadSatellite() throws URISyntaxException {
		URL url = HierarchyTest.class.getResource("/invalid-env.xml");
		String envFile = url.toURI().getPath();

		MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(envFile);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidFile() throws URISyntaxException {
		MeasurementEnvironmentFactory.getInstance().parseXMLFile("/invalidFile.xml");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidHostMeasurement() throws URISyntaxException {
		URL url = HierarchyTest.class.getResource("/test-env.xml");
		String envFile = url.toURI().getPath();
		List<IMeasurementController> measurementControllers = MeasurementEnvironmentFactory.getInstance()
				.createMeasurementControllers(envFile);
		Assert.assertEquals(1, measurementControllers.size());
		Assert.assertNull(measurementControllers.get(0).getHost());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidPortMeasurement() throws URISyntaxException {
		URL url = HierarchyTest.class.getResource("/test-env.xml");
		String envFile = url.toURI().getPath();
		List<IMeasurementController> measurementControllers = MeasurementEnvironmentFactory.getInstance()
				.createMeasurementControllers(envFile);
		Assert.assertEquals(1, measurementControllers.size());
		Assert.assertNull(measurementControllers.get(0).getPort());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidHostWorkload() throws URISyntaxException {
		URL url = HierarchyTest.class.getResource("/test-env.xml");
		String envFile = url.toURI().getPath();
		List<IWorkloadAdapter> wlAdapters = MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(envFile);
		Assert.assertEquals(1, wlAdapters.size());
		Assert.assertNull(wlAdapters.get(0).getHost());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidPortWorkload() throws URISyntaxException {
		URL url = HierarchyTest.class.getResource("/test-env.xml");
		String envFile = url.toURI().getPath();
		List<IWorkloadAdapter> wlAdapters = MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(envFile);
		Assert.assertEquals(1, wlAdapters.size());
		Assert.assertNull(wlAdapters.get(0).getPort());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidNameMeasurement() throws URISyntaxException {
		URL url = HierarchyTest.class.getResource("/test-env.xml");
		String envFile = url.toURI().getPath();
		List<IMeasurementController> measurementControllers = MeasurementEnvironmentFactory.getInstance()
				.createMeasurementControllers(envFile);
		Assert.assertEquals(1, measurementControllers.size());
		Assert.assertNull(measurementControllers.get(0).getName());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidNameWorkload() throws URISyntaxException {
	
		URL url = HierarchyTest.class.getResource("/test-env.xml");
		String envFile = url.toURI().getPath();
		List<IWorkloadAdapter> wlAdapters = MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(envFile);
		Assert.assertEquals(1, wlAdapters.size());
		Assert.assertNull(wlAdapters.get(0).getName());
	}
}
