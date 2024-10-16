package org.opencdmp.model.planstatus;

import org.opencdmp.commons.enums.IsActive;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PlanStatus {

    public final static String _id = "id";
    private UUID id;

    public final static String _name = "name";
    private String name;

    public final static String _description = "description";
    private String description;

    public final static String _createdAt = "createdAt";
    private Instant createdAt;

    public final static String _updatedAt = "updatedAt";
    private Instant updatedAt;

    public final static String _isActive = "isActive";
    private IsActive isActive;

    public final static String _internalStatus = "internalStatus";
    private org.opencdmp.commons.enums.PlanStatus internalStatus;

    public final static String _definition = "definition";
    private PlanStatusDefinition definition;

    public final static String _hash = "hash";
    private String hash;

    public static final String _authorizationFlags = "authorizationFlags";
    private List<String> authorizationFlags;

    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";
    private Boolean belongsToCurrentTenant;


    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() { return this.description; }

    public void setDescription(String description) { this.description = description; }

    public PlanStatusDefinition getDefinition() {
        return this.definition;
    }

    public void setDefinition(PlanStatusDefinition definition) {
        this.definition = definition;
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

    public IsActive getIsActive() {
        return this.isActive;
    }

    public void setIsActive(IsActive isActive) {
        this.isActive = isActive;
    }

    public org.opencdmp.commons.enums.PlanStatus getInternalStatus() {
        return this.internalStatus;
    }
    public void setInternalStatus(org.opencdmp.commons.enums.PlanStatus internalStatus) { this.internalStatus = internalStatus; }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<String> getAuthorizationFlags() {
        return this.authorizationFlags;
    }

    public void setAuthorizationFlags(List<String> authorizationFlags) {
        this.authorizationFlags = authorizationFlags;
    }

    public Boolean getBelongsToCurrentTenant() {
        return this.belongsToCurrentTenant;
    }

    public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) { this.belongsToCurrentTenant = belongsToCurrentTenant; }
}
