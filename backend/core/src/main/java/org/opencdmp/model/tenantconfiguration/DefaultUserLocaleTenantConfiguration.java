package org.opencdmp.model.tenantconfiguration;

public class DefaultUserLocaleTenantConfiguration {
	private String timezone;
	public static final String _timezone = "timezone";
	private String language;
	public static final String _language = "language";
	private String culture;
	public static final String _culture = "culture";

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCulture() {
		return culture;
	}

	public void setCulture(String culture) {
		this.culture = culture;
	}
}

