package org.opencdmp.model.descriptiontemplate;

import org.opencdmp.model.pluginconfiguration.PluginConfiguration;

import java.util.List;

public class Definition {

	public final static String _pages = "pages";
	private List<Page> pages;

	private List<PluginConfiguration> pluginConfigurations;
	public final static String _pluginConfigurations = "pluginConfigurations";


	public List<Page> getPages() {
		return pages;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

	public List<PluginConfiguration> getPluginConfigurations() {
		return pluginConfigurations;
	}

	public void setPluginConfigurations(List<PluginConfiguration> pluginConfigurations) {
		this.pluginConfigurations = pluginConfigurations;
	}

}
