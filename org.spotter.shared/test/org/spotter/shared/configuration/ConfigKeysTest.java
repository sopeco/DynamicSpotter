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
package org.spotter.shared.configuration;

import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.lpe.common.config.ConfigParameterDescription;

public class ConfigKeysTest {
	@Test
	public void testconfigKeys() {
		Set<ConfigParameterDescription> pDescriptions = ConfigKeys.getSpotterConfigParamters();
		Assert.assertNotNull(getParameterWithName(ConfigKeys.WORKLOAD_MAXUSERS, pDescriptions));
		Assert.assertNotNull(getParameterWithName(ConfigKeys.EXPERIMENT_RAMP_UP_NUM_USERS_PER_INTERVAL, pDescriptions));
		Assert.assertNotNull(getParameterWithName(ConfigKeys.EXPERIMENT_RAMP_UP_INTERVAL_LENGTH, pDescriptions));
		Assert.assertNotNull(getParameterWithName(ConfigKeys.EXPERIMENT_COOL_DOWN_NUM_USERS_PER_INTERVAL, pDescriptions));
		Assert.assertNotNull(getParameterWithName(ConfigKeys.EXPERIMENT_COOL_DOWN_INTERVAL_LENGTH, pDescriptions));
		Assert.assertNotNull(getParameterWithName(ConfigKeys.EXPERIMENT_DURATION, pDescriptions));
		Assert.assertNotNull(getParameterWithName(ConfigKeys.OMIT_EXPERIMENTS, pDescriptions));
		Assert.assertNotNull(getParameterWithName(ConfigKeys.DUMMY_EXPERIMENT_DATA, pDescriptions));
		Assert.assertNotNull(getParameterWithName(ConfigKeys.PREWARUMUP_DURATION, pDescriptions));

	}

	private ConfigParameterDescription getParameterWithName(String name, Set<ConfigParameterDescription> pDescriptions) {
		for (ConfigParameterDescription pdecr : pDescriptions) {
			if (pdecr.getName().equals(name)) {
				return pdecr;
			}
		}
		return null;
	}

}
