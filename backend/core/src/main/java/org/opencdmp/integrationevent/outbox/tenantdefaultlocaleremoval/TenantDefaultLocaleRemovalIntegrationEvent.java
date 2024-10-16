package org.opencdmp.integrationevent.outbox.tenantdefaultlocaleremoval;

import org.opencdmp.integrationevent.TrackedEvent;

import java.util.UUID;

public class TenantDefaultLocaleRemovalIntegrationEvent extends TrackedEvent {

	private UUID tenantId;

	public TenantDefaultLocaleRemovalIntegrationEvent() {
	}


	public UUID getTenantId() {
		return tenantId;
	}

	public void setTenantId(UUID tenantId) {
		this.tenantId = tenantId;
	}
}
