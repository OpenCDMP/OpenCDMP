package org.opencdmp.model.planreference;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.reference.Reference;

import java.time.Instant;
import java.util.UUID;

public class PlanReference {

    private UUID id;
    public static final String _id = "id";

    private Plan plan;
    public static final String _plan = "plan";

    private Reference reference;
    public static final String _reference = "reference";

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

    private PlanReferenceData data;
    public static final String _data = "data";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public IsActive getIsActive() {
        return isActive;
    }

    public void setIsActive(IsActive isActive) {
        this.isActive = isActive;
    }

    public Instant getCreatedAt() { return createdAt;}

    public void setCreatedAt(Instant createdAt) {this.createdAt = createdAt;}

    public Instant getUpdatedAt() {return updatedAt;}

    public void setUpdatedAt(Instant updatedAt) {this.updatedAt = updatedAt;}

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setData(PlanReferenceData data) {
        this.data = data;
    }

    public PlanReferenceData getData() {
        return data;
    }

    public Boolean getBelongsToCurrentTenant() {
        return belongsToCurrentTenant;
    }

    public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
        this.belongsToCurrentTenant = belongsToCurrentTenant;
    }
}
