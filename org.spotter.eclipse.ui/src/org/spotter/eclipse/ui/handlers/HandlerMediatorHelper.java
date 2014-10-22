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
package org.spotter.eclipse.ui.handlers;

import java.util.HashMap;
import java.util.Map;

/**
 * A helper class that can be used by implementors of {@link IHandlerMediator}
 * to delegate to.
 * 
 * @author Denis Knoepfle
 * 
 */
public class HandlerMediatorHelper implements IHandlerMediator {

	private final Map<String, Object> handlers = new HashMap<>();

	@Override
	public boolean canHandle(String commandId) {
		return handlers.containsKey(commandId);
	}

	@Override
	public Object getHandler(String commandId) {
		return handlers.get(commandId);
	}

	@Override
	public void addHandler(String commandId, Object handler) {
		handlers.put(commandId, handler);
	}

	@Override
	public void removeHandler(String commandId) {
		handlers.remove(commandId);
	}

}
