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
package org.spotter.eclipse.ui.navigator.actions;

import org.eclipse.ui.internal.navigator.resources.actions.NewActionProvider;

/**
 * <p>
 * A custom new action provider that extends {@link NewActionProvider}. This
 * provider is used instead of the internal one in case there are changes that
 * would affect the current required functionality, so that given that scenario
 * the old behavior could be restored easily without modifying the
 * <em>plugin.xml</em>.
 * </p>
 * <p>
 * This provider is used to control the enablement of the custom content that is
 * contributed to the DynamicSpotter Project Navigator.
 * </p>
 * 
 * @author Denis Knoepfle
 * 
 */
@SuppressWarnings("restriction")
public class CustomNewActionProvider extends NewActionProvider {

}
