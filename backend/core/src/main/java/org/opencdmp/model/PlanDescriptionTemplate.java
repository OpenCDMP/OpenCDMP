package org.opencdmp.model;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.plan.Plan;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PlanDescriptionTemplate {

    private UUID id;

    public static final String _id = "id";

    private Plan plan;

    public static final String _plan = "plan";

    private DescriptionTemplate currentDescriptionTemplate;

    public static final String _currentDescriptionTemplate = "currentDescriptionTemplate";


    private List<DescriptionTemplate>  descriptionTemplates;

    public static final String _descriptionTemplates = "descriptionTemplates";

    private UUID sectionId;

    public static final String _sectionId = "sectionId";

    private UUID descriptionTemplateGroupId;

    public static final String _descriptionTemplateGroupId = "descriptionTemplateGroupId";

    private Instant createdAt;

    public static final String _createdAt = "createdAt";

    private Instant updatedAt;

    public static final String _updatedAt = "updatedAt";

    private IsActive isActive;

    public static final String _isActive = "isActive";

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

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public DescriptionTemplate getCurrentDescriptionTemplate() {
        return currentDescriptionTemplate;
    }

    public void setCurrentDescriptionTemplate(DescriptionTemplate currentDescriptionTemplate) {
        this.currentDescriptionTemplate = currentDescriptionTemplate;
    }

    public List<DescriptionTemplate> getDescriptionTemplates() {
        return descriptionTemplates;
    }

    public void setDescriptionTemplates(List<DescriptionTemplate> descriptionTemplates) {
        this.descriptionTemplates = descriptionTemplates;
    }

    public UUID getDescriptionTemplateGroupId() {
        return descriptionTemplateGroupId;
    }

    public void setDescriptionTemplateGroupId(UUID descriptionTemplateGroupId) {
        this.descriptionTemplateGroupId = descriptionTemplateGroupId;
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
