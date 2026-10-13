package org.opencdmp.model.descriptiontemplate;


import org.opencdmp.model.iconconfiguration.IconConfiguration;
import org.opencdmp.model.pluginconfiguration.PluginConfiguration;

import java.util.List;

public class Definition {

	public final static String _sections = "sections";
	private List<Section> sections;

	private List<PluginConfiguration> pluginConfigurations;
	public final static String _pluginConfigurations = "pluginConfigurations";

	private IconConfiguration iconConfiguration;
	public final static String _iconConfiguration = "iconConfiguration";

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

	public IconConfiguration getIconConfiguration() {
		return iconConfiguration;
	}

	public void setIconConfiguration(IconConfiguration iconConfiguration) {
		this.iconConfiguration = iconConfiguration;
	}

}
