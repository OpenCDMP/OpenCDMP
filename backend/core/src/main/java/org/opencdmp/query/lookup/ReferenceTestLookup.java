package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import org.opencdmp.model.persist.externalfetcher.ExternalFetcherBaseSourceConfigurationPersist;
import org.opencdmp.model.reference.Reference;

import java.util.List;

public class ReferenceTestLookup extends Lookup {

    private String like;

    private String key;

    private List<Reference> dependencyReferences;

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

    public List<Reference> getDependencyReferences() {
        return dependencyReferences;
    }

    public void setDependencyReferences(List<Reference> dependencyReferences) {
        this.dependencyReferences = dependencyReferences;
    }

    public List<ExternalFetcherBaseSourceConfigurationPersist> getSources() {
        return sources;
    }

    public void setSources(List<ExternalFetcherBaseSourceConfigurationPersist> sources) {
        this.sources = sources;
    }
}
