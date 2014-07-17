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
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.lpe.common.config.ConfigParameterDescription;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.ServiceClientWrapper;
import org.spotter.eclipse.ui.UICoreException;
import org.spotter.eclipse.ui.editors.factory.ElementFactory;
import org.spotter.eclipse.ui.model.ExtensionMetaobject;
import org.spotter.eclipse.ui.model.ExtensionItem;
import org.spotter.eclipse.ui.model.xml.HierarchyFactory;
import org.spotter.eclipse.ui.model.xml.HierarchyModelWrapper;
import org.spotter.eclipse.ui.model.xml.IModelWrapper;
import org.spotter.eclipse.ui.util.SpotterProjectSupport;
import org.spotter.shared.configuration.SpotterExtensionType;
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

	private XPerformanceProblem performanceProblems;

	@Override
	protected String getEditorName() {
		return EDITOR_NAME;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		AbstractSpotterEditorInput input = (AbstractSpotterEditorInput) getEditorInput();

		try {
			HierarchyFactory factory = HierarchyFactory.getInstance();
			XPerformanceProblem problem = factory.parseHierarchyFile(input.getPath().toString());

			applyChanges(problem);
			SpotterProjectSupport.saveHierarchy(input.getFile(), problem);

			super.doSave(monitor);
		} catch (Exception e) {
			MessageDialog.openError(null, TITLE_ERR_DIALOG, ERR_MSG_SAVE + e.getMessage());
		}
	}

	@Override
	public ExtensionItem getInitialExtensionsInput() {
		if (performanceProblems == null) {
			performanceProblems = new XPerformanceProblem();
			HierarchyEditorInput editorInput = (HierarchyEditorInput) getEditorInput();
			performanceProblems.setProblem(editorInput.getPerformanceProblems());
		}

		String projectName = getProject().getName();
		return createPerformanceProblemHierarchy(projectName, performanceProblems);
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
		problem.setUniqueId(UUID.randomUUID().toString());
		if (container.getProblem() == null) {
			container.setProblem(new ArrayList<XPerformanceProblem>());
		}
		container.getProblem().add(problem);
		return new HierarchyModelWrapper(extensionComponent, container.getProblem(), problem);
	}

	@Override
	protected void applyChanges(Object xmlModelRoot) {
		XPerformanceProblem problem = (XPerformanceProblem) xmlModelRoot;
		problem.setProblem(performanceProblems.getProblem());
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
		XPerformanceProblem problem = HierarchyFactory.getInstance().createProblemHierarchyRoot();
		SpotterProjectSupport.saveHierarchy(input.getFile(), problem);
	}

	/**
	 * Creates a performance problem hierarchy and stores it in an ExtensionItem
	 * that will be returned.
	 * 
	 * @param projectName
	 *            The name of the project the hierarchy is created for
	 * @param rootProblem
	 *            The XML root problem model
	 * @return an ExtensionItem containing the hierarchy
	 */
	public static ExtensionItem createPerformanceProblemHierarchy(String projectName, XPerformanceProblem rootProblem) {
		IModelWrapper rootModel = new HierarchyModelWrapper(null, null, rootProblem);
		ExtensionItem input = new ExtensionItem(rootModel);
		input.setIgnoreConnection(true);

		ServiceClientWrapper client = Activator.getDefault().getClient(projectName);
		for (XPerformanceProblem problem : rootProblem.getProblem()) {
			try {
				buildRecursiveTree(client, input, rootProblem, problem);
			} catch (UICoreException e) {
				MessageDialog.openWarning(null, TITLE_ERR_DIALOG,
						"Creating performance problem hierarchy failed. Cause: " + e.getMessage());
				return new ExtensionItem(rootModel);
			}
		}
		return input;
	}

	private static void buildRecursiveTree(ServiceClientWrapper client, ExtensionItem parent,
			XPerformanceProblem parentProblem, XPerformanceProblem problem) throws UICoreException {
		String extName = problem.getExtensionName();
		Set<ConfigParameterDescription> configParams = client.getExtensionConfigParamters(extName);

		if (configParams == null) {
			throw new UICoreException("Could not fully initialize ExtensionItem");
		}

		String projectName = client.getProjectName();
		ExtensionMetaobject extension = new ExtensionMetaobject(projectName, extName, configParams);
		IModelWrapper wrapper = new HierarchyModelWrapper(extension, parentProblem.getProblem(), problem);
		ExtensionItem child = new ExtensionItem(wrapper);
		parent.addItem(child);
		if (problem.getProblem() != null) {
			for (XPerformanceProblem childProblem : problem.getProblem()) {
				buildRecursiveTree(client, child, problem, childProblem);
			}
		}
	}

}
