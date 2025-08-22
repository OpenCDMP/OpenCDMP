package org.opencdmp.query.lookup;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.ReferenceSourceType;
import org.opencdmp.query.ReferenceQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ReferenceLookup extends Lookup {

    @Schema(description = SwaggerHelpers.Reference.like_description)
    private String like;

    @Schema(description = SwaggerHelpers.Reference.isActive_description)
    private List<IsActive> isActive;

    @Schema(description = SwaggerHelpers.Reference.sourceTypes_description)
    private Collection<ReferenceSourceType> sourceTypes;

    @Schema(description = SwaggerHelpers.Reference.typeIds_description)
    private Collection<UUID> typeIds;

    @Schema(description = SwaggerHelpers.Reference.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.Reference.excludeIds_description)
    private List<UUID> excludedIds;

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public List<IsActive> getIsActive() {
        return isActive;
    }

    public void setIsActive(List<IsActive> isActive) {
        this.isActive = isActive;
    }

    public List<UUID> getIds() {
        return ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<UUID> excludeIds) {
        this.excludedIds = excludeIds;
    }

    public Collection<ReferenceSourceType> getSourceTypes() {
        return sourceTypes;
    }

    public void setSourceTypes(Collection<ReferenceSourceType> sourceTypes) {
        this.sourceTypes = sourceTypes;
    }

    public Collection<UUID> getTypeIds() {
        return typeIds;
    }

    public void setTypeIds(Collection<UUID> typeIds) {
        this.typeIds = typeIds;
    }

    public ReferenceQuery enrich(QueryFactory queryFactory) {
        ReferenceQuery query = queryFactory.query(ReferenceQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.typeIds != null) query.typeIds(this.typeIds);
        if (this.sourceTypes != null) query.sourceTypes(this.sourceTypes);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);

        this.enrichCommon(query);

        return query;
    }

}
