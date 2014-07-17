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
package org.spotter.eclipse.ui.providers;

import org.eclipse.swt.graphics.Image;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.model.ExtensionItem;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;
import org.spotter.shared.result.model.ResultsContainer;
import org.spotter.shared.result.model.SpotterResult;

/**
 * An image provider for extension items displayed in the results view.
 * 
 * @author Denis Knoepfle
 * 
 */
public class ResultExtensionsImageProvider extends SpotterExtensionsImageProvider {

	private static final Image IMG_NO_LOOKUP = Activator.getImage("icons/exclamation.png");
	private static final Image IMG_DETECTED = Activator.getImage("icons/flag-red.png");
	private static final Image IMG_NOT_DETECTED = Activator.getImage("icons/flag-green.png");

	private ResultsContainer resultsContainer;

	public void setResultsContainer(ResultsContainer resultsContainer) {
		this.resultsContainer = resultsContainer;
	}

	@Override
	public Image getImage(ExtensionItem item) {
		Object xmlModel = item.getModelWrapper().getXMLModel();
		if (resultsContainer == null || !(xmlModel instanceof XPerformanceProblem)) {
			return null;
		}

		XPerformanceProblem problem = (XPerformanceProblem) xmlModel;
		String id = problem.getUniqueId();
		SpotterResult spotterResult = resultsContainer.getResultsMap().get(id);
		if (spotterResult == null) {
			return IMG_NO_LOOKUP;
		} else {
			return spotterResult.isDetected() ? IMG_DETECTED : IMG_NOT_DETECTED;
		}
	}

}
