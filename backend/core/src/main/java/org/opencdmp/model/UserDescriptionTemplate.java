package org.opencdmp.model;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UserDescriptionTemplateRole;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.user.User;

import java.time.Instant;
import java.util.UUID;

public class UserDescriptionTemplate {

    public final static String _id = "id";
    private UUID id;

    public final static String _descriptionTemplate = "descriptionTemplate";
    private DescriptionTemplate descriptionTemplate;
    
    public final static String _role = "role";
    private UserDescriptionTemplateRole role;

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

    public DescriptionTemplate getDescriptionTemplate() {
        return descriptionTemplate;
    }

    public void setDescriptionTemplate(DescriptionTemplate descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }

    public UserDescriptionTemplateRole getRole() {
        return role;
    }

    public void setRole(UserDescriptionTemplateRole role) {
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
