package org.opencdmp.model;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.model.usagelimit.Definition;

import java.time.Instant;
import java.util.UUID;

public class UsageLimit {

    private UUID id;
    public static final String _id = "id";

    private String label;
    public static final String _label = "label";

    private UsageLimitTargetMetric targetMetric;
    public static final String _targetMetric = "targetMetric";

    private Long value;
    public static final String _value = "value";

    private IsActive isActive;
    public static final String _isActive = "isActive";

    private Definition definition;
    public static final String _definition = "definition";

    private Instant createdAt;
    public static final String _createdAt = "createdAt";

    private Instant updatedAt;
    public static final String _updatedAt = "updatedAt";

    private String hash;
    public static final String _hash = "hash";

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public UsageLimitTargetMetric getTargetMetric() {
        return targetMetric;
    }

    public void setTargetMetric(UsageLimitTargetMetric targetMetric) {
        this.targetMetric = targetMetric;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public IsActive getIsActive() {
        return isActive;
    }

    public void setIsActive(IsActive isActive) {
        this.isActive = isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Boolean getBelongsToCurrentTenant() {
        return belongsToCurrentTenant;
    }

    public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
        this.belongsToCurrentTenant = belongsToCurrentTenant;
    }
}
