package org.opencdmp.model.publicapi.listingmodels;

import org.opencdmp.model.publicapi.datasetprofile.DatasetProfilePublicModel;
import org.opencdmp.model.publicapi.grant.GrantPublicOverviewModel;
import org.opencdmp.model.publicapi.user.UserInfoPublicModel;

import java.util.Date;
import java.util.List;

public class DatasetPublicListingModel {
    private String id;
    private String label;
    private GrantPublicOverviewModel grant;
    private String dmp;
    private String dmpId;
    private DatasetProfilePublicModel profile;
    private Date createdAt;
    private Date modifiedAt;
    private String description;
    private Date finalizedAt;
    private Date dmpPublishedAt;
    private int version;
    private List<UserInfoPublicModel> users;

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

    public GrantPublicOverviewModel getGrant() {
        return grant;
    }
    public void setGrant(GrantPublicOverviewModel grant) {
        this.grant = grant;
    }

    public String getDmp() {
        return dmp;
    }
    public void setDmp(String dmp) {
        this.dmp = dmp;
    }

    public String getDmpId() {
        return dmpId;
    }
    public void setDmpId(String dmpId) {
        this.dmpId = dmpId;
    }

    public DatasetProfilePublicModel getProfile() {
        return profile;
    }
    public void setProfile(DatasetProfilePublicModel profile) {
        this.profile = profile;
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

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Date getFinalizedAt() {
        return finalizedAt;
    }
    public void setFinalizedAt(Date finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public Date getDmpPublishedAt() {
        return dmpPublishedAt;
    }
    public void setDmpPublishedAt(Date dmpPublishedAt) {
        this.dmpPublishedAt = dmpPublishedAt;
    }

    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }

    public List<UserInfoPublicModel> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfoPublicModel> users) {
        this.users = users;
    }

}
