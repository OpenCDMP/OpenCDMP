package org.opencdmp.model.persist;

import org.opencdmp.model.persist.externalfetcher.ExternalFetcherBaseSourceConfigurationPersist;

import java.util.List;

public class PrefillingTestRequest {

    private String like;

    private String key;

    private List<ExternalFetcherBaseSourceConfigurationPersist> sources;

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
}

