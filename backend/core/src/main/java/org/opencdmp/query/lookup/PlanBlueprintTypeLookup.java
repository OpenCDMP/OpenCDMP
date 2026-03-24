package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanBlueprintTypeStatus;
import org.opencdmp.query.PlanBlueprintTypeQuery;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.List;
import java.util.UUID;

public class PlanBlueprintTypeLookup extends Lookup {

    @Schema(description = SwaggerHelpers.PlanBlueprintType.like_description)
    private String like;

    @Schema(description = SwaggerHelpers.PlanBlueprintType.isActive_description)
    private List<IsActive> isActive;

    @Schema(description = SwaggerHelpers.PlanBlueprintType.statuses_description)
    private List<PlanBlueprintTypeStatus> statuses;

    @Schema(description = SwaggerHelpers.PlanBlueprintType.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.PlanBlueprintType.excludeIds_description)
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

    public List<PlanBlueprintTypeStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<PlanBlueprintTypeStatus> statuses) {
        this.statuses = statuses;
    }

    public PlanBlueprintTypeQuery enrich(QueryFactory queryFactory) {
        PlanBlueprintTypeQuery query = queryFactory.query(PlanBlueprintTypeQuery.class);
        if (this.like != null)
            query.like(this.like);
        if (this.isActive != null)
            query.isActive(this.isActive);
        if (this.statuses != null)
            query.statuses(this.statuses);
        if (this.ids != null)
            query.ids(this.ids);
        if (this.excludedIds != null)
            query.excludedIds(this.excludedIds);

        this.enrichCommon(query);

        return query;
    }

}
