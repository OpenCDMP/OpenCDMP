package org.opencdmp.model.user;

import org.opencdmp.model.pluginconfiguration.PluginConfigurationUser;
import org.opencdmp.model.reference.Reference;

import java.util.List;

public class UserAdditionalInfo {
	private String avatarUrl;
	public static final String _avatarUrl = "avatarUrl";
	
	private String timezone;
	public static final String _timezone = "timezone";
	
	private String culture;
	public static final String _culture = "culture";
	
	private String language;
	public static final String _language = "language";
	
	private String roleOrganization;
	public static final String _roleOrganization = "roleOrganization";

	private Reference organization;
	public static final String _organization = "organization";

	private List<PluginConfigurationUser> pluginConfigurations;
	public static final String _pluginConfigurations = "pluginConfigurations";

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getCulture() {
		return culture;
	}

	public void setCulture(String culture) {
		this.culture = culture;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getRoleOrganization() {
		return roleOrganization;
	}

	public void setRoleOrganization(String roleOrganization) {
		this.roleOrganization = roleOrganization;
	}

	public Reference getOrganization() {
		return organization;
	}

	public void setOrganization(Reference organization) {
		this.organization = organization;
	}

	public List<PluginConfigurationUser> getPluginConfigurations() {
		return pluginConfigurations;
	}

	public void setPluginConfigurations(List<PluginConfigurationUser> pluginConfigurations) {
		this.pluginConfigurations = pluginConfigurations;
	}
}
