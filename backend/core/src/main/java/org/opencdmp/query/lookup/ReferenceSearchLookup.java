package org.opencdmp.query.lookup;

import org.opencdmp.model.reference.Reference;
import org.opencdmp.query.ReferenceQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class ReferenceSearchLookup extends Lookup {

    private String like;

    private UUID typeId;

    private String key;

    private List<Reference> dependencyReferences;

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public UUID getTypeId() {
        return typeId;
    }

    public void setTypeId(UUID typeId) {
        this.typeId = typeId;
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

    public ReferenceQuery enrich(QueryFactory queryFactory) {
        ReferenceQuery query = queryFactory.query(ReferenceQuery.class);
        if (this.typeId != null) query.typeIds(this.typeId);

        this.enrichCommon(query);

        return query;
    }
}
