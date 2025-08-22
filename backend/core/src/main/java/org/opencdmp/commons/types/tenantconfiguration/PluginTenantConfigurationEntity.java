package org.opencdmp.commons.types.tenantconfiguration;


import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;

import java.util.List;

public class PluginTenantConfigurationEntity {


	private List<PluginConfigurationEntity> pluginConfigurations;

	public List<PluginConfigurationEntity> getPluginConfigurations() {
		return pluginConfigurations;
	}

	public void setPluginConfigurations(List<PluginConfigurationEntity> pluginConfigurations) {
		this.pluginConfigurations = pluginConfigurations;
	}
}
