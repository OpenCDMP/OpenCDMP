package org.opencdmp.integrationevent.outbox.tenantremoval;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opencdmp.integrationevent.TrackedEvent;

import java.util.UUID;

public class TenantRemovalIntegrationEvent extends TrackedEvent {

    @JsonProperty(value = "Id")
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}