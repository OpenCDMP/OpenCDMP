package org.opencdmp.integrationevent.outbox.tenanttouched;

public interface TenantTouchedIntegrationEventHandler {

    void handle(TenantTouchedIntegrationEvent event);

}
