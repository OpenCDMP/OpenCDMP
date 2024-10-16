package org.opencdmp.model.publicapi.listingmodels;

import org.opencdmp.model.publicapi.researcher.ResearcherPublicModel;
import org.opencdmp.model.publicapi.user.UserInfoPublicModel;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DataManagementPlanPublicListingModel {
    private String id;
    private String label;
    private String grant;
    private Date createdAt;
    private Date modifiedAt;
    private int version;
    private UUID groupId;
    private List<UserInfoPublicModel> users;
    private List<ResearcherPublicModel> researchers;
    private Date finalizedAt;
    private Date publishedAt;

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

    public String getGrant() {
        return grant;
    }
    public void setGrant(String grant) {
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

    public List<UserInfoPublicModel> getUsers() {
        return users;
    }
    public void setUsers(List<UserInfoPublicModel> users) {
        this.users = users;
    }

    public List<ResearcherPublicModel> getResearchers() {
        return researchers;
    }
    public void setResearchers(List<ResearcherPublicModel> researchers) {
        this.researchers = researchers;
    }

    public Date getFinalizedAt() {
        return finalizedAt;
    }
    public void setFinalizedAt(Date finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }
    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }
}
