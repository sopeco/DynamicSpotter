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
package org.spotter.detection.godclass;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.instrumentation.description.InstrumentationDescription;
import org.aim.api.instrumentation.description.InstrumentationDescriptionBuilder;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.artifacts.probes.JmsCommunicationProbe;
import org.aim.artifacts.scopes.JmsScope;
import org.lpe.common.extension.IExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;
import org.spotter.detection.godclass.analyze.ComponentExclusionAnalyzer;
import org.spotter.detection.godclass.processor.DataProcessor;
import org.spotter.detection.godclass.processor.data.ProcessedData;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.SpotterResult;

/**
 * God Class antipattern detection controller.
 * 
 * 
 * 
 * 
 * @author Alexander Wert
 * 
 */
public class GodClassDetectionController extends AbstractDetectionController {

	private static final Logger LOGGER = LoggerFactory.getLogger(GodClassDetectionController.class);

	private static final int EXPERIMENT_STEPS = 1;

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *            extension provider.
	 */
	public GodClassDetectionController(IExtension<IDetectionController> provider) {
		super(provider);
	}

	@Override
	protected void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException {

		executeDefaultExperimentSeries(GodClassDetectionController.class, EXPERIMENT_STEPS,
				getInstrumentationDescription());

	}

	private InstrumentationDescription getInstrumentationDescription() throws InstrumentationException {
		InstrumentationDescriptionBuilder idBuilder = new InstrumentationDescriptionBuilder();
		return idBuilder.addAPIInstrumentation(JmsScope.class).addProbe(JmsCommunicationProbe.class).entityDone()
				.build();
	}

	@Override
	protected SpotterResult analyze(DatasetCollection data) {
		LOGGER.debug("Analyze data for GodClass Antipattern..");

		SpotterResult result = new SpotterResult();
		// result.addMessage("Scope: " + scope);

		/** Process the raw measurement data */
		LOGGER.debug("process data..");
		ProcessedData processData = DataProcessor.processData(data);

		/** Analyze the processed data */
		LOGGER.debug("analyze data..");
		ComponentExclusionAnalyzer analyzer = new ComponentExclusionAnalyzer();
		analyzer.analyze(processData, result);
		// Analyzer.analyzeData(processData, result);
		result.setDetected(true);
		return result;
	}

	@Override
	public void loadProperties() {
		// TODO Auto-generated method stub

	}

	@Override
	protected int getNumOfExperiments() {
		return EXPERIMENT_STEPS;
	}
}
