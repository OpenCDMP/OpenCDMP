package org.opencdmp.event;

import java.util.UUID;

public class PlanStatusTouchedEvent {

    public PlanStatusTouchedEvent() {
    }

    public PlanStatusTouchedEvent(UUID id, String tenantCode) {
        this.id = id;
        this.tenantCode = tenantCode;
    }

    private UUID id;

    private String tenantCode;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }
}
