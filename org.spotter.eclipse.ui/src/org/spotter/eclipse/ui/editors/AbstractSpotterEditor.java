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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.UICoreException;
import org.spotter.eclipse.ui.util.DialogUtils;

/**
 * Abstract super class for all Spotter editors. Implements basic functionality
 * and leaves open {@link #createPartControl(org.eclipse.swt.widgets.Composite)
 * createPartControl&nbsp;(...)} and several template methods to be implemented
 * by subclasses.
 * <p>
 * This implementation does not support the Save As operation, so extending
 * classes must override {@link #isSaveAsAllowed()} and {@link #doSaveAs()} to
 * change this. But <code>doSave(IProgressMonitor)</code> has basic
 * functionality implemented and may be used by subclasses.
 * </p>
 * 
 * @author Denis Knoepfle
 * 
 */
public abstract class AbstractSpotterEditor extends EditorPart {

	/**
	 * Title for general error dialogs.
	 */
	protected static final String TITLE_ERR_DIALOG = "Editor Error";
	/**
	 * Error message for failure when saving.
	 */
	protected static final String ERR_MSG_SAVE = "Could not save file!";
	/**
	 * Error message for failing to initialize editor.
	 */
	protected static final String ERR_MSG_INIT = "Could not initialize editor with configuration data.";

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSpotterEditor.class);
	private static final String ERR_MSG_INPUT_INVALID = "Invalid input: The editor's input is corrupted or the linked file does not exist.";
	private static final String MSG_CREATE_NEW = "The required file '%s' for project '%s' could not be found. Do you want to create a new file instead?";
	private static final String MSG_REPAIR_CORRUPTED = "The file '%s' of project '%s' is corrupted or not applicable for "
			+ "this editor type. Do you want to create a new file instead?\n\nWarning: This may cause "
			+ "loss of data! You should manually backup any important data before you continue.";
	private static final String ERR_MSG_MAKE_APPLICABLE_FAILED = "It was not possible to create an applicable editor input for this editor!";
	private static final String TITLE_INPUT_INVALID = "Input invalid";

	private boolean dirtyFlag = false;

	/**
	 * @return The name of the editor
	 */
	protected abstract String getEditorName();

	/**
	 * Implementing editors should create a suitable editor input and return it.
	 * 
	 * @param file
	 *            the resource file
	 * @return an editor input for this editor
	 * @throws UICoreException
	 *             when the editor input with the given file could not be
	 *             created
	 */
	protected abstract AbstractSpotterEditorInput createEditorInput(IFile file) throws UICoreException;

	/**
	 * Implementing editors should check whether the given input is readable and
	 * can be opened.
	 * 
	 * @param input
	 *            the input to check
	 * @return <code>true</code> if the input is readable, otherwise
	 *         <code>false</code>
	 * @throws Exception
	 *             if an error occurs while parsing the input
	 */
	protected abstract boolean isInputApplicable(AbstractSpotterEditorInput input) throws Exception;

	/**
	 * Implementing editors are supposed to repair the given input in order to
	 * make it applicable for the editor.
	 * 
	 * @param input
	 *            the corrupted input
	 * @throws UICoreException
	 *             when the editor was unable to make the input applicable again
	 */
	protected abstract void makeInputApplicable(AbstractSpotterEditorInput input) throws UICoreException;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		AbstractSpotterEditorInput spotterInput;
		if (input instanceof AbstractSpotterEditorInput) {
			spotterInput = (AbstractSpotterEditorInput) input;
		} else if (input instanceof FileEditorInput) {
			FileEditorInput fileInput = (FileEditorInput) input;
			try {
				spotterInput = createEditorInput(fileInput.getFile());
			} catch (UICoreException e) {
				spotterInput = null;
			}
			input = spotterInput;
		} else {
			throw new PartInitException("Invalid input type: '" + input.getClass().getName() + "' not understood.");
		}

		if (!checkAndRepairFileInput(spotterInput)) {
			throw new PartInitException(ERR_MSG_INPUT_INVALID);
		}

		setSite(site);
		setInput(input);
	}

	/**
	 * Save as is not allowed and currently not supported.
	 */
	@Override
	public void doSaveAs() {
	}

	/**
	 * Saves the contents of this editor.
	 * <p>
	 * This implementation resets the dirty flag including firing an according
	 * property change event and calling the monitor's <code>done()</code>
	 * method. Implementing editors should override this method to properly save
	 * their input but are advised to call this super method at the end to
	 * properly reflect changes.
	 * </p>
	 * 
	 * @param monitor
	 *            the monitor used to give progress feedback
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		updateDirtyFlag(false);
		if (monitor != null) {
			monitor.done();
		}
	}

	/**
	 * Returns the project this editor's input currently is associated with or
	 * <code>null</code> if none.
	 * 
	 * @return the associated project or <code>null</code> if none
	 */
	public IProject getProject() {
		IEditorInput editorInput = getEditorInput();
		if (editorInput instanceof AbstractSpotterEditorInput) {
			return ((AbstractSpotterEditorInput) editorInput).getProject();
		}

		return null;
	}

	@Override
	public boolean isDirty() {
		return dirtyFlag;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Marks the editor as dirty. This is a convenience method and has the same
	 * effect as calling <code>updateDirtyFlag(true)</code>.
	 */
	public void markDirty() {
		updateDirtyFlag(true);
	}

	/**
	 * Convenience method to open an editor.
	 * 
	 * @param editorInput
	 *            the editor input for the editor
	 * @param editorId
	 *            the id of the editor
	 */
	public static void openInstance(IEditorInput editorInput, String editorId) {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			page.openEditor(editorInput, editorId);
		} catch (PartInitException e) {
			// Usually exceptions of type PartInitException are handled in
			// CompatibilityPart.create() after openEditor(...) is called,
			// so a NullEditorInput page is shown, but when the exception
			// could not be handled, it will be thrown again and returns here
			LOGGER.error("Unhandled PartInitException will be rethrown", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Updates the dirty flag of this editor and fires an according property
	 * change event if the flag has changed.
	 * 
	 * @param dirtyFlag
	 *            dirtyFlag to set
	 */
	protected void updateDirtyFlag(boolean dirtyFlag) {
		boolean fireChange = this.dirtyFlag != dirtyFlag;
		this.dirtyFlag = dirtyFlag;
		if (fireChange) {
			firePropertyChange(PROP_DIRTY);
		}
	}

	/**
	 * Checks the given input whether it is applicable for this editor and asks
	 * the user whether the input should be repaired if it was corrupted.
	 * 
	 * @param input
	 *            the input to check
	 * @return <code>true</code> when the input was applicable or repaired,
	 *         otherwise <code>false</code>
	 */
	protected boolean checkAndRepairFileInput(AbstractSpotterEditorInput input) {
		if (input == null) {
			return false;
		}
		IFile containedFile = input.getFile();
		boolean applicableOrRepaired;
		String dialogQuestion;

		try {
			if (!containedFile.isSynchronized(IResource.DEPTH_ZERO)) {
				containedFile.refreshLocal(IResource.DEPTH_ZERO, null);
			}
		} catch (CoreException e) {
			applicableOrRepaired = false;
		}

		if (!containedFile.exists()) {
			dialogQuestion = MSG_CREATE_NEW;
			applicableOrRepaired = false;
		} else {
			dialogQuestion = MSG_REPAIR_CORRUPTED;
			try {
				applicableOrRepaired = isInputApplicable(input);
			} catch (Exception e) {
				applicableOrRepaired = false;
			}
		}

		String projectName = containedFile.getProject().getName();
		dialogQuestion = String.format(dialogQuestion, containedFile.getLocation(), projectName);
		if (!applicableOrRepaired && DialogUtils.openConfirm(TITLE_INPUT_INVALID, dialogQuestion)) {
			try {
				makeInputApplicable(input);
				applicableOrRepaired = true;
			} catch (UICoreException e) {
				DialogUtils.handleError(ERR_MSG_MAKE_APPLICABLE_FAILED, e);
			}
		}
		return applicableOrRepaired;
	}

}
