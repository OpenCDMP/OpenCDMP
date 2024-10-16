package org.opencdmp.integrationevent.outbox.tenantdefaultlocaletouched;

import javax.management.InvalidApplicationException;

public interface TenantDefaultLocaleTouchedIntegrationEventHandler {
	void handle(TenantDefaultLocaleTouchedIntegrationEvent event) throws InvalidApplicationException;
}
