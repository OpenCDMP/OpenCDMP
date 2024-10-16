package org.opencdmp.query.lookup;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.elastic.query.NestedReferenceElasticQuery;
import org.opencdmp.query.DescriptionReferenceQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.Collection;
import java.util.UUID;

public class DescriptionReferenceLookup extends Lookup {

    private Collection<UUID> ids;

    private Collection<UUID> excludedIds;

    private Collection<IsActive> isActives;

    private Collection<UUID> descriptionIds;

    private Collection<UUID> referenceIds;

    public Collection<UUID> getIds() {
        return ids;
    }

    public void setIds(Collection<UUID> ids) {
        this.ids = ids;
    }

    public Collection<UUID> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(Collection<UUID> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public Collection<IsActive> getIsActives() {
        return isActives;
    }

    public void setIsActives(Collection<IsActive> isActives) {
        this.isActives = isActives;
    }

    public Collection<UUID> getDescriptionIds() {
        return descriptionIds;
    }

    public void setDescriptionIds(Collection<UUID> descriptionIds) {
        this.descriptionIds = descriptionIds;
    }

    public Collection<UUID> getReferenceIds() {
        return referenceIds;
    }

    public void setReferenceIds(Collection<UUID> referenceIds) {
        this.referenceIds = referenceIds;
    }

    public DescriptionReferenceQuery enrich(QueryFactory queryFactory) {
        DescriptionReferenceQuery query = queryFactory.query(DescriptionReferenceQuery.class);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.isActives != null) query.isActive(this.isActives);
        if (this.descriptionIds != null) query.descriptionIds(this.descriptionIds);
        if (this.referenceIds != null) query.referenceIds(this.referenceIds);

        this.enrichCommon(query);

        return query;
    }

    public NestedReferenceElasticQuery enrichElasticInner(QueryFactory queryFactory) {
        NestedReferenceElasticQuery query = queryFactory.query(NestedReferenceElasticQuery.class);
        if (this.ids != null) throw new UnsupportedOperationException("");
        if (this.excludedIds != null) throw new UnsupportedOperationException("");
        if (this.isActives != null) throw new UnsupportedOperationException("");
        if (this.descriptionIds != null) throw new UnsupportedOperationException("");
        if (this.referenceIds != null) query.ids(this.referenceIds);

        this.enrichCommon(query);

        return query;
    }

}
