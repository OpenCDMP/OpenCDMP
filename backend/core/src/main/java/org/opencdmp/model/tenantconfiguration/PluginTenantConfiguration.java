package org.opencdmp.model.tenantconfiguration;


import org.opencdmp.model.pluginconfiguration.PluginConfiguration;

import java.util.List;

public class PluginTenantConfiguration {

	private List<PluginConfiguration> pluginConfigurations;

	public List<PluginConfiguration> getPluginConfigurations() {
		return pluginConfigurations;
	}

	public void setPluginConfigurations(List<PluginConfiguration> pluginConfigurations) {
		this.pluginConfigurations = pluginConfigurations;
	}
}
