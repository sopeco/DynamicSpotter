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
package org.spotter.eclipse.ui.editors;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.UICoreException;
import org.spotter.eclipse.ui.editors.factory.ElementFactory;
import org.spotter.eclipse.ui.model.BasicEditorExtensionItemFactory;
import org.spotter.eclipse.ui.model.ExtensionMetaobject;
import org.spotter.eclipse.ui.model.IExtensionItem;
import org.spotter.eclipse.ui.model.IExtensionItemFactory;
import org.spotter.eclipse.ui.model.xml.HierarchyFactory;
import org.spotter.eclipse.ui.model.xml.HierarchyModelWrapper;
import org.spotter.eclipse.ui.model.xml.IModelWrapper;
import org.spotter.eclipse.ui.util.DialogUtils;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;
import org.spotter.shared.configuration.SpotterExtensionType;
import org.spotter.shared.hierarchy.model.RawHierarchyFactory;
import org.spotter.shared.hierarchy.model.XPerformanceProblem;

/**
 * An editor to edit a performance problem hierarchy. Overrides
 * <code>supportsHierarchicalExtensionItems()</code> to provide hierarchical
 * extension items.
 * 
 * @author Denis Knoepfle
 * 
 */
public class HierarchyEditor extends AbstractExtensionsEditor {

	/**
	 * The id of this editor.
	 */
	public static final String ID = "org.spotter.eclipse.ui.editors.hierarchy";

	private static final String EDITOR_NAME = "Hierarchy";
	private static final SpotterExtensionType EXTENSION_TYPE = SpotterExtensionType.DETECTION_EXTENSION;
	private static final Logger LOGGER = LoggerFactory.getLogger(HierarchyEditor.class);

	private XPerformanceProblem problemRoot;

	@Override
	protected String getEditorName() {
		return EDITOR_NAME;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			HierarchyEditorInput input = (HierarchyEditorInput) getEditorInput();
			SpotterProjectSupport.saveHierarchy(input.getFile(), input.getPerformanceProblemRoot());
			super.doSave(monitor);
		} catch (Exception e) {
			DialogUtils.handleError(ERR_MSG_SAVE, e);
		}
	}

	@Override
	public IExtensionItem getInitialExtensionsInput() {
		if (problemRoot == null) {
			HierarchyEditorInput editorInput = (HierarchyEditorInput) getEditorInput();
			problemRoot = editorInput.getPerformanceProblemRoot();
		}

		String projectName = getProject().getName();
		IExtensionItemFactory factory = new BasicEditorExtensionItemFactory();
		return createPerformanceProblemHierarchy(projectName, factory, problemRoot);
	}

	@Override
	public ExtensionMetaobject[] getAvailableExtensions() {
		String projectName = getProject().getName();
		ServiceClientWrapper client = Activator.getDefault().getClient(projectName);
		return client.getAvailableExtensions(EXTENSION_TYPE);
	}

	@Override
	protected AbstractSpotterEditorInput createEditorInput(IFile file) {
		return ElementFactory.createEditorInput(ID, file);
	}

	@Override
	public IModelWrapper createModelWrapper(Object parent, ExtensionMetaobject extensionComponent) {
		XPerformanceProblem container = (XPerformanceProblem) parent;
		XPerformanceProblem problem = new XPerformanceProblem();
		problem.setExtensionName(extensionComponent.getExtensionName());
		problem.setUniqueId(RawHierarchyFactory.generateUniqueId());
		if (container.getProblem() == null) {
			container.setProblem(new ArrayList<XPerformanceProblem>());
		}
		container.getProblem().add(problem);
		return new HierarchyModelWrapper(extensionComponent, container.getProblem(), problem);
	}

	@Override
	protected boolean supportsHierarchicalExtensionItems() {
		return true;
	}

	@Override
	protected boolean isInputApplicable(AbstractSpotterEditorInput input) throws Exception {
		return HierarchyFactory.getInstance().parseHierarchyFile(input.getPath().toString()) != null;
	}

	@Override
	protected void makeInputApplicable(AbstractSpotterEditorInput input) throws UICoreException {
		String projectName = input.getProject().getName();
		XPerformanceProblem problem = Activator.getDefault().getClient(projectName).getDefaultHierarchy();

		SpotterProjectSupport.saveHierarchy(input.getFile(), problem);
	}

	/**
	 * Creates a performance problem hierarchy and stores it in an ExtensionItem
	 * that will be returned.
	 * 
	 * @param projectName
	 *            The name of the project the hierarchy is created for
	 * @param factory
	 *            The factory to use to create extension items
	 * @param rootProblem
	 *            The XML root problem model
	 * @return an ExtensionItem containing the hierarchy
	 */
	public static IExtensionItem createPerformanceProblemHierarchy(String projectName, IExtensionItemFactory factory,
			XPerformanceProblem rootProblem) {
		IModelWrapper rootModel = new HierarchyModelWrapper(null, null, rootProblem);
		IExtensionItem input = factory.createExtensionItem(rootModel);
		input.setIgnoreConnection(true);

		if (rootProblem.getProblem() == null) {
			rootProblem.setProblem(new ArrayList<XPerformanceProblem>());
		}
		ServiceClientWrapper client = Activator.getDefault().getClient(projectName);
		for (XPerformanceProblem problem : rootProblem.getProblem()) {
			try {
				buildRecursiveTree(client, factory, input, rootProblem, problem);
			} catch (UICoreException e) {
				String message = "Creating performance problem hierarchy failed.";
				LOGGER.warn(message, e);
				DialogUtils.openWarning(TITLE_ERR_DIALOG, DialogUtils.appendCause(message, e.getMessage()));
				return factory.createExtensionItem(rootModel);
			}
		}
		return input;
	}

	private static void buildRecursiveTree(ServiceClientWrapper client, IExtensionItemFactory factory,
			IExtensionItem parent, XPerformanceProblem parentProblem, XPerformanceProblem problem)
			throws UICoreException {
		String extName = problem.getExtensionName();

		if (client.getExtensionConfigParamters(extName) == null) {
			throw new UICoreException("Could not fully initialize ExtensionItem");
		}

		String projectName = client.getProjectName();
		ExtensionMetaobject extension = new ExtensionMetaobject(projectName, extName);
		IModelWrapper wrapper = new HierarchyModelWrapper(extension, parentProblem.getProblem(), problem);
		IExtensionItem child = factory.createExtensionItem(wrapper);
		parent.addItem(child);
		if (problem.getProblem() != null) {
			for (XPerformanceProblem childProblem : problem.getProblem()) {
				buildRecursiveTree(client, factory, child, problem, childProblem);
			}
		}
	}

}
