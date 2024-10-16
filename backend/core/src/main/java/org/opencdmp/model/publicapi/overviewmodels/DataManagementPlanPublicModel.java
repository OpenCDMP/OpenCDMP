package org.opencdmp.model.publicapi.overviewmodels;

import org.opencdmp.data.PlanEntity;
import org.opencdmp.model.publicapi.associatedprofile.AssociatedProfilePublicModel;
import org.opencdmp.model.publicapi.doi.DoiPublicModel;
import org.opencdmp.model.publicapi.grant.GrantPublicOverviewModel;
import org.opencdmp.model.publicapi.organisation.OrganizationPublicModel;
import org.opencdmp.model.publicapi.researcher.ResearcherPublicModel;
import org.opencdmp.model.publicapi.user.UserInfoPublicModel;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DataManagementPlanPublicModel {
    private String id;
    private String label;
    private String profile;
    private GrantPublicOverviewModel grant;
    private Date createdAt;
    private Date modifiedAt;
    private Date finalizedAt;
    private List<OrganizationPublicModel> organisations;
    private int version;
    private UUID groupId;
    private List<DatasetPublicModel> datasets;
    private List<AssociatedProfilePublicModel> associatedProfiles;
    private List<ResearcherPublicModel> researchers;
    private List<UserInfoPublicModel> users;
    private String description;
    private Date publishedAt;
    private List<DoiPublicModel> dois;


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public String getProfile() {
        return profile;
    }
    public void setProfile(String profile) {
        this.profile = profile;
    }

    public GrantPublicOverviewModel getGrant() {
        return grant;
    }
    public void setGrant(GrantPublicOverviewModel grant) {
        this.grant = grant;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Date getFinalizedAt() {
        return finalizedAt;
    }
    public void setFinalizedAt(Date finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public List<OrganizationPublicModel> getOrganisations() {
        return organisations;
    }
    public void setOrganisations(List<OrganizationPublicModel> organizations) {
        this.organisations = organizations;
    }

    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }

    public UUID getGroupId() {
        return groupId;
    }
    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public List<DatasetPublicModel> getDatasets() {
        return datasets;
    }
    public void setDatasets(List<DatasetPublicModel> datasets) {
        this.datasets = datasets;
    }

    public List<AssociatedProfilePublicModel> getAssociatedProfiles() {
        return associatedProfiles;
    }
    public void setAssociatedProfiles(List<AssociatedProfilePublicModel> associatedProfiles) {
        this.associatedProfiles = associatedProfiles;
    }

    public List<UserInfoPublicModel> getUsers() {
        return users;
    }
    public void setUsers(List<UserInfoPublicModel> users) {
        this.users = users;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public List<ResearcherPublicModel> getResearchers() {
        return researchers;
    }
    public void setResearchers(List<ResearcherPublicModel> researchers) {
        this.researchers = researchers;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }
    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<DoiPublicModel> getDois() {
        return dois;
    }
    public void setDois(List<DoiPublicModel> dois) {
        this.dois = dois;
    }

    public PlanEntity toDataModel() {
        return null;
    }

    public String getHint() {
        return "dataManagementPlanOverviewModel";
    }
}
