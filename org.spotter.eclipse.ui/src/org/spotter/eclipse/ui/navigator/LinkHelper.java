package org.spotter.eclipse.ui.navigator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ILinkHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.editors.AbstractSpotterEditorInput;
import org.spotter.eclipse.ui.editors.InstrumentationEditor;
import org.spotter.eclipse.ui.editors.MeasurementEditor;
import org.spotter.eclipse.ui.editors.SpotterConfigEditor;
import org.spotter.eclipse.ui.editors.WorkloadEditor;
import org.spotter.eclipse.ui.providers.NavigatorContentProvider;

/**
 * A helper class that links a selection in the Navigator to the corresponding
 * active editor and vice-versa if the "Link with Editor" option is enabled.
 * 
 * @author Denis Knoepfle
 *
 */
public class LinkHelper implements ILinkHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkHelper.class);
	
	private Map<String, Class<?>> editorsMapping;

	/**
	 * The constructor.
	 */
	public LinkHelper() {
		this.editorsMapping = createEditorsMapping();
	}

	private Map<String, Class<?>> createEditorsMapping() {
		Map<String, Class<?>> map = new HashMap<>();

		map.put(SpotterConfigEditor.ID, SpotterProjectConfigFile.class);
		map.put(InstrumentationEditor.ID, SpotterProjectConfigInstrumentation.class);
		map.put(MeasurementEditor.ID, SpotterProjectConfigMeasurement.class);
		map.put(WorkloadEditor.ID, SpotterProjectConfigWorkload.class);

		return map;
	}

	@Override
	public IStructuredSelection findSelection(IEditorInput anInput) {
		if (!(anInput instanceof AbstractSpotterEditorInput)) {
			throw new RuntimeException("Invalid input type");
		}
		AbstractSpotterEditorInput input = (AbstractSpotterEditorInput) anInput;
		IProject correspondingProject = input.getProject();
		String editorId = input.getEditorId();
		Class<?> selectionClazz = editorsMapping.get(editorId);
		
		if (selectionClazz == null) {
			return null;
		}
		
		NavigatorContentProvider provider = Activator.getDefault().getNavigatorContentProvider();
		CommonViewer viewer = provider.getViewer();
		Object[] parentObjects = provider.getChildren(viewer.getInput());
		
		for (Object rawParent : parentObjects) {
			ISpotterProjectElement parent = (ISpotterProjectElement) rawParent;
			if (parent.getProject().equals(correspondingProject)) {
				ISpotterProjectElement element = recursiveElementSearch(selectionClazz, parent);
				if (element != null) {
					// found a valid matching selection, so make it visible
					Activator.getDefault().getNavigatorViewer().reveal(element);
					return new StructuredSelection(element);
				}
				break;
			}
		}

		return null;
	}

	private ISpotterProjectElement recursiveElementSearch(Class<?> selectionClazz, ISpotterProjectElement parent) {
		if (!parent.hasChildren()) {
			return null;
		}
		
		for (Object rawChild : parent.getChildren()) {
			ISpotterProjectElement element = (ISpotterProjectElement) rawChild;
			if (selectionClazz.isInstance(element)) {
				return element;
			}
			element = recursiveElementSearch(selectionClazz, element);
			if (element != null) {
				return element;
			}
		}
		return null;
	}

	@Override
	public void activateEditor(IWorkbenchPage aPage, IStructuredSelection aSelection) {
		Object rawElement = aSelection.getFirstElement();
		if (!(rawElement instanceof ISpotterProjectElement) || !(rawElement instanceof IOpenableProjectElement)) {
			return;
		}
		if (!aPage.isEditorAreaVisible()) {
			aPage.setEditorAreaVisible(true);
		}

		ISpotterProjectElement element = (ISpotterProjectElement) rawElement;
		IOpenableProjectElement openableElement = (IOpenableProjectElement) rawElement;
		IProject project = element.getProject();
		
		for (IEditorReference reference : aPage.getEditorReferences()) {
			try {
				IEditorInput editorInput = reference.getEditorInput();
				if (editorInput instanceof AbstractSpotterEditorInput) {
					AbstractSpotterEditorInput input = (AbstractSpotterEditorInput) editorInput;
					if (project.equals(input.getProject()) && input.getEditorId().equals(openableElement.getOpenId())) {
						aPage.activate(reference.getEditor(true));
						return;
					}
				}
			} catch (PartInitException e) {
				LOGGER.warn("Skipping editor reference: failed to retrieve related editor input");
			}
		}
	}

}
