package org.spotter.eclipse.ui.model;

import org.spotter.eclipse.ui.model.xml.IModelWrapper;

/**
 * A factory to produce {@link IExtensionItem}.
 * 
 * @author Denis Knoepfle
 * 
 */
public interface IExtensionItemFactory {

	/**
	 * Creates an extension item with no children and no model.
	 * 
	 * @return the newly created extension item
	 */
	IExtensionItem createExtensionItem();

	/**
	 * Creates an extension item with no parent and the given model.
	 * 
	 * @param modelWrapper
	 *            the model wrapper for this item
	 * @return the newly created extension item
	 */
	IExtensionItem createExtensionItem(IModelWrapper modelWrapper);

	/**
	 * Creates an extension item under the given parent.
	 * 
	 * @param parent
	 *            the parent of this item
	 * @param modelWrapper
	 *            the model wrapper for this item
	 * @return the newly created extension item
	 */
	IExtensionItem createExtensionItem(IExtensionItem parent, IModelWrapper modelWrapper);

}
