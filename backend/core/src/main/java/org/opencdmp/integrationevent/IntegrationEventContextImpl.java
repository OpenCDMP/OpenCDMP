package org.opencdmp.integrationevent;

import gr.cite.rabbitmq.IntegrationEventContext;

import java.util.UUID;

public class IntegrationEventContextImpl implements IntegrationEventContext {
	private UUID tenant;

	public IntegrationEventContextImpl() {
	}

	public UUID getTenant() {
		return tenant;
	}

	public void setTenant(UUID tenant) {
		this.tenant = tenant;
	}
}
