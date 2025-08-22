package org.opencdmp.query.lookup;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.elastic.query.NestedTagElasticQuery;
import org.opencdmp.query.DescriptionTagQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.Collection;
import java.util.UUID;

public class DescriptionTagLookup extends Lookup {

    @Schema(description = SwaggerHelpers.DescriptionTag.ids_description)
    private Collection<UUID> ids;

    @Schema(description = SwaggerHelpers.DescriptionTag.excludeIds_description)
    private Collection<UUID> excludedIds;

    @Schema(description = SwaggerHelpers.DescriptionTag.isActive_description)
    private Collection<IsActive> isActives;

    @Schema(description = SwaggerHelpers.DescriptionTag.descriptionIds_description)
    private Collection<UUID> descriptionIds;

    @Schema(description = SwaggerHelpers.DescriptionTag.tagIds_description)
    private Collection<UUID> tagIds;

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

    public Collection<UUID> getTagIds() {
        return tagIds;
    }

    public void setTagIds(Collection<UUID> referenceIds) {
        this.tagIds = referenceIds;
    }

    public DescriptionTagQuery enrich(QueryFactory queryFactory) {
        DescriptionTagQuery query = queryFactory.query(DescriptionTagQuery.class);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.isActives != null) query.isActive(this.isActives);
        if (this.descriptionIds != null) query.descriptionIds(this.descriptionIds);
        if (this.tagIds != null) query.tagIds(this.tagIds);

        this.enrichCommon(query);

        return query;
    }

    public NestedTagElasticQuery enrichElasticInner(QueryFactory queryFactory) {
        NestedTagElasticQuery query = queryFactory.query(NestedTagElasticQuery.class);
        if (this.ids != null) throw new UnsupportedOperationException("");
        if (this.excludedIds != null) throw new UnsupportedOperationException("");
        if (this.isActives != null) throw new UnsupportedOperationException("");
        if (this.descriptionIds != null) throw new UnsupportedOperationException("");
        if (this.tagIds != null) query.ids(this.tagIds);

        this.enrichCommon(query);

        return query;
    }
}
