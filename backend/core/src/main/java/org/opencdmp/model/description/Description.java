package org.opencdmp.model.description;

import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.model.*;
import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.user.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Description {

    private UUID id;

    public static final String _id = "id";

    private UUID tenantId;

    public static final String _tenantId = "tenantId";

    private String label;

    public static final String _label = "label";

    private PropertyDefinition properties;

    public static final String _properties = "properties";


    private DescriptionStatus status;

    public static final String _status = "status";

    private String description;

    public static final String _description = "description";

    private User createdBy;

    public static final String _createdBy = "createdBy";

    private Instant createdAt;

    public static final String _createdAt = "createdAt";

    private Instant updatedAt;

    public static final String _updatedAt = "updatedAt";

    private IsActive isActive;

    public static final String _isActive = "isActive";

    private Instant finalizedAt;

    public static final String _finalizedAt = "finalizedAt";

    private String hash;

    public static final String _hash = "hash";

    private List<DescriptionReference> descriptionReferences;

    public static final String _descriptionReferences = "descriptionReferences";

    private List<DescriptionTag> descriptionTags;

    public static final String _descriptionTags = "descriptionTags";

    private PlanDescriptionTemplate planDescriptionTemplate;

    public static final String _planDescriptionTemplate = "planDescriptionTemplate";

    private DescriptionTemplate descriptionTemplate;

    public static final String _descriptionTemplate = "descriptionTemplate";

    private List<String> authorizationFlags;
    public static final String _authorizationFlags = "authorizationFlags";

    private Plan plan;

    public static final String _plan = "plan";

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }


    public UUID getTenantId() { return tenantId; }

    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


    public PropertyDefinition getProperties() {
        return properties;
    }

    public void setProperties(PropertyDefinition properties) {
        this.properties = properties;
    }

    public DescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(DescriptionStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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

    public Instant getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(Instant finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<DescriptionReference> getDescriptionReferences() {
        return descriptionReferences;
    }

    public void setDescriptionReferences(List<DescriptionReference> descriptionReferences) {
        this.descriptionReferences = descriptionReferences;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public PlanDescriptionTemplate getPlanDescriptionTemplate() {
        return planDescriptionTemplate;
    }

    public void setPlanDescriptionTemplate(PlanDescriptionTemplate planDescriptionTemplate) {
        this.planDescriptionTemplate = planDescriptionTemplate;
    }

    public List<DescriptionTag> getDescriptionTags() {
        return descriptionTags;
    }

    public void setDescriptionTags(List<DescriptionTag> descriptionTags) {
        this.descriptionTags = descriptionTags;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public DescriptionTemplate getDescriptionTemplate() {
        return descriptionTemplate;
    }

    public void setDescriptionTemplate(DescriptionTemplate descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }

    public List<String> getAuthorizationFlags() {
        return authorizationFlags;
    }

    public void setAuthorizationFlags(List<String> authorizationFlags) {
        this.authorizationFlags = authorizationFlags;
    }

    public Boolean getBelongsToCurrentTenant() {
        return belongsToCurrentTenant;
    }

    public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
        this.belongsToCurrentTenant = belongsToCurrentTenant;
    }
}
