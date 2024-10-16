package org.opencdmp.model;

import org.opencdmp.commons.enums.EntityType;

import java.util.UUID;

public class PublicEntityDoi {

    private UUID id;

    public static final String _id = "id";

    private EntityType entityType;

    public static final String _entityType = "entityType";

    private String repositoryId;

    public static final String _repositoryId = "repositoryId";

    private String doi;

    public static final String _doi = "doi";

    private UUID entityId;

    public static final String _entityId = "entityId";


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
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

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }
}
