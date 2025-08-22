package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.DescriptionStatusQuery;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.List;
import java.util.UUID;

public class DescriptionStatusLookup extends Lookup {

    @Schema(description = SwaggerHelpers.DescriptionStatus.like_description)
    private String like;

    @Schema(description = SwaggerHelpers.DescriptionStatus.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.DescriptionStatus.excludeIds_description)
    private List<UUID> excludedIds;

    @Schema(description = SwaggerHelpers.DescriptionStatus.isActive_description)
    private List<IsActive> isActive;

    @Schema(description = SwaggerHelpers.DescriptionStatus.internalStatuses_description)
    private List<org.opencdmp.commons.enums.DescriptionStatus> internalStatuses;

    public String getLike() { return like; }

    public void setLike(String like) { this.like = like; }

    public List<UUID> getIds() { return ids; }

    public void setIds(List<UUID> ids) { this.ids = ids; }

    public List<UUID> getExcludedIds() { return excludedIds; }

    public void setExcludedIds(List<UUID> excludedIds) { this.excludedIds = excludedIds; }

    public List<IsActive> getIsActive() { return isActive; }

    public void setIsActive(List<IsActive> isActive) { this.isActive = isActive; }

    public List<DescriptionStatus> getInternalStatuses() { return this.internalStatuses; }

    public void setInternalStatuses(List<DescriptionStatus> internalStatuses) { this.internalStatuses = internalStatuses; }

    public DescriptionStatusQuery enrich(QueryFactory queryFactory) {
        DescriptionStatusQuery query = queryFactory.query(DescriptionStatusQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludeIds(this.excludedIds);
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.internalStatuses != null) query.internalStatuses(this.internalStatuses);

        this.enrichCommon(query);

        return query;
    }
}
