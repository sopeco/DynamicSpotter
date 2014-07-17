package org.spotter.core.test.dummies.satellites;

import org.lpe.common.config.ConfigParameterDescription;
import org.lpe.common.util.LpeSupportedTypes;
import org.spotter.core.workload.AbstractWorkloadExtension;
import org.spotter.core.workload.IWorkloadAdapter;

public class DummyWorkloadExtension extends AbstractWorkloadExtension {

	@Override
	public String getName() {
		return "DummyWorkload";
	}

	@Override
	public IWorkloadAdapter createExtensionArtifact() {
		return new DummyWorkload(this);
	}

	@Override
	protected void initializeConfigurationParameters() {
		ConfigParameterDescription parDescription = new ConfigParameterDescription("test.workload.parameter",
				LpeSupportedTypes.Integer);
		addConfigParameter(parDescription);
	}

	@Override
	public boolean testConnection(String host, String port) {
		return true;
	}

	@Override
	public boolean isRemoteExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
