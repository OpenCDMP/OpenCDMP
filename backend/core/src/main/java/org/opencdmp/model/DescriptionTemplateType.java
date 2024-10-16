package org.opencdmp.model;

import org.opencdmp.commons.enums.DescriptionTemplateTypeStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.model.descriptiontemplate.Definition;

import java.time.Instant;
import java.util.UUID;

public class DescriptionTemplateType {

    public final static String _id = "id";
    private UUID id;

    public final static String _code = "code";
    private String code;

    public final static String _name = "name";
    private String name;

    public final static String _createdAt = "createdAt";
    private Instant createdAt;

    public final static String _updatedAt = "updatedAt";
    private Instant updatedAt;

    public final static String _isActive = "isActive";
    private IsActive isActive;

    public final static String _status = "status";
    private DescriptionTemplateTypeStatus status;

    public final static String _definition = "definition";
    private Definition definition;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public DescriptionTemplateTypeStatus getStatus() {
        return status;
    }

    public void setStatus(DescriptionTemplateTypeStatus status) {
        this.status = status;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public Boolean getBelongsToCurrentTenant() {
        return belongsToCurrentTenant;
    }

    public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
        this.belongsToCurrentTenant = belongsToCurrentTenant;
    }
}
