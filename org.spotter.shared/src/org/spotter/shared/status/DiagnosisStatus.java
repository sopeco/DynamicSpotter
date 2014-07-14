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

/**
 * Diagnosis status for a Dynamic Spotter diagnosis step.
 * 
 * @author Alexander Wert
 * 
 */
public enum DiagnosisStatus {
	/**
	 * not yet analyzed.
	 */
	PENDING,
	/**
	 * diagnosis finished and corresponding problem detected.
	 */
	DETECTED,
	/**
	 * diagnosis finished but corresponding problem not detected.
	 */
	NOT_DETECTED,
	/**
	 * ramp-up phase of the experimentation phase.
	 */
	EXPERIMENTING_RAMP_UP,

	/**
	 * cool-down phase of the experimentation phase.
	 */
	EXPERIMENTING_COOL_DOWN,
	/**
	 * measurement phase of the experimentation phase.
	 */
	EXPERIMENTING_STABLE_PHASE,
	/**
	 * analysis is progress.
	 */
	ANALYSING,
	/**
	 * initialization in progress.
	 */
	INITIALIZING,
	/**
	 * warm-up phase of the system under test.
	 */
	WARM_UP,
	/**
	 * instrumentation phase in progress.
	 */
	INSTRUMENTING,
	/**
	 * reversion of instrumentation.
	 */
	UNINSTRUMENTING,
	/**
	 * data collection in progress.
	 */
	COLLECTING_DATA
}
