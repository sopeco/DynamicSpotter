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
package org.spotter.core.config.interpretation;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.config.GlobalConfiguration;
import org.spotter.core.instrumentation.IInstrumentationAdapter;
import org.spotter.core.measurement.IMeasurementAdapter;
import org.spotter.core.workload.IWorkloadAdapter;
import org.spotter.shared.configuration.ConfigKeys;

import junit.framework.Assert;

public class MEFactoryTest {
	@BeforeClass
	public static void initializeGlobalConfig() {
		GlobalConfiguration.initialize(new Properties());
		final String dir = System.getProperty("user.dir");
		final Properties properties = new Properties();
		properties.setProperty("org.lpe.common.extension.appRootDir", dir);
		properties.setProperty("org.spotter.conf.pluginDirNames", "plugins");
		GlobalConfiguration.reinitialize(properties);
	}

	@Test
	public void testMeasurementEnvironmentCreation() throws URISyntaxException {
		final URL url = HierarchyTest.class.getResource("/test-env.xml");
		final String envFile = url.toURI().getPath();

		// *****************************
		// INSTRUMENTATION CONTROLLERS
		// *****************************
		List<IInstrumentationAdapter> instrumentations = MeasurementEnvironmentFactory.getInstance()
				.createInstrumentationControllers(envFile);
		Assert.assertEquals(1, instrumentations.size());
		Assert.assertEquals("org.spotter.core.test.dummies.satellites.DummyInstrumentation", instrumentations.get(0).getProvider().getName());
		Assert.assertEquals("0", instrumentations.get(0).getPort());
		Assert.assertEquals("X", instrumentations.get(0).getHost());
		Assert.assertEquals("Y", instrumentations.get(0).getName());

		Assert.assertEquals(4, instrumentations.get(0).getProvider().getConfigParameters().size());
		ConfigParameterDescription cpDesription = null;
		for (final ConfigParameterDescription cpd : instrumentations.get(0).getProvider().getConfigParameters()) {
			cpDesription = cpd;
			Assert.assertTrue(cpDesription.getName().equals("test.instrumentation.parameter")
					|| cpDesription.getName().equals(ConfigKeys.SATELLITE_ADAPTER_NAME_KEY)
					|| cpDesription.getName().equals(ConfigParameterDescription.EXT_DESCRIPTION_KEY)
					|| cpDesription.getName().equals(ConfigParameterDescription.EXT_LABEL_KEY));
		}

		// *****************************
		// MEASUREMENT CONTROLLERS
		// *****************************
		List<IMeasurementAdapter> measurementControllers = MeasurementEnvironmentFactory.getInstance()
				.createMeasurementControllers(envFile);
		Assert.assertEquals(1, measurementControllers.size());
		Assert.assertEquals("org.spotter.core.test.dummies.satellites.DummyMeasurement", measurementControllers.get(0).getProvider().getName());
		Assert.assertEquals("measurement.value",
				measurementControllers.get(0).getProperties().get("org.test.measurement.key"));

		Assert.assertEquals(6, measurementControllers.get(0).getProvider().getConfigParameters().size());
		cpDesription = null;
		for (final ConfigParameterDescription cpd : measurementControllers.get(0).getProvider().getConfigParameters()) {
			cpDesription = cpd;

			Assert.assertTrue(cpDesription.getName().equals("test.measurement.parameter")
					|| cpDesription.getName().equals(ConfigKeys.SATELLITE_ADAPTER_NAME_KEY)
					|| cpDesription.getName().equals(ConfigKeys.SATELLITE_HOST_KEY)
					|| cpDesription.getName().equals(ConfigParameterDescription.EXT_DESCRIPTION_KEY)
					|| cpDesription.getName().equals(ConfigParameterDescription.EXT_LABEL_KEY)
					|| cpDesription.getName().equals(ConfigKeys.SATELLITE_PORT_KEY));
		}

		// *****************************
		// WORKLOAD CONTROLLERS
		// *****************************
		List<IWorkloadAdapter> wlAdapters = MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(envFile);
		Assert.assertEquals(1, wlAdapters.size());
		Assert.assertEquals("org.spotter.core.test.dummies.satellites.DummyWorkload", wlAdapters.get(0).getProvider().getName());
		Assert.assertEquals("workload.value", wlAdapters.get(0).getProperties().get("org.test.workload.key"));

		Assert.assertEquals(4, wlAdapters.get(0).getProvider().getConfigParameters().size());
		cpDesription = null;
		for (final ConfigParameterDescription cpd : wlAdapters.get(0).getProvider().getConfigParameters()) {
			cpDesription = cpd;
			Assert.assertTrue(cpDesription.getName().equals("test.workload.parameter")
					|| cpDesription.getName().equals("org.spotter.satellite.adapter.name")
					|| cpDesription.getName().equals(ConfigParameterDescription.EXT_DESCRIPTION_KEY)
					|| cpDesription.getName().equals(ConfigParameterDescription.EXT_LABEL_KEY));
		}

		
		// *****************************
		// EMPTY ENVIRONMENT
		// *****************************
		final URL emptyEnv_url = HierarchyTest.class.getResource("/empty-env.xml");
		final String emptyEnvFile = emptyEnv_url.toURI().getPath();

		instrumentations = MeasurementEnvironmentFactory.getInstance().createInstrumentationControllers(emptyEnvFile);
		Assert.assertTrue(instrumentations.isEmpty());

		measurementControllers = MeasurementEnvironmentFactory.getInstance().createMeasurementControllers(emptyEnvFile);
		Assert.assertTrue(measurementControllers.isEmpty());

		wlAdapters = MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(emptyEnvFile);
		Assert.assertTrue(wlAdapters.isEmpty());

	}

	@Test(expected = RuntimeException.class)
	public void testInvalidInstrumentationSatellite() throws URISyntaxException {
		final URL url = HierarchyTest.class.getResource("/invalid-env.xml");
		final String envFile = url.toURI().getPath();

		MeasurementEnvironmentFactory.getInstance().createInstrumentationControllers(envFile);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidMeasurementSatellite() throws URISyntaxException {
		final URL url = HierarchyTest.class.getResource("/invalid-env.xml");
		final String envFile = url.toURI().getPath();

		MeasurementEnvironmentFactory.getInstance().createMeasurementControllers(envFile);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidWorkloadSatellite() throws URISyntaxException {
		final URL url = HierarchyTest.class.getResource("/invalid-env.xml");
		final String envFile = url.toURI().getPath();

		MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(envFile);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidFile() throws URISyntaxException {
		MeasurementEnvironmentFactory.getInstance().parseXMLFile("/invalidFile.xml");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidHostMeasurement() throws URISyntaxException {
		final URL url = HierarchyTest.class.getResource("/test-env.xml");
		final String envFile = url.toURI().getPath();
		final List<IMeasurementAdapter> measurementControllers = MeasurementEnvironmentFactory.getInstance()
				.createMeasurementControllers(envFile);
		Assert.assertEquals(1, measurementControllers.size());
		Assert.assertNull(measurementControllers.get(0).getHost());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidPortMeasurement() throws URISyntaxException {
		final URL url = HierarchyTest.class.getResource("/test-env.xml");
		final String envFile = url.toURI().getPath();
		final List<IMeasurementAdapter> measurementControllers = MeasurementEnvironmentFactory.getInstance()
				.createMeasurementControllers(envFile);
		Assert.assertEquals(1, measurementControllers.size());
		Assert.assertNull(measurementControllers.get(0).getPort());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidHostWorkload() throws URISyntaxException {
		final URL url = HierarchyTest.class.getResource("/test-env.xml");
		final String envFile = url.toURI().getPath();
		final List<IWorkloadAdapter> wlAdapters = MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(envFile);
		Assert.assertEquals(1, wlAdapters.size());
		Assert.assertNull(wlAdapters.get(0).getHost());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidPortWorkload() throws URISyntaxException {
		final URL url = HierarchyTest.class.getResource("/test-env.xml");
		final String envFile = url.toURI().getPath();
		final List<IWorkloadAdapter> wlAdapters = MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(envFile);
		Assert.assertEquals(1, wlAdapters.size());
		Assert.assertNull(wlAdapters.get(0).getPort());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidNameMeasurement() throws URISyntaxException {
		final URL url = HierarchyTest.class.getResource("/test-env.xml");
		final String envFile = url.toURI().getPath();
		final List<IMeasurementAdapter> measurementControllers = MeasurementEnvironmentFactory.getInstance()
				.createMeasurementControllers(envFile);
		Assert.assertEquals(1, measurementControllers.size());
		Assert.assertNull(measurementControllers.get(0).getName());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidNameWorkload() throws URISyntaxException {
	
		final URL url = HierarchyTest.class.getResource("/test-env.xml");
		final String envFile = url.toURI().getPath();
		final List<IWorkloadAdapter> wlAdapters = MeasurementEnvironmentFactory.getInstance().createWorkloadAdapters(envFile);
		Assert.assertEquals(1, wlAdapters.size());
		Assert.assertNull(wlAdapters.get(0).getName());
	}
}
