package org.opencdmp.model.file;

import org.opencdmp.commonmodels.enums.PluginEntityType;

import java.util.UUID;

public class TemplateValidationRequestModel {

    private UUID fileId;
    private String repositoryId;
    private PluginEntityType entityType;

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public PluginEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(PluginEntityType entityType) {
        this.entityType = entityType;
    }
}
