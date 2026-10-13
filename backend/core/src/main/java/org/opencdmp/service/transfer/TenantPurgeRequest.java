package org.opencdmp.service.transfer;

public class TenantPurgeRequest {

    private Boolean deletePlans;

    private Boolean deleteReferences;

    private Boolean deleteTags;

    private Boolean deleteDescriptionTemplates;

    private Boolean deletePlanBlueprints;

    private Boolean deleteReferenceTypes;

    private Boolean deletePrefillingSources;

    private Boolean deleteStatusesAndWorkflows;

    private Boolean deleteBlueprintTypes;

    private Boolean deleteTemplateTypes;

    private Boolean deleteStorageFiles;

    public Boolean getDeletePlans() {
        return this.deletePlans;
    }

    public void setDeletePlans(Boolean deletePlans) {
        this.deletePlans = deletePlans;
    }

    public Boolean getDeleteReferences() {
        return this.deleteReferences;
    }

    public void setDeleteReferences(Boolean deleteReferences) {
        this.deleteReferences = deleteReferences;
    }

    public Boolean getDeleteTags() {
        return this.deleteTags;
    }

    public void setDeleteTags(Boolean deleteTags) {
        this.deleteTags = deleteTags;
    }

    public Boolean getDeleteDescriptionTemplates() {
        return this.deleteDescriptionTemplates;
    }

    public void setDeleteDescriptionTemplates(Boolean deleteDescriptionTemplates) {
        this.deleteDescriptionTemplates = deleteDescriptionTemplates;
    }

    public Boolean getDeletePlanBlueprints() {
        return this.deletePlanBlueprints;
    }

    public void setDeletePlanBlueprints(Boolean deletePlanBlueprints) {
        this.deletePlanBlueprints = deletePlanBlueprints;
    }

    public Boolean getDeleteReferenceTypes() {
        return this.deleteReferenceTypes;
    }

    public void setDeleteReferenceTypes(Boolean deleteReferenceTypes) {
        this.deleteReferenceTypes = deleteReferenceTypes;
    }

    public Boolean getDeletePrefillingSources() {
        return this.deletePrefillingSources;
    }

    public void setDeletePrefillingSources(Boolean deletePrefillingSources) {
        this.deletePrefillingSources = deletePrefillingSources;
    }

    public Boolean getDeleteStatusesAndWorkflows() {
        return this.deleteStatusesAndWorkflows;
    }

    public void setDeleteStatusesAndWorkflows(Boolean deleteStatusesAndWorkflows) {
        this.deleteStatusesAndWorkflows = deleteStatusesAndWorkflows;
    }

    public Boolean getDeleteBlueprintTypes() {
        return this.deleteBlueprintTypes;
    }

    public void setDeleteBlueprintTypes(Boolean deleteBlueprintTypes) {
        this.deleteBlueprintTypes = deleteBlueprintTypes;
    }

    public Boolean getDeleteTemplateTypes() {
        return this.deleteTemplateTypes;
    }

    public void setDeleteTemplateTypes(Boolean deleteTemplateTypes) {
        this.deleteTemplateTypes = deleteTemplateTypes;
    }

    public Boolean getDeleteStorageFiles() {
        return this.deleteStorageFiles;
    }

    public void setDeleteStorageFiles(Boolean deleteStorageFiles) {
        this.deleteStorageFiles = deleteStorageFiles;
    }
}
