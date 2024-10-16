package org.opencdmp.integrationevent.outbox.tenantdefaultlocaleremoval;

import javax.management.InvalidApplicationException;

public interface TenantDefaultLocaleRemovalIntegrationEventHandler {
	void handle(TenantDefaultLocaleRemovalIntegrationEvent event) throws InvalidApplicationException;
}
