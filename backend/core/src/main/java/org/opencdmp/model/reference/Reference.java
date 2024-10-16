package org.opencdmp.model.reference;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.ReferenceSourceType;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.model.user.User;
import org.opencdmp.model.referencetype.ReferenceType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Reference {

    private UUID id;
    public static final String _id = "id";

    private String label;
    public static final String _label = "label";

    private ReferenceType type;
    public static final String _type = "type";

    private String description;
    public static final String _description = "description";

    private Definition definition;
    public static final String _definition = "definition";

    private String reference;
    public static final String _reference = "reference";

    private String abbreviation;
    public static final String _abbreviation = "abbreviation";

    private String source;
    public static final String _source = "source";

    private ReferenceSourceType sourceType;
    public static final String _sourceType = "sourceType";

    private IsActive isActive;
    public static final String _isActive = "isActive";

    private Instant createdAt;
    public static final String _createdAt = "createdAt";

    private Instant updatedAt;
    public static final String _updatedAt = "updatedAt";

    private User createdBy;
    public static final String _createdBy = "createdBy";

    private List<PlanReference> planReferences;
    public static final String _planReferences = "planReferences";

    private String hash;
    public final static String _hash = "hash";

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ReferenceType getType() {
        return type;
    }

    public void setType(ReferenceType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ReferenceSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ReferenceSourceType sourceType) {
        this.sourceType = sourceType;
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

    public List<PlanReference> getPlanReferences() {
        return planReferences;
    }

    public void setPlanReferences(List<PlanReference> planReferences) {
        this.planReferences = planReferences;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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
