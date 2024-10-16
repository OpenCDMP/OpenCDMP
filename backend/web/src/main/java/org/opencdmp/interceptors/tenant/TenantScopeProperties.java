package org.opencdmp.interceptors.tenant;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.List;

@ConfigurationProperties(prefix = "tenant.interceptor")
public class TenantScopeProperties {

	private String clientClaimsPrefix;
	public String getClientClaimsPrefix() {
		return clientClaimsPrefix;
	}
	public void setClientClaimsPrefix(String clientClaimsPrefix) {
		this.clientClaimsPrefix = clientClaimsPrefix;
	}

	private HashSet<String> whiteListedClients;
	public HashSet<String> getWhiteListedClients() {
		return whiteListedClients;
	}
	public void setWhiteListedClients(HashSet<String> whiteListedClients) {
		this.whiteListedClients = whiteListedClients;
	}

	private List<String> whiteListedEndpoints;
	public List<String> getWhiteListedEndpoints() {
		return whiteListedEndpoints;
	}
	public void setWhiteListedEndpoints(List<String> whiteListedEndpoints) {
		this.whiteListedEndpoints = whiteListedEndpoints;
	}

	private Boolean enforceTrustedTenant;
	public Boolean getEnforceTrustedTenant() {
		return enforceTrustedTenant;
	}
	public void setEnforceTrustedTenant(Boolean enforceTrustedTenant) {
		this.enforceTrustedTenant = enforceTrustedTenant;
	}

	private Boolean autoCreateTenantUser;

	public Boolean getAutoCreateTenantUser() {
		return autoCreateTenantUser;
	}

	public void setAutoCreateTenantUser(Boolean autoCreateTenantUser) {
		this.autoCreateTenantUser = autoCreateTenantUser;
	}
}
