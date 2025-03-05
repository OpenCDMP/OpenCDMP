package org.opencdmp.model.descriptionstatus;

import org.opencdmp.commons.enums.IsActive;

import java.time.Instant;
import java.util.UUID;

public class DescriptionStatus {

    public final static String _id = "id";
    private UUID id;

    public final static String _name = "name";
    private String name;

    public final static String _description = "description";
    private String description;

    public static final String _action = "action";
    private String action;

    public static final String _ordinal = "ordinal";
    private Integer ordinal;

    public final static String _createdAt = "createdAt";
    private Instant createdAt;

    public final static String _updatedAt = "updatedAt";
    private Instant updatedAt;

    public final static String _isActive = "isActive";
    private IsActive isActive;

    public final static String _hash = "hash";
    private String hash;

    public final static String _belongsToCurrentTenant = "belongsToCurrentTenant";
    private Boolean belongsToCurrentTenant;

    public final static String _internalStatus = "internalStatus";
    private org.opencdmp.commons.enums.DescriptionStatus internalStatus;

    public final static String _definition = "definition";
    private DescriptionStatusDefinition definition;

    public UUID getId() { return this.id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public Instant getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return this.updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public IsActive getIsActive() { return this.isActive; }
    public void setIsActive(IsActive isActive) { this.isActive = isActive; }

    public String getHash() { return this.hash; }
    public void setHash(String hash) { this.hash = hash; }

    public org.opencdmp.commons.enums.DescriptionStatus getInternalStatus() { return this.internalStatus; }
    public void setInternalStatus(org.opencdmp.commons.enums.DescriptionStatus internalStatus) { this.internalStatus = internalStatus; }

    public DescriptionStatusDefinition getDefinition() { return this.definition; }
    public void setDefinition(DescriptionStatusDefinition definition) { this.definition = definition; }

    public Boolean getBelongsToCurrentTenant() { return this.belongsToCurrentTenant; }

    public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) { this.belongsToCurrentTenant = belongsToCurrentTenant; }
}
