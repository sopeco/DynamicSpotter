package org.spotter.eclipse.ui.model;

import org.spotter.eclipse.ui.model.xml.IModelWrapper;

/**
 * Factory to create immutable extension items without any additional
 * functionality.
 * 
 * @author Denis Knöpfle
 * 
 */
public class ImmutableExtensionItemFactory implements IExtensionItemFactory {

	@Override
	public IExtensionItem createExtensionItem() {
		return new ExtensionItem();
	}

	@Override
	public IExtensionItem createExtensionItem(IModelWrapper modelWrapper) {
		return new ExtensionItem(modelWrapper);
	}

	@Override
	public IExtensionItem createExtensionItem(IExtensionItem parent, IModelWrapper modelWrapper) {
		return new ExtensionItem(parent, modelWrapper);
	}

}
