package org.opencdmp.model;

import org.opencdmp.commons.enums.IsActive;

import java.util.UUID;

public class PublicPlanReference {

    private UUID id;
    public static final String _id = "id";

    private PublicPlan plan;
    public static final String _plan = "plan";

    private PublicReference reference;
    public static final String _reference = "reference";

    private IsActive isActive;
    public static final String _isActive = "isActive";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PublicPlan getPlan() {
        return plan;
    }

    public void setPlan(PublicPlan plan) {
        this.plan = plan;
    }

    public PublicReference getReference() {
        return reference;
    }

    public void setReference(PublicReference reference) {
        this.reference = reference;
    }

    public IsActive getIsActive() {
        return isActive;
    }

    public void setIsActive(IsActive isActive) {
        this.isActive = isActive;
    }
}
