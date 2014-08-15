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
package org.spotter.shared.status;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Unit tests for Spotter Progress.
 * 
 * @author Alexander Wert
 * 
 */
public class SpotterProgressTest {

	private static final int _10000 = 10000;
	private static final double _0_8 = 0.8;
	private static final String MESSAGE = "test message";

	/**
	 * Tests {@link DiagnosisProgress}.
	 */
	@Test
	public void testDiagnosisProgress() {
		DiagnosisProgress diagProgress = new DiagnosisProgress("", DiagnosisStatus.EXPERIMENTING_STABLE_PHASE, _0_8,
				_10000, MESSAGE);
		Assert.assertEquals(_10000, diagProgress.getEstimatedRemainingDuration());
		Assert.assertEquals(_0_8, diagProgress.getEstimatedProgress());
		Assert.assertEquals(MESSAGE, diagProgress.getCurrentProgressMessage());
		Assert.assertEquals(DiagnosisStatus.EXPERIMENTING_STABLE_PHASE, diagProgress.getStatus());

		String message = "myMessage";
		diagProgress.setCurrentProgressMessage(message);
		Assert.assertEquals(message, diagProgress.getCurrentProgressMessage());

		double progress = 0.7;
		diagProgress.setEstimatedProgress(progress);
		Assert.assertEquals(progress, diagProgress.getEstimatedProgress(), 0.01);

		long duration = 879;
		diagProgress.setEstimatedRemainingDuration(duration);
		Assert.assertEquals(duration, diagProgress.getEstimatedRemainingDuration());

		diagProgress.setStatus(DiagnosisStatus.COLLECTING_DATA);
		Assert.assertEquals(DiagnosisStatus.COLLECTING_DATA, diagProgress.getStatus());
	}

	@Test
	public void testSpotterProgress() {
		SpotterProgress sProgress = new SpotterProgress();

		DiagnosisProgress diagProgress_1 = new DiagnosisProgress("", DiagnosisStatus.EXPERIMENTING_STABLE_PHASE, _0_8,
				_10000, MESSAGE);
		String message = "myMessage";
		DiagnosisProgress diagProgress_2 = new DiagnosisProgress("", DiagnosisStatus.COLLECTING_DATA, _0_8, _10000, message);

		String problemName_1 = "prob_A";
		String problemName_2 = "prob_B";

		Map<String, DiagnosisProgress> map = new HashMap<>();
		map.put(problemName_1, diagProgress_1);
		map.put(problemName_2, diagProgress_2);
		sProgress.setProblemProgressMapping(map);
		Assert.assertEquals(map, sProgress.getProblemProgressMapping());
		Assert.assertEquals(diagProgress_1, sProgress.getProgress(problemName_1));
		Assert.assertEquals(diagProgress_2, sProgress.getProgress(problemName_2));

		
		Assert.assertEquals(DiagnosisStatus.PENDING, sProgress.getProgress("invalidName").getStatus());

	}

}
