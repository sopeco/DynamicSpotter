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
package org.spotter.detection.godclass.analyze;

import org.spotter.detection.godclass.processor.data.Component;
import org.spotter.detection.godclass.processor.data.ProcessedData;
import org.spotter.shared.result.model.SpotterResult;

public class ComponentExclusionAnalyzer implements IGodClassAnalyzer {

	@Override
	public void analyze(ProcessedData processData, SpotterResult result) {

		result.setDetected(false);
		double totalMessagingTime = processData.getTotalMessagingTime();
		double totalCountMessages = processData.getTotalMessagesSent();

		for (Component comp : processData.getComponents()) {
			double messagingTimeWithoutComp = totalMessagingTime - comp.getTotalMessageSentDuration();
			double messageCountWithoutComp = totalCountMessages - comp.getMessagesSent();
			for (Component sender : processData.getComponents()) {
				if (!sender.getId().equals(comp.getId())) {
					Double receivingDuration = sender.getSendToDurationMap().get(comp.getId());
					if (receivingDuration != null) {
						messagingTimeWithoutComp -= receivingDuration;
					}

					Long receivingCount = sender.getSendToCountMap().get(comp.getId());
					if (receivingCount != null) {
						messageCountWithoutComp -= receivingCount;
					}

				}
			}

			double percentageTimeImprovement = 1 - messagingTimeWithoutComp / totalMessagingTime;

			double percentageCountImprovement = 1 - messageCountWithoutComp / totalCountMessages;

			if (percentageTimeImprovement > 0.4) {
				if (percentageCountImprovement > 0.4) {
					result.setDetected(true);
					result.addMessage("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
					result.addMessage("GodClass: Component " + comp.getId());
					result.addMessage("Total messaging time: " + totalMessagingTime);
					result.addMessage("Component's messaging time: " + (totalMessagingTime - messagingTimeWithoutComp));
					result.addMessage("Messaging time Improvement potential: " + percentageTimeImprovement);
					result.addMessage("Total message count: " + totalCountMessages);
					result.addMessage("Component's message count: " + (totalCountMessages - messageCountWithoutComp));
					result.addMessage("Message number improvement potential: " + percentageCountImprovement);
					result.addMessage("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
				} else {
					result.addMessage("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
					result.addMessage("Bottleneck: Component " + comp.getId());
					result.addMessage("Total messaging time: " + totalMessagingTime);
					result.addMessage("Component's messaging time: " + (totalMessagingTime - messagingTimeWithoutComp));
					result.addMessage("Messaging time Improvement potential: " + percentageTimeImprovement);
					result.addMessage("Total message count: " + totalCountMessages);
					result.addMessage("Component's message count: " + (totalCountMessages - messageCountWithoutComp));
					result.addMessage("Message number improvement potential: " + percentageCountImprovement);
					result.addMessage("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
				}

			} else {
				result.addMessage("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
				result.addMessage("Non-GodClass: Component " + comp.getId());
				result.addMessage("Total messaging time: " + totalMessagingTime);
				result.addMessage("Component's messaging time: " + (totalMessagingTime - messagingTimeWithoutComp));
				result.addMessage("Messaging time Improvement potential: " + percentageTimeImprovement);
				result.addMessage("Total message count: " + totalCountMessages);
				result.addMessage("Component's message count: " + (totalCountMessages - messageCountWithoutComp));
				result.addMessage("Message number improvement potential: " + percentageCountImprovement);
				result.addMessage("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
			}
		}

	}

}
