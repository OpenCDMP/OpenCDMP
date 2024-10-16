package org.opencdmp.model;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.SupportiveMaterialFieldType;

import java.time.Instant;
import java.util.UUID;


public class SupportiveMaterial {

    private UUID id;
    public static final String _id = "id";

    private SupportiveMaterialFieldType type;
    public static final String _type = "type";

    private String languageCode;
    public static final String _languageCode = "languageCode";

    private String payload;
    public static final String _payload = "payload";

    private Instant createdAt;
    public static final String _createdAt = "createdAt";

    private Instant updatedAt;
    public static final String _updatedAt = "updatedAt";

    private IsActive isActive;
    public static final String _isActive = "isActive";

    private String hash;
    public final static String _hash = "hash";

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public SupportiveMaterialFieldType getType() {
        return type;
    }

    public void setType(SupportiveMaterialFieldType type) {
        this.type = type;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
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

    public IsActive getIsActive() {
        return isActive;
    }

    public void setIsActive(IsActive isActive) {
        this.isActive = isActive;
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
