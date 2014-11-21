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
import org.spotter.eclipse.ui.model.IExtensionItem;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;
import org.spotter.shared.status.DiagnosisProgress;
import org.spotter.shared.status.SpotterProgress;

/**
 * An image provider for extension items displayed in the Active Run View.
 * 
 * @author Denis Knoepfle
 * 
 */
public class RunExtensionsImageProvider extends SpotterExtensionsImageProvider {

	private static final Image IMG_PENDING = Activator.getImage("icons/pending.gif");
	private static final Image IMG_DIAGNOSING = Activator.getImage("icons/diagnosis.png");
	private static final Image IMG_DETECTED = Activator.getImage("icons/flag-red.png");
	private static final Image IMG_NOT_DETECTED = Activator.getImage("icons/flag-green.png");

	private SpotterProgress spotterProgress;

	/**
	 * Sets the spotter progress.
	 * 
	 * @param spotterProgress
	 *            the spotter progress to set
	 */
	public void setSpotterProgress(SpotterProgress spotterProgress) {
		this.spotterProgress = spotterProgress;
	}

	@Override
	public Image getImage(IExtensionItem item) {
		Object xmlModel = item.getModelWrapper().getXMLModel();
		if (!(xmlModel instanceof XPerformanceProblem)) {
			return null;
		}

		XPerformanceProblem problem = (XPerformanceProblem) xmlModel;
		String id = problem.getUniqueId();
		DiagnosisProgress diagnosisProgress = spotterProgress == null ? null : spotterProgress.getProgress(id);
		if (diagnosisProgress == null) {
			return IMG_PENDING;
		} else {
			switch (diagnosisProgress.getStatus()) {
			case PENDING:
				return IMG_PENDING;
			case DETECTED:
				return IMG_DETECTED;
			case NOT_DETECTED:
				return IMG_NOT_DETECTED;
			default:
				return IMG_DIAGNOSING;
			}
		}
	}

}
