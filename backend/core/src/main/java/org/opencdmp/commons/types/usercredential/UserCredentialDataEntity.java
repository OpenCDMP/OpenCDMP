package org.opencdmp.commons.types.usercredential;

import java.util.List;

public class UserCredentialDataEntity {
	private List<String> externalProviderNames;
	private String email;

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
