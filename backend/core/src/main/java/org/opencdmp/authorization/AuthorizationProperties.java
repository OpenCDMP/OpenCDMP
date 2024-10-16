package org.opencdmp.authorization;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "authorization")
public class AuthorizationProperties {

	private List<String> globalAdminRoles;
	private String adminRole;
	private String tenantAdminRole;
	private String globalUserRole;
	private String tenantUserRole;
	private Boolean autoAssignGlobalAdminToNewTenants;
	private List<String> allowedTenantRoles;
	private List<String> allowedGlobalRoles;

	public List<String> getGlobalAdminRoles() {
		return this.globalAdminRoles;
	}

	public void setGlobalAdminRoles(List<String> globalAdminRoles) {
		this.globalAdminRoles = globalAdminRoles;
	}

	public String getAdminRole() {
		return this.adminRole;
	}

	public void setAdminRole(String adminRole) {
		this.adminRole = adminRole;
	}

	public String getTenantAdminRole() {
		return this.tenantAdminRole;
	}

	public void setTenantAdminRole(String tenantAdminRole) {
		this.tenantAdminRole = tenantAdminRole;
	}

	public String getGlobalUserRole() {
		return this.globalUserRole;
	}

	public void setGlobalUserRole(String globalUserRole) {
		this.globalUserRole = globalUserRole;
	}

	public String getTenantUserRole() {
		return this.tenantUserRole;
	}

	public void setTenantUserRole(String tenantUserRole) {
		this.tenantUserRole = tenantUserRole;
	}

	public Boolean getAutoAssignGlobalAdminToNewTenants() {
		return this.autoAssignGlobalAdminToNewTenants;
	}

	public void setAutoAssignGlobalAdminToNewTenants(Boolean autoAssignGlobalAdminToNewTenants) {
		this.autoAssignGlobalAdminToNewTenants = autoAssignGlobalAdminToNewTenants;
	}

	public List<String> getAllowedTenantRoles() {
		return this.allowedTenantRoles;
	}

	public void setAllowedTenantRoles(List<String> allowedTenantRoles) {
		this.allowedTenantRoles = allowedTenantRoles;
	}

	public List<String> getAllowedGlobalRoles() {
		return this.allowedGlobalRoles;
	}

	public void setAllowedGlobalRoles(List<String> allowedGlobalRoles) {
		this.allowedGlobalRoles = allowedGlobalRoles;
	}
}
