package org.opencdmp.model.tenantconfiguration;

import org.opencdmp.commons.types.deposit.DepositSourceEntity;
import org.opencdmp.model.deposit.DepositSource;

import java.util.List;

public class DepositTenantConfiguration {
	private List<DepositSource> sources;
	public static final String _sources = "sources";
	private Boolean disableSystemSources;
	public static final String _disableSystemSources = "disableSystemSources";

	public List<DepositSource> getSources() {
		return sources;
	}

	public void setSources(List<DepositSource> sources) {
		this.sources = sources;
	}

	public Boolean getDisableSystemSources() {
		return disableSystemSources;
	}

	public void setDisableSystemSources(Boolean disableSystemSources) {
		this.disableSystemSources = disableSystemSources;
	}
}
