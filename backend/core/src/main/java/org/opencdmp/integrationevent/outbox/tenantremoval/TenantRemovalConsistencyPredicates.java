package org.opencdmp.integrationevent.outbox.tenantremoval;

import org.opencdmp.integrationevent.inbox.ConsistencyPredicates;

import java.util.UUID;

public class TenantRemovalConsistencyPredicates implements ConsistencyPredicates {

    private UUID tenantId;

    public TenantRemovalConsistencyPredicates(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

}
