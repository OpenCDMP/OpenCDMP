package org.opencdmp.model.plan;

import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.enums.PlanVersionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.model.*;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.model.user.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Plan {

    private UUID id;
    public static final String _id = "id";

    private UUID tenantId;
    public static final String _tenantId = "tenantId";

    private String label;
    public static final String _label = "label";

    private Short version;
    public static final String _version = "version";

    private PlanStatus status;
    public static final String _status = "status";
    
    private PlanVersionStatus versionStatus;
    public static final String _versionStatus = "versionStatus";

    private PlanProperties properties;
    public static final String _properties = "properties";

    private UUID groupId;
    public static final String _groupId = "groupId";

    private String description;
    public static final String _description = "description";

    private Instant createdAt;
    public static final String _createdAt = "createdAt";

    private Instant updatedAt;
    public static final String _updatedAt = "updatedAt";

    private IsActive isActive;
    public static final String _isActive = "isActive";

    private Instant finalizedAt;
    public static final String _finalizedAt = "finalizedAt";

    private Instant publishedAt;
    public static final String _publishedAt = "publishedAt";

    private User creator;
    public static final String _creator = "creator";

    private PlanAccessType accessType;
    public static final String _accessType = "accessType";

    private PlanBlueprint blueprint;
    public static final String _blueprint = "blueprint";

    private String language;
    public static final String _language = "language";

    private Instant publicAfter;
    public static final String _publicAfter = "publicAfter";

    private String hash;
    public static final String _hash = "hash";

    private List<PlanReference> planReferences;
    public static final String _planReferences = "planReferences";

    private List<PlanUser> planUsers;
    public static final String _planUsers = "planUsers";

    private List<Description> descriptions;
    public static final String _descriptions = "descriptions";

    private List<PlanDescriptionTemplate> planDescriptionTemplates;
    public static final String _planDescriptionTemplates = "planDescriptionTemplates";

    private List<EntityDoi> entityDois;
    public static final String _entityDois = "entityDois";

    private List<String> authorizationFlags;
    public static final String _authorizationFlags = "authorizationFlags";

    private List<Plan> otherPlanVersions;
    public static final String _otherPlanVersions = "otherPlanVersions";


    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }

    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Short getVersion() {
        return version;
    }

    public void setVersion(Short version) {
        this.version = version;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public PlanProperties getProperties() {
        return properties;
    }

    public void setProperties(PlanProperties properties) {
        this.properties = properties;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Instant getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(Instant finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public PlanAccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(PlanAccessType accessType) {
        this.accessType = accessType;
    }

    public PlanBlueprint getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(PlanBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Instant getPublicAfter() {
        return publicAfter;
    }

    public void setPublicAfter(Instant publicAfter) {
        this.publicAfter = publicAfter;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<PlanReference> getPlanReferences() {
        return planReferences;
    }

    public void setPlanReferences(List<PlanReference> planReferences) {
        this.planReferences = planReferences;
    }

    public List<PlanUser> getPlanUsers() {
        return planUsers;
    }

    public void setPlanUsers(List<PlanUser> planUsers) {
        this.planUsers = planUsers;
    }

    public PlanVersionStatus getVersionStatus() {
        return versionStatus;
    }

    public void setVersionStatus(PlanVersionStatus versionStatus) {
        this.versionStatus = versionStatus;
    }

    public List<Description> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<Description> descriptions) {
        this.descriptions = descriptions;
    }

    public List<PlanDescriptionTemplate> getPlanDescriptionTemplates() {
        return planDescriptionTemplates;
    }

    public void setPlanDescriptionTemplates(List<PlanDescriptionTemplate> planDescriptionTemplates) {
        this.planDescriptionTemplates = planDescriptionTemplates;
    }

    public List<EntityDoi> getEntityDois() {
        return entityDois;
    }

    public void setEntityDois(List<EntityDoi> entityDois) {
        this.entityDois = entityDois;
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

    public List<Plan> getOtherPlanVersions() {
        return otherPlanVersions;
    }

    public void setOtherPlanVersions(List<Plan> otherPlanVersions) {
        this.otherPlanVersions = otherPlanVersions;
    }
}
