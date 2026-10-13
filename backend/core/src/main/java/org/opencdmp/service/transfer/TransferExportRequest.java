package org.opencdmp.service.transfer;

import java.util.List;
import java.util.UUID;

public class TransferExportRequest {

    private Boolean includeAllReferenceTypes;

    private Boolean includeAllPrefillingSources;

    private Boolean includeAllDescriptionTemplateTypes;

    private Boolean includeAllPlanBlueprintTypes;

    private Boolean includeAllStatusesAndWorkflows;

    private Boolean includeAllLanguages;

    private Boolean includeSupportiveMaterial;

    private Boolean includeAllDescriptionTemplates;

    private List<UUID> descriptionTemplateIds;

    private Boolean includeAllPlanBlueprints;

    private List<UUID> planBlueprintIds;

    private Boolean includeAllPlans;

    private List<UUID> planIds;

    public Boolean getIncludeAllReferenceTypes() {
        return this.includeAllReferenceTypes;
    }

    public void setIncludeAllReferenceTypes(Boolean includeAllReferenceTypes) {
        this.includeAllReferenceTypes = includeAllReferenceTypes;
    }

    public Boolean getIncludeAllPrefillingSources() {
        return this.includeAllPrefillingSources;
    }

    public void setIncludeAllPrefillingSources(Boolean includeAllPrefillingSources) {
        this.includeAllPrefillingSources = includeAllPrefillingSources;
    }

    public Boolean getIncludeAllDescriptionTemplateTypes() {
        return this.includeAllDescriptionTemplateTypes;
    }

    public void setIncludeAllDescriptionTemplateTypes(Boolean includeAllDescriptionTemplateTypes) {
        this.includeAllDescriptionTemplateTypes = includeAllDescriptionTemplateTypes;
    }

    public Boolean getIncludeAllPlanBlueprintTypes() {
        return this.includeAllPlanBlueprintTypes;
    }

    public void setIncludeAllPlanBlueprintTypes(Boolean includeAllPlanBlueprintTypes) {
        this.includeAllPlanBlueprintTypes = includeAllPlanBlueprintTypes;
    }

    public Boolean getIncludeAllStatusesAndWorkflows() {
        return this.includeAllStatusesAndWorkflows;
    }

    public void setIncludeAllStatusesAndWorkflows(Boolean includeAllStatusesAndWorkflows) {
        this.includeAllStatusesAndWorkflows = includeAllStatusesAndWorkflows;
    }

    public Boolean getIncludeAllLanguages() {
        return this.includeAllLanguages;
    }

    public void setIncludeAllLanguages(Boolean includeAllLanguages) {
        this.includeAllLanguages = includeAllLanguages;
    }

    public Boolean getIncludeSupportiveMaterial() {
        return this.includeSupportiveMaterial;
    }

    public void setIncludeSupportiveMaterial(Boolean includeSupportiveMaterial) {
        this.includeSupportiveMaterial = includeSupportiveMaterial;
    }

    public Boolean getIncludeAllDescriptionTemplates() {
        return this.includeAllDescriptionTemplates;
    }

    public void setIncludeAllDescriptionTemplates(Boolean includeAllDescriptionTemplates) {
        this.includeAllDescriptionTemplates = includeAllDescriptionTemplates;
    }

    public List<UUID> getDescriptionTemplateIds() {
        return this.descriptionTemplateIds;
    }

    public void setDescriptionTemplateIds(List<UUID> descriptionTemplateIds) {
        this.descriptionTemplateIds = descriptionTemplateIds;
    }

    public Boolean getIncludeAllPlanBlueprints() {
        return this.includeAllPlanBlueprints;
    }

    public void setIncludeAllPlanBlueprints(Boolean includeAllPlanBlueprints) {
        this.includeAllPlanBlueprints = includeAllPlanBlueprints;
    }

    public List<UUID> getPlanBlueprintIds() {
        return this.planBlueprintIds;
    }

    public void setPlanBlueprintIds(List<UUID> planBlueprintIds) {
        this.planBlueprintIds = planBlueprintIds;
    }

    public Boolean getIncludeAllPlans() {
        return this.includeAllPlans;
    }

    public void setIncludeAllPlans(Boolean includeAllPlans) {
        this.includeAllPlans = includeAllPlans;
    }

    public List<UUID> getPlanIds() {
        return this.planIds;
    }

    public void setPlanIds(List<UUID> planIds) {
        this.planIds = planIds;
    }
}
