package org.opencdmp.service.maintenance;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.UUID;

@ConfigurationProperties(prefix = "kpi.user-indicator")
public class KpiProperties {
	private UUID id;

	private String label;

	private String description;

	private String url;

	private String code;

	private List<String> roles;

	private List<String> tenantRoles;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public List<String> getTenantRoles() {
		return tenantRoles;
	}

	public void setTenantRoles(List<String> tenantRoles) {
		this.tenantRoles = tenantRoles;
	}
}
