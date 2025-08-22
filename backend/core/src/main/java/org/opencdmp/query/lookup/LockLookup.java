package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.LockTargetType;
import org.opencdmp.query.LockQuery;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.List;
import java.util.UUID;

public class LockLookup extends Lookup {

    @Schema(description = SwaggerHelpers.Lock.like_description)
    private String like;

    @Schema(description = SwaggerHelpers.Lock.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.Lock.targetIds_description)
    private List<UUID> targetIds;

    @Schema(description = SwaggerHelpers.Lock.targetTypes_description)
    private List<LockTargetType> targetTypes;

    @Schema(description = SwaggerHelpers.Lock.excludeIds_description)
    private List<UUID> excludedIds;

    @Schema(description = SwaggerHelpers.Lock.userIds_description)
    private List<UUID> userIds;

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public List<UUID> getIds() {
        return ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getTargetIds() {
        return targetIds;
    }

    public void setTargetIds(List<UUID> targetIds) {
        this.targetIds = targetIds;
    }

    public List<UUID> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<UUID> excludeIds) {
        this.excludedIds = excludeIds;
    }

    public List<LockTargetType> getTargetTypes() {
        return targetTypes;
    }

    public void setTargetTypes(List<LockTargetType> targetTypes) {
        this.targetTypes = targetTypes;
    }

    public List<UUID> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<UUID> userIds) {
        this.userIds = userIds;
    }

    public LockQuery enrich(QueryFactory queryFactory) {
        LockQuery query = queryFactory.query(LockQuery.class);
        if (this.like != null)
            query.like(this.like);
        if (this.ids != null)
            query.ids(this.ids);
        if (this.targetIds != null)
            query.targetIds(this.targetIds);
        if (this.targetTypes != null)
            query.targetTypes(this.targetTypes);
        if (this.excludedIds != null)
            query.excludedIds(this.excludedIds);
        if (this.userIds != null)
            query.userIds(this.userIds);

        this.enrichCommon(query);

        return query;
    }

}
