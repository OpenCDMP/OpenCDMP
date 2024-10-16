package org.opencdmp.event;

import java.util.UUID;

public class TenantTouchedEvent {
	public TenantTouchedEvent() {
	}

	public TenantTouchedEvent(UUID tenantId, String tenantCode) {
		this.tenantId = tenantId;
		this.tenantCode = tenantCode;
	}

	private UUID tenantId;
	private String tenantCode;

	public UUID getTenantId() {
		return this.tenantId;
	}

	public void setTenantId(UUID tenantId) {
		this.tenantId = tenantId;
	}

	public String getTenantCode() {
		return this.tenantCode;
	}

	public void setTenantCode(String tenantCode) {
		this.tenantCode = tenantCode;
	}

}
