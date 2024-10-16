package org.opencdmp.controllers.publicapi.criteria.dataset;

import org.opencdmp.controllers.publicapi.criteria.Criteria;
import org.opencdmp.data.DescriptionEntity;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;
import java.util.UUID;


public class DatasetPublicCriteria extends Criteria<DescriptionEntity> {
    @ApiModelProperty(value = "periodStart", name = "periodStart", dataType = "Date", example = "2022-01-01T13:19:42.210Z")
    private Date periodStart;
    @ApiModelProperty(value = "periodEnd", name = "periodEnd", dataType = "Date", example = "2022-12-31T13:19:42.210Z")
    private Date periodEnd;
    @ApiModelProperty(value = "grants", name = "grants", dataType = "List<UUID>", example = "[]")
    private List<UUID> grants;
    @ApiModelProperty(value = "collaborators", name = "collaborators", dataType = "List<UUID>", example = "[]")
    private List<UUID> collaborators;
    @ApiModelProperty(value = "datasetTemplates", name = "datasetTemplates", dataType = "List<UUID>", example = "[]")
    private List<UUID> datasetTemplates;
    @ApiModelProperty(value = "dmpOrganisations", name = "dmpOrganisations", dataType = "List<String>", example = "[]")
    private List<String> dmpOrganisations;
//    @ApiModelProperty(value = "tags", name = "tags", dataType = "List<Tag>", example = "[]")
//    private List<Tag> tags; //TODO
    @ApiModelProperty(value = "dmpIds", name = "dmpIds", dataType = "List<UUID>", example = "[]")
    private List<UUID> dmpIds;
    @ApiModelProperty(value = "groupIds", name = "groupIds", dataType = "List<UUID>", example = "[]")
    private List<UUID> groupIds;
    @ApiModelProperty(value = "allVersions", name = "allVersions", dataType = "Boolean", example = "false")
    private boolean allVersions;

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

    public List<UUID> getCollaborators() {
        return collaborators;
    }
    public void setCollaborators(List<UUID> collaborators) {
        this.collaborators = collaborators;
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

//    public List<Tag> getTags() {
//        return tags;
//    }
//    public void setTags(List<Tag> tags) {
//        this.tags = tags;
//    }

    public List<UUID> getDmpIds() {
        return dmpIds;
    }
    public void setDmpIds(List<UUID> dmpIds) {
        this.dmpIds = dmpIds;
    }

    public List<UUID> getGroupIds() {
        return groupIds;
    }
    public void setGroupIds(List<UUID> groupIds) {
        this.groupIds = groupIds;
    }

    public boolean getAllVersions() {
        return allVersions;
    }
    public void setAllVersions(boolean allVersions) {
        this.allVersions = allVersions;
    }
}
