package org.opencdmp.event;

import java.util.UUID;

public class UserAddedToTenantEvent {
	public UserAddedToTenantEvent() {
	}

	public UserAddedToTenantEvent(UUID userId, UUID tenantId) {
		this.userId = userId;
		this.tenantId = tenantId;
	}

	private UUID userId;
	private UUID tenantId;

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public void setTenantId(UUID tenantId) {
		this.tenantId = tenantId;
	}
}
