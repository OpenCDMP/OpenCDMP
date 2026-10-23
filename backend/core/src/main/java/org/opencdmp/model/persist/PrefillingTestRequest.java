package org.opencdmp.model.persist;

import org.opencdmp.model.persist.externalfetcher.ExternalFetcherBaseSourceConfigurationPersist;

import java.util.List;
import java.util.UUID;

public class PrefillingTestRequest {

    private String like;

    private String key;

    private List<ExternalFetcherBaseSourceConfigurationPersist> sources;

    private UUID prefillingSourceId;

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<ExternalFetcherBaseSourceConfigurationPersist> getSources() {
        return sources;
    }

    public void setSources(List<ExternalFetcherBaseSourceConfigurationPersist> sources) {
        this.sources = sources;
    }

    public UUID getPrefillingSourceId() {
        return prefillingSourceId;
    }

    public void setPrefillingSourceId(UUID prefillingSourceId) {
        this.prefillingSourceId = prefillingSourceId;
    }
}

