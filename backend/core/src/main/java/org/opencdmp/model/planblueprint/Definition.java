package org.opencdmp.model.planblueprint;


import org.opencdmp.model.iconconfiguration.IconConfiguration;
import org.opencdmp.model.pluginconfiguration.PluginConfiguration;

import java.util.List;

public class Definition {

	public final static String _sections = "sections";
	private List<RootSection> sections;

	private List<PluginConfiguration> pluginConfigurations;
	public final static String _pluginConfigurations = "pluginConfigurations";

	private PlanCreationConsent planCreationConsent;
	public final static String _planCreationConsent = "planCreationConsent";

	private IconConfiguration iconConfiguration;
	public final static String _iconConfiguration = "iconConfiguration";

	public List<RootSection> getSections() {
		return sections;
	}

	public void setSections(List<RootSection> sections) {
		this.sections = sections;
	}

	public List<PluginConfiguration> getPluginConfigurations() {
		return pluginConfigurations;
	}

	public void setPluginConfigurations(List<PluginConfiguration> pluginConfigurations) {
		this.pluginConfigurations = pluginConfigurations;
	}

	public PlanCreationConsent getPlanCreationConsent() {
		return planCreationConsent;
	}

	public void setPlanCreationConsent(PlanCreationConsent planCreationConsent) {
		this.planCreationConsent = planCreationConsent;
	}

	public IconConfiguration getIconConfiguration() {
		return iconConfiguration;
	}

	public void setIconConfiguration(IconConfiguration iconConfiguration) {
		this.iconConfiguration = iconConfiguration;
	}
}
