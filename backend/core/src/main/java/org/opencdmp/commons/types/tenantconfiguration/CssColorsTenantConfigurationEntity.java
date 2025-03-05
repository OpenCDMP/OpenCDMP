package org.opencdmp.commons.types.tenantconfiguration;

public class CssColorsTenantConfigurationEntity {
	private String primaryColor;
	private String cssOverride;

	public String getPrimaryColor() {
		return primaryColor;
	}

	public void setPrimaryColor(String primaryColor) {
		this.primaryColor = primaryColor;
	}

	public String getCssOverride() {
		return cssOverride;
	}

	public void setCssOverride(String cssOverride) {
		this.cssOverride = cssOverride;
	}
}
