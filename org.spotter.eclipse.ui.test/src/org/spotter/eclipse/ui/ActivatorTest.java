package org.spotter.eclipse.ui;

//import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

public class ActivatorTest {

	@Test
	public void testActivator() {
		Assert.assertEquals("org.spotter.eclipse.ui", Activator.PLUGIN_ID);
	}

	/*@Test
	public void testStartBundleContext() {
		fail("Not yet implemented");
	}

	@Test
	public void testStopBundleContext() {
		fail("Not yet implemented");
	}*/

	@Test
	public void testGetDefault() {
		Activator activator = Activator.getDefault();
		Assert.assertNotNull(activator);
	}

	/*@Test
	public void testGetClient() {
		fail("Not yet implemented");
	}

	@Test
	public void testTestServiceStatus() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNavigatorViewer() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetNavigatorViewer() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProjectHistoryElements() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetProjectHistoryElements() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSelectedProjects() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetSelectedProjects() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetImage() {
		fail("Not yet implemented");
	}*/

}
