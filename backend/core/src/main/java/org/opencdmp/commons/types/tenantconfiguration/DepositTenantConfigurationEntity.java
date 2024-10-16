package org.opencdmp.commons.types.tenantconfiguration;

import org.opencdmp.commons.types.deposit.DepositSourceEntity;

import java.util.List;

public class DepositTenantConfigurationEntity {
	private List<DepositSourceEntity> sources;
	private boolean disableSystemSources;

	public List<DepositSourceEntity> getSources() {
		return sources;
	}

	public void setSources(List<DepositSourceEntity> sources) {
		this.sources = sources;
	}

	public boolean getDisableSystemSources() {
		return disableSystemSources;
	}

	public void setDisableSystemSources(boolean disableSystemSources) {
		this.disableSystemSources = disableSystemSources;
	}
}
