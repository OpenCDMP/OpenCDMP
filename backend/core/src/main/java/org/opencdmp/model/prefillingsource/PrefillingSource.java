package org.opencdmp.model.prefillingsource;

import org.opencdmp.commons.enums.IsActive;

import java.time.Instant;
import java.util.UUID;

public class PrefillingSource {

    private UUID id;
    public static final String _id = "id";

    private String label;
    public static final String _label = "label";

    private String code;
    public static final String _code = "code";

    private PrefillingSourceDefinition definition;
    public static final String _definition = "definition";

    private IsActive isActive;
    public static final String _isActive = "isActive";

    private Instant createdAt;
    public static final String _createdAt = "createdAt";

    private Instant updatedAt;
    public static final String _updatedAt = "updatedAt";

    public final static String _hash = "hash";
    private String hash;

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public PrefillingSourceDefinition getDefinition() {
        return this.definition;
    }

    public void setDefinition(PrefillingSourceDefinition definition) {
        this.definition = definition;
    }

    public IsActive getIsActive() {
        return this.isActive;
    }

    public void setIsActive(IsActive isActive) {
        this.isActive = isActive;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Boolean getBelongsToCurrentTenant() {
        return this.belongsToCurrentTenant;
    }

    public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
        this.belongsToCurrentTenant = belongsToCurrentTenant;
    }
}
