package org.opencdmp.model.tenantconfiguration;

public class CssColorsTenantConfiguration {
	private String primaryColor;
	public static final String _primaryColor = "primaryColor";

	private String cssOverride;
	public static final String _cssOverride = "cssOverride";

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
