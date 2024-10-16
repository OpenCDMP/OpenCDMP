package org.opencdmp.controllers.publicapi.criteria.plan;

import org.opencdmp.controllers.publicapi.criteria.Criteria;
import org.opencdmp.data.PlanEntity;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DataManagementPlanPublicCriteria extends Criteria<PlanEntity> {
    @ApiModelProperty(value = "periodStart", name = "periodStart", dataType = "Date", example = "2022-01-01T13:19:42.210Z")
    private Date periodStart;
    @ApiModelProperty(value = "periodEnd", name = "periodEnd", dataType = "Date", example = "2022-12-31T13:19:42.210Z")
    private Date periodEnd;
    @ApiModelProperty(value = "grants", name = "grants", dataType = "List<UUID>", example = "[]")
    private List<UUID> grants;
    @ApiModelProperty(value = "grantsLike", name = "grantsLike", dataType = "List<String>", example = "[]")
    private List<String> grantsLike;
    @ApiModelProperty(value = "funders", name = "funders", dataType = "List<UUID>", example = "[]")
    private List<UUID> funders;
    @ApiModelProperty(value = "fundersLike", name = "fundersLike", dataType = "List<String>", example = "[]")
    private List<String> fundersLike;
    @ApiModelProperty(value = "datasetTemplates", name = "datasetTemplates", dataType = "List<UUID>", example = "[]")
    private List<UUID> datasetTemplates;
    @ApiModelProperty(value = "dmpOrganisations", name = "dmpOrganisations", dataType = "List<String>", example = "[]")
    private List<String> dmpOrganisations;
    @ApiModelProperty(value = "collaborators", name = "collaborators", dataType = "List<UUID>", example = "[]")
    private List<UUID> collaborators;
    @ApiModelProperty(value = "collaboratorsLike", name = "collaboratorsLike", dataType = "List<String>", example = "[]")
    private List<String> collaboratorsLike;
    @ApiModelProperty(value = "allVersions", name = "allVersions", dataType = "Boolean", example = "false")
    private boolean allVersions;
    @ApiModelProperty(value = "groupIds", name = "groupIds", dataType = "List<UUID>", example = "[]")
    private List<UUID> groupIds;

    public Date getPeriodStart() {
        return periodStart;
    }
    public void setPeriodStart(Date periodStart) {
        this.periodStart = periodStart;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }
    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    public List<UUID> getGrants() {
        return grants;
    }
    public void setGrants(List<UUID> grants) {
        this.grants = grants;
    }

    public List<String> getGrantsLike() {
        return grantsLike;
    }
    public void setGrantsLike(List<String> grantsLike) {
        this.grantsLike = grantsLike;
    }

    public List<UUID> getFunders() {
        return funders;
    }
    public void setFunders(List<UUID> funders) {
        this.funders = funders;
    }

    public List<String> getFundersLike() {
        return fundersLike;
    }
    public void setFundersLike(List<String> fundersLike) {
        this.fundersLike = fundersLike;
    }

    public List<UUID> getDatasetTemplates() {
        return datasetTemplates;
    }
    public void setDatasetTemplates(List<UUID> datasetTemplates) {
        this.datasetTemplates = datasetTemplates;
    }

    public List<String> getDmpOrganisations() {
        return dmpOrganisations;
    }
    public void setDmpOrganisations(List<String> dmpOrganisations) {
        this.dmpOrganisations = dmpOrganisations;
    }

    public List<UUID> getCollaborators() {
        return collaborators;
    }
    public void setCollaborators(List<UUID> collaborators) {
        this.collaborators = collaborators;
    }

    public List<String> getCollaboratorsLike() {
        return collaboratorsLike;
    }
    public void setCollaboratorsLike(List<String> collaboratorsLike) {
        this.collaboratorsLike = collaboratorsLike;
    }

    public boolean getAllVersions() {
        return allVersions;
    }
    public void setAllVersions(boolean allVersions) {
        this.allVersions = allVersions;
    }

    public List<UUID> getGroupIds() {
        return groupIds;
    }
    public void setGroupIds(List<UUID> groupIds) {
        this.groupIds = groupIds;
    }
}
