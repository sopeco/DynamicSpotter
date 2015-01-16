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
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import junit.framework.Assert;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.config.GlobalConfiguration;
import org.spotter.core.detection.DetectionControllerAccessor;
import org.spotter.core.result.ResultBlackboard;
import org.spotter.core.test.dummies.detection.DetectionA;
import org.spotter.core.test.dummies.detection.DetectionB;
import org.spotter.core.test.dummies.detection.DetectionD;
import org.spotter.core.test.dummies.detection.DetectionE;
import org.spotter.core.test.dummies.detection.DetectionF;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.ResultsContainer;

/**
 * Unit tests for performance Problem.
 * 
 * @author Alexander Wert
 * 
 */
public class HierarchyTest {

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
	public void testHierarchyCreation() throws URISyntaxException {
		URL url = HierarchyTest.class.getResource("/test-hierarchy.xml");
		String hierarchyFile = url.toURI().getPath();
		ResultsContainer rContainer = new ResultsContainer();

		Set<String> uniqueIds = new HashSet<>();

		PerformanceProblem root = HierarchyFactory.getInstance().createPerformanceProblemHierarchy(hierarchyFile,
				rContainer);
		Assert.assertNotNull(root);
		Assert.assertNull(root.getProblemName());
		Assert.assertFalse(root.isDetectable());
		Assert.assertNotNull(root.getChildren());
		Assert.assertEquals(1, root.getChildren().size());
		Assert.assertNotNull(root.getConfiguration());
		Assert.assertEquals(1, root.getConfiguration().size());
		uniqueIds.add(root.getUniqueId());
		Assert.assertNull(root.getDetectionController());

		PerformanceProblem problemA = root.getChildren().get(0);
		Assert.assertNotNull(problemA);
		Assert.assertEquals("DetectionA", problemA.getProblemName());
		Assert.assertTrue(problemA.isDetectable());
		Assert.assertNotNull(problemA.getChildren());
		Assert.assertEquals(2, problemA.getChildren().size());
		Assert.assertNotNull(problemA.getConfiguration());
		Assert.assertEquals(1, problemA.getConfiguration().size());
		uniqueIds.add(problemA.getUniqueId());
		Assert.assertTrue(problemA.getDetectionController() instanceof DetectionA);
		Assert.assertEquals(2, problemA.getDetectionController().getProvider().getConfigParameters().size());
		boolean foundParameter = false;
		for(ConfigParameterDescription cpd : problemA.getDetectionController().getProvider().getConfigParameters()){
			if(cpd.getName().equals("test.parameter")){
				foundParameter=true;
			}
		}
		Assert.assertTrue(foundParameter);
		

		PerformanceProblem problemB = problemA.getChildren().get(0);
		Assert.assertNotNull(problemB);
		Assert.assertEquals("DetectionB", problemB.getProblemName());
		Assert.assertTrue(problemB.isDetectable());
		Assert.assertNotNull(problemB.getChildren());
		Assert.assertEquals(0, problemB.getChildren().size());
		Assert.assertNotNull(problemB.getConfiguration());
		Assert.assertEquals(1, problemB.getConfiguration().size());
		Assert.assertEquals(2, problemB.getDetectionController().getProvider().getConfigParameters().size());
		uniqueIds.add(problemB.getUniqueId());
		Assert.assertTrue(problemB.getDetectionController() instanceof DetectionB);

		PerformanceProblem problemC = problemA.getChildren().get(1);
		Assert.assertNotNull(problemC);
		Assert.assertEquals("DetectionC", problemC.getProblemName());
		Assert.assertFalse(problemC.isDetectable());
		Assert.assertNotNull(problemC.getChildren());
		Assert.assertEquals(2, problemC.getChildren().size());
		Assert.assertNotNull(problemC.getConfiguration());
		Assert.assertEquals(1, problemC.getConfiguration().size());
		uniqueIds.add(problemC.getUniqueId());
		Assert.assertNull(problemC.getDetectionController());

		PerformanceProblem problemD = problemC.getChildren().get(0);
		Assert.assertNotNull(problemD);
		Assert.assertEquals("DetectionD", problemD.getProblemName());
		Assert.assertTrue(problemD.isDetectable());
		Assert.assertNotNull(problemD.getChildren());
		Assert.assertEquals(1, problemD.getChildren().size());
		Assert.assertNotNull(problemD.getConfiguration());
		Assert.assertEquals(1, problemD.getConfiguration().size());
		uniqueIds.add(problemD.getUniqueId());
		Assert.assertTrue(problemD.getDetectionController() instanceof DetectionD);

		PerformanceProblem problemF = problemD.getChildren().get(0);
		Assert.assertNotNull(problemF);
		Assert.assertEquals("DetectionF", problemF.getProblemName());
		Assert.assertTrue(problemF.isDetectable());
		Assert.assertNotNull(problemF.getChildren());
		Assert.assertEquals(0, problemF.getChildren().size());
		Assert.assertNotNull(problemF.getConfiguration());
		Assert.assertEquals(2, problemF.getConfiguration().size());
		Assert.assertEquals("test.value", problemF.getConfiguration().getProperty("test.key"));
		uniqueIds.add(problemF.getUniqueId());
		Assert.assertTrue(problemF.getDetectionController() instanceof DetectionF);

		PerformanceProblem problemE = problemC.getChildren().get(1);
		Assert.assertNotNull(problemE);
		Assert.assertEquals("DetectionE", problemE.getProblemName());
		Assert.assertTrue(problemE.isDetectable());
		Assert.assertNotNull(problemE.getChildren());
		Assert.assertEquals(0, problemE.getChildren().size());
		Assert.assertNotNull(problemE.getConfiguration());
		Assert.assertEquals(1, problemE.getConfiguration().size());
		uniqueIds.add(problemE.getUniqueId());
		Assert.assertTrue(problemE.getDetectionController() instanceof DetectionE);

		Assert.assertEquals(7, uniqueIds.size());

	}

	@Test
	public void testHierarchyInterpreter() throws URISyntaxException, InstrumentationException, MeasurementException,
			WorkloadException {
		URL url = HierarchyTest.class.getResource("/test-hierarchy.xml");
		String hierarchyFile = url.toURI().getPath();
		ResultsContainer rContainer = new ResultsContainer();

		PerformanceProblem root = HierarchyFactory.getInstance().createPerformanceProblemHierarchy(hierarchyFile,
				rContainer);
		HierarchyModelInterpreter hInterpreter = new HierarchyModelInterpreter(root);
		Assert.assertNull(hInterpreter.getCurrentProblem());

		PerformanceProblem problemA = hInterpreter.next();
		Assert.assertEquals("DetectionA", problemA.getProblemName());
		Assert.assertSame(problemA, hInterpreter.getCurrentProblem());

		ResultBlackboard.getInstance().putResult(problemA,
				DetectionControllerAccessor.analyzeProblem(problemA.getDetectionController()));

		PerformanceProblem problemB = hInterpreter.next();
		Assert.assertEquals("DetectionB", problemB.getProblemName());
		Assert.assertSame(problemB, hInterpreter.getCurrentProblem());
		ResultBlackboard.getInstance().putResult(problemB,
				DetectionControllerAccessor.analyzeProblem(problemB.getDetectionController()));

		PerformanceProblem problemD = hInterpreter.next();
		Assert.assertEquals("DetectionD", problemD.getProblemName());
		Assert.assertSame(problemD, hInterpreter.getCurrentProblem());
		ResultBlackboard.getInstance().putResult(problemD,
				DetectionControllerAccessor.analyzeProblem(problemD.getDetectionController()));

		PerformanceProblem problemE = hInterpreter.next();
		Assert.assertEquals("DetectionE", problemE.getProblemName());
		Assert.assertSame(problemE, hInterpreter.getCurrentProblem());
		ResultBlackboard.getInstance().putResult(problemE,
				DetectionControllerAccessor.analyzeProblem(problemE.getDetectionController()));

		Assert.assertNull(hInterpreter.next());

	}

	@Test(expected = RuntimeException.class)
	public void testInvalidHierarchyFile() {
		ResultsContainer rContainer = new ResultsContainer();

		HierarchyFactory.getInstance().createPerformanceProblemHierarchy("/wrong/file", rContainer);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidController() throws URISyntaxException {
		URL url = HierarchyTest.class.getResource("/invalid-hierarchy.xml");
		String hierarchyFile = url.toURI().getPath();
		ResultsContainer rContainer = new ResultsContainer();

		PerformanceProblem root = HierarchyFactory.getInstance().createPerformanceProblemHierarchy(hierarchyFile,
				rContainer);
	}
	@Test
	public void testEqualProblems() throws URISyntaxException{
		URL url = HierarchyTest.class.getResource("/test-hierarchy.xml");
		String hierarchyFile = url.toURI().getPath();
		ResultsContainer rContainerA = new ResultsContainer();

		PerformanceProblem rootA = HierarchyFactory.getInstance().createPerformanceProblemHierarchy(hierarchyFile,
				rContainerA);
		
		ResultsContainer rContainerB = new ResultsContainer();

		PerformanceProblem rootB= HierarchyFactory.getInstance().createPerformanceProblemHierarchy(hierarchyFile,
				rContainerB);
		
		Assert.assertNotSame(rootA, rootB);
		Assert.assertEquals(rootA, rootB);
		Assert.assertNotSame(rootA.getChildren().get(0), rootB.getChildren().get(0));
		Assert.assertEquals(rootA.getChildren().get(0), rootB.getChildren().get(0));
		
		
		PerformanceProblem otherProblem = new PerformanceProblem("abc");
		PerformanceProblem yetAnotherProblem = new PerformanceProblem("abc");
		
		Assert.assertNotSame(rootA, otherProblem);
		Assert.assertFalse(rootA.equals(otherProblem));
		Assert.assertFalse(rootA.equals(null));
		Assert.assertEquals(rootA, rootA);
		Assert.assertNotSame(yetAnotherProblem, otherProblem);
		Assert.assertEquals(yetAnotherProblem, otherProblem);
		Assert.assertEquals(yetAnotherProblem.hashCode(), otherProblem.hashCode());
		
		PerformanceProblem nullProblem = new PerformanceProblem(null);
		PerformanceProblem nullProblem_2 = new PerformanceProblem(null);
		Assert.assertEquals(nullProblem, nullProblem_2);
		Assert.assertEquals(nullProblem.hashCode(), nullProblem_2.hashCode());
		Assert.assertFalse(nullProblem.equals(otherProblem));
		Assert.assertFalse(otherProblem.equals(nullProblem));
		Assert.assertFalse(otherProblem.equals(new Object()));
	}
}
