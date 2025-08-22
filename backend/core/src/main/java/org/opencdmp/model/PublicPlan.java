package org.opencdmp.model;

import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.model.plan.PlanProperties;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PublicPlan {

    private UUID id;

    public static final String _id = "id";

    private String label;

    public static final String _label = "label";

    private Short version;

    public static final String _version = "version";

    private String description;

    public static final String _description = "description";

    private Instant updatedAt;
    public static final String _updatedAt = "updatedAt";

    private Instant finalizedAt;

    public static final String _finalizedAt = "finalizedAt";

    private Instant publishedAt;

    public static final String _publishedAt = "publishedAt";

    private PublicPlanStatus status;
    public static final String _status = "status";

    private UUID groupId;
    public static final String _groupId = "groupId";

    private PublicPlanBlueprint blueprint;
    public static final String _blueprint = "blueprint";

    private PublicPlanProperties properties;
    public static final String _properties = "properties";

    private String language;
    public static final String _language = "language";

    private PlanAccessType accessType;
    public static final String _accessType = "accessType";

    private List<PublicPlanUser> planUsers;

    public static final String _planUsers = "planUsers";

    private List<PublicPlanReference> planReferences;

    public static final String _planReferences = "planReferences";

    private List<PublicDescription> descriptions;
    public static final String _descriptions = "descriptions";

    private List<PublicEntityDoi> entityDois;
    public static final String _entityDois = "entityDois";

    private List<PublicPlan> otherPlanVersions;
    public static final String _otherPlanVersions = "otherPlanVersions";

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

    public Short getVersion() {
        return version;
    }

    public void setVersion(Short version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
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

    public PublicPlanStatus getStatus() {
        return status;
    }

    public void setStatus(PublicPlanStatus status) {
        this.status = status;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public PublicPlanBlueprint getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(PublicPlanBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    public PublicPlanProperties getProperties() {
        return properties;
    }

    public void setProperties(PublicPlanProperties properties) {
        this.properties = properties;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public PlanAccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(PlanAccessType accessType) {
        this.accessType = accessType;
    }

    public List<PublicPlanUser> getPlanUsers() {
        return planUsers;
    }

    public void setPlanUsers(List<PublicPlanUser> planUsers) {
        this.planUsers = planUsers;
    }

    public List<PublicPlanReference> getPlanReferences() {
        return planReferences;
    }

    public void setPlanReferences(List<PublicPlanReference> planReferences) {
        this.planReferences = planReferences;
    }

    public List<PublicDescription> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<PublicDescription> descriptions) {
        this.descriptions = descriptions;
    }

    public List<PublicEntityDoi> getEntityDois() {
        return entityDois;
    }

    public void setEntityDois(List<PublicEntityDoi> entityDois) {
        this.entityDois = entityDois;
    }

    public List<PublicPlan> getOtherPlanVersions() {
        return otherPlanVersions;
    }

    public void setOtherPlanVersions(List<PublicPlan> otherPlanVersions) {
        this.otherPlanVersions = otherPlanVersions;
    }
}
