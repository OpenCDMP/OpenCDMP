package org.opencdmp.integrationevent.outbox.accountingentrycreated;

import org.opencdmp.commons.enums.accounting.AccountingMeasureType;
import org.opencdmp.commons.enums.accounting.AccountingValueType;
import org.opencdmp.integrationevent.TrackedEvent;

import java.time.Instant;
import java.util.UUID;

public class AccountingEntryCreatedIntegrationEvent extends TrackedEvent {

    private Instant timeStamp;

    private String serviceId;
    public static final String _serviceId = "service";

    private String resource;
    public static final String _resource = "resource";

    private String action;
    public static final String _action = "action";

    private AccountingMeasureType measure;

    private AccountingValueType type;

    private Double value;

    private String userId;

    private UUID tenant;

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public AccountingMeasureType getMeasure() {
        return measure;
    }

    public void setMeasure(AccountingMeasureType measure) {
        this.measure = measure;
    }

    public AccountingValueType getType() {
        return type;
    }

    public void setType(AccountingValueType type) {
        this.type = type;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UUID getTenant() {
        return tenant;
    }

    public void setTenant(UUID tenant) {
        this.tenant = tenant;
    }
}
