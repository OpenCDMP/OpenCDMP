package org.opencdmp.commons.types.user;

import java.util.UUID;

public class AdditionalInfoEntity {
	private String avatarUrl;
	private String timezone;
	private String culture;
	private String language;
	private String roleOrganization;
	private UUID organizationId;

	public String getAvatarUrl() {
		return this.avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getTimezone() {
		return this.timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getCulture() {
		return this.culture;
	}

	public void setCulture(String culture) {
		this.culture = culture;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public UUID getOrganizationId() {
		return this.organizationId;
	}

	public void setOrganizationId(UUID organizationId) {
		this.organizationId = organizationId;
	}

	public String getRoleOrganization() {
		return this.roleOrganization;
	}

	public void setRoleOrganization(String roleOrganization) {
		this.roleOrganization = roleOrganization;
	}
}
