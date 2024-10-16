package org.opencdmp.model.descriptionreference;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.description.Description;

import java.time.Instant;
import java.util.UUID;

public class DescriptionReference {

    private UUID id;

    public static final String _id = "id";

    private Description description;

    public static final String _description = "description";

    private Reference reference;

    public static final String _reference = "reference";

    private Instant createdAt;

    public static final String _createdAt = "createdAt";

    private Instant updatedAt;

    public static final String _updatedAt = "updatedAt";

    private IsActive isActive;

    public static final String _isActive = "isActive";

    private String hash;

    public static final String _hash = "hash";

    private DescriptionReferenceData data;
    public static final String _data = "data";

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
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

    public DescriptionReferenceData getData() {
        return data;
    }

    public void setData(DescriptionReferenceData data) {
        this.data = data;
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
