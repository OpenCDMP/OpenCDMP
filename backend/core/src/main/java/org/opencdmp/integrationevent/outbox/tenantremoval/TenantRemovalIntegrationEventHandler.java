package org.opencdmp.integrationevent.outbox.tenantremoval;

import java.util.UUID;

public interface TenantRemovalIntegrationEventHandler {

    void handle(TenantRemovalIntegrationEvent event);

    void handle(UUID tenantId);

}
