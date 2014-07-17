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
package org.spotter.detection.gcoverloading;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.exceptions.MeasurementException;
import org.aim.api.instrumentation.description.InstrumentationDescription;
import org.aim.api.instrumentation.description.InstrumentationDescriptionBuilder;
import org.aim.api.measurement.dataset.DatasetCollection;
import org.aim.artifacts.sampler.GarbageCollectionSampler;
import org.lpe.common.extension.IExtension;
import org.spotter.core.detection.AbstractDetectionController;
import org.spotter.core.detection.IDetectionController;
import org.spotter.core.detection.IExperimentReuser;
import org.spotter.exceptions.WorkloadException;
import org.spotter.shared.result.model.SpotterResult;

/**
 * Detection controller for the Garbage Collection Overloading (GCO) problem.
 * 
 * @author Le-Huan Stefan Tran
 */
public class GCOverloadingDetectionController extends AbstractDetectionController implements IExperimentReuser {

	private long gcSamplingDelay;

	/**
	 * Constructor.
	 * 
	 * @param provider
	 *            extension provider.
	 */
	public GCOverloadingDetectionController(IExtension<IDetectionController> provider) {
		super(provider);

	}

	@Override
	protected void executeExperiments() throws InstrumentationException, MeasurementException, WorkloadException {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadProperties() {
		String gcSamplingDelayStr = getProblemDetectionConfiguration().getProperty(
				GCOverloadingExtension.GC_SAMPLING_DELAY_KEY);
		gcSamplingDelay = gcSamplingDelayStr != null ? Long.parseLong(gcSamplingDelayStr)
				: GCOverloadingExtension.GC_SAMPLING_DELAY_DEFAULT;

	}

	@Override
	public InstrumentationDescription getInstrumentationDescription() {

		InstrumentationDescriptionBuilder idBuilder = new InstrumentationDescriptionBuilder();
		return idBuilder.addSamplingInstruction(GarbageCollectionSampler.class, gcSamplingDelay).build();

	}

	@Override
	protected SpotterResult analyze(DatasetCollection data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getNumOfExperiments() {
		return 0;
	}

}
