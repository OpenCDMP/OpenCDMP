package org.opencdmp.event;

import org.opencdmp.commons.enums.TenantConfigurationType;

import java.util.UUID;

public class TenantConfigurationTouchedEvent {
	public TenantConfigurationTouchedEvent() {
	}

	public TenantConfigurationTouchedEvent(UUID tenantId, String tenantCode, TenantConfigurationType type) {
		this.tenantId = tenantId;
		this.tenantCode = tenantCode;
		this.type = type;
	}

	private UUID tenantId;
	private String tenantCode;
	private TenantConfigurationType type;

	public UUID getTenantId() {
		return tenantId;
	}

	public void setTenantId(UUID tenantId) {
		this.tenantId = tenantId;
	}

	public TenantConfigurationType getType() {
		return type;
	}

	public void setType(TenantConfigurationType type) {
		this.type = type;
	}

	public String getTenantCode() {
		return tenantCode;
	}

	public void setTenantCode(String tenantCode) {
		this.tenantCode = tenantCode;
	}
}
