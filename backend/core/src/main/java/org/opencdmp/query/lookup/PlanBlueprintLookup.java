package org.opencdmp.query.lookup;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.PlanBlueprintStatus;
import org.opencdmp.commons.enums.PlanBlueprintVersionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.PlanBlueprintQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.List;
import java.util.UUID;

public class PlanBlueprintLookup extends Lookup {

    @Schema(description = SwaggerHelpers.PlanBlueprint.like_description)
    private String like;

    @Schema(description = SwaggerHelpers.PlanBlueprint.isActive_description)
    private List<IsActive> isActive;

    @Schema(description = SwaggerHelpers.PlanBlueprint.statuses_description)
    private List<PlanBlueprintStatus> statuses;

    @Schema(description = SwaggerHelpers.PlanBlueprint.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.PlanBlueprint.excludeIds_description)
    private List<UUID> excludedIds;

    @Schema(description = SwaggerHelpers.PlanBlueprint.groupIds_description)
    private List<UUID> groupIds;

    @Schema(description = SwaggerHelpers.PlanBlueprint.excludeGroupIds_description)
    private List<UUID> excludedGroupIds;

    @Schema(description = SwaggerHelpers.PlanBlueprint.versionStatuses_description)
    private List<PlanBlueprintVersionStatus> versionStatuses;

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

    public List<PlanBlueprintStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<PlanBlueprintStatus> statuses) {
        this.statuses = statuses;
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

    public void setExcludedIds(List<UUID> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public List<PlanBlueprintVersionStatus> getVersionStatuses() {
        return versionStatuses;
    }

    public void setVersionStatuses(List<PlanBlueprintVersionStatus> versionStatuses) {
        this.versionStatuses = versionStatuses;
    }

    public List<UUID> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<UUID> groupIds) {
        this.groupIds = groupIds;
    }

    public List<UUID> getExcludedGroupIds() {
        return excludedGroupIds;
    }

    public void setExcludedGroupIds(List<UUID> excludedGroupIds) {
        this.excludedGroupIds = excludedGroupIds;
    }

    public PlanBlueprintQuery enrich(QueryFactory queryFactory) {
        PlanBlueprintQuery query = queryFactory.query(PlanBlueprintQuery.class);
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
        if (this.groupIds != null)
            query.groupIds(this.groupIds);
        if (this.excludedGroupIds != null)
            query.excludedGroupIds(this.excludedGroupIds);
        if (this.versionStatuses != null)
            query.versionStatuses(this.versionStatuses);

        this.enrichCommon(query);

        return query;
    }

}
