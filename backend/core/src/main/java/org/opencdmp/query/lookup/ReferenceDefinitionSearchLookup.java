package org.opencdmp.query.lookup;

import org.opencdmp.query.ReferenceQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.UUID;

public class ReferenceDefinitionSearchLookup extends Lookup {

    private String like;

    private UUID referenceTypeId;

    private String key;

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public UUID getReferenceTypeId() {
        return referenceTypeId;
    }

    public void setReferenceTypeId(UUID referenceTypeId) {
        this.referenceTypeId = referenceTypeId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ReferenceQuery enrich(QueryFactory queryFactory) {
        ReferenceQuery query = queryFactory.query(ReferenceQuery.class);
        if (this.referenceTypeId != null) query.typeIds(this.referenceTypeId);

        this.enrichCommon(query);

        return query;
    }
}
