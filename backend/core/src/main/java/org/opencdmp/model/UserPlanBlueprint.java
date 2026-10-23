package org.opencdmp.model;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UserPlanBlueprintRole;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.user.User;

import java.time.Instant;
import java.util.UUID;

public class UserPlanBlueprint {

    public final static String _id = "id";
    private UUID id;

    public final static String _planBlueprint = "planBlueprint";
    private PlanBlueprint planBlueprint;
    
    public final static String _role = "role";
    private UserPlanBlueprintRole role;

    public final static String _user = "user";
    private User user;

    public final static String _createdAt = "createdAt";
    private Instant createdAt;

    public final static String _updatedAt = "updatedAt";
    private Instant updatedAt;

    public final static String _isActive = "isActive";
    private IsActive isActive;

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

    public PlanBlueprint getPlanBlueprint() {
        return planBlueprint;
    }

    public void setPlanBlueprint(PlanBlueprint planBlueprint) {
        this.planBlueprint = planBlueprint;
    }

    public UserPlanBlueprintRole getRole() {
        return role;
    }

    public void setRole(UserPlanBlueprintRole role) {
        this.role = role;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getBelongsToCurrentTenant() {
        return belongsToCurrentTenant;
    }

    public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
        this.belongsToCurrentTenant = belongsToCurrentTenant;
    }
}
