package org.opencdmp.model.usercredential;

import java.util.List;

public class UserCredentialData {
	private List<String> externalProviderNames;
	public static final String _externalProviderNames = "externalProviderNames";
	private String email;
	public static final String _email = "email";

	public List<String> getExternalProviderNames() {
		return externalProviderNames;
	}

	public void setExternalProviderNames(List<String> externalProviderNames) {
		this.externalProviderNames = externalProviderNames;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
