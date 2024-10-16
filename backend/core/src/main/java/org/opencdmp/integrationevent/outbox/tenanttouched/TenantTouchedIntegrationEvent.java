package org.opencdmp.integrationevent.outbox.tenanttouched;

import org.opencdmp.integrationevent.TrackedEvent;

import java.util.UUID;

public class TenantTouchedIntegrationEvent extends TrackedEvent {

    private UUID id;

    private String code;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
