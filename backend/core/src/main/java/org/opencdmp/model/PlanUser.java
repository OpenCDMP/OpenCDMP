package org.opencdmp.model;

import org.opencdmp.commons.enums.PlanUserRole;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.user.User;

import java.time.Instant;
import java.util.UUID;

public class PlanUser {

    private UUID id;

    public static final String _id = "id";

    private Plan plan;

    public static final String _plan = "plan";

    private User user;

    public static final String _user = "user";

    private String hash;
    public final static String _hash = "hash";

    private PlanUserRole role;

    public static final String _role = "role";

    private UUID sectionId;
    public static final String _sectionId = "sectionId";

    private Instant createdAt;

    public static final String _createdAt = "createdAt";

    private Instant updatedAt;

    public static final String _updatedAt = "updatedAt";

    private IsActive isActive;
    public static final String _isActive = "isActive";

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PlanUserRole getRole() {
        return role;
    }

    public void setRole(PlanUserRole role) {
        this.role = role;
    }

    public UUID getSectionId() {
        return sectionId;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
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
