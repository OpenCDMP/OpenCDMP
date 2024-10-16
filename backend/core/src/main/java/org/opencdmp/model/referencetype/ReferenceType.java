package org.opencdmp.model.referencetype;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.model.referencetype.ReferenceTypeDefinition;

import java.time.Instant;
import java.util.UUID;

public class ReferenceType {

    private UUID id;
    public static final String _id = "id";

    private String name;
    public static final String _name = "name";

    private String code;
    public static final String _code = "code";

    private ReferenceTypeDefinition definition;
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
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ReferenceTypeDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(ReferenceTypeDefinition definition) {
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
