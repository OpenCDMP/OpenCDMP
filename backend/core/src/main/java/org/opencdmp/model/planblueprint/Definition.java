package org.opencdmp.model.planblueprint;


import org.opencdmp.model.pluginconfiguration.PluginConfiguration;

import java.util.List;

public class Definition {

	public final static String _sections = "sections";
	private List<Section> sections;

	private List<PluginConfiguration> pluginConfigurations;
	public final static String _pluginConfigurations = "pluginConfigurations";

	public List<Section> getSections() {
		return sections;
	}

	public void setSections(List<Section> sections) {
		this.sections = sections;
	}

	public List<PluginConfiguration> getPluginConfigurations() {
		return pluginConfigurations;
	}

	public void setPluginConfigurations(List<PluginConfiguration> pluginConfigurations) {
		this.pluginConfigurations = pluginConfigurations;
	}
}
