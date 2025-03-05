package org.opencdmp.service.kpi;

import org.opencdmp.integrationevent.outbox.indicator.IndicatorMetadata;
import org.opencdmp.integrationevent.outbox.indicator.IndicatorSchema;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.UUID;

@ConfigurationProperties(prefix = "kpi.indicator")
public class IndicatorProperties {
	private UUID id;

	private IndicatorMetadata metadata;

	private List<String> roles;

	private List<String> tenantRoles;

	private IndicatorSchema schema;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public IndicatorMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(IndicatorMetadata metadata) {
		this.metadata = metadata;
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

	public IndicatorSchema getSchema() {
		return schema;
	}

	public void setSchema(IndicatorSchema schema) {
		this.schema = schema;
	}
}
