package org.opencdmp.model.publicapi.doi;

import org.opencdmp.data.EntityDoiEntity;

import java.util.UUID;

public class DoiPublicModel {
    private UUID id;
    private String repositoryId;
    private String doi;

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getRepositoryId() {
        return repositoryId;
    }
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getDoi() {
        return doi;
    }
    public void setDoi(String doi) {
        this.doi = doi;
    }

    public static DoiPublicModel fromDataModel(EntityDoiEntity entity) {
        DoiPublicModel model = new DoiPublicModel();
        model.setId(entity.getId());
        model.setRepositoryId(entity.getRepositoryId());
        model.setDoi(entity.getDoi());
        return model;
    }
}
