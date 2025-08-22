package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.DescriptionWorkflowQuery;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.List;
import java.util.UUID;

public class DescriptionWorkflowLookup extends Lookup {
    @Schema(description = SwaggerHelpers.DescriptionWorkflow.like_description)
    private String like;

    @Schema(description = SwaggerHelpers.DescriptionWorkflow.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.DescriptionWorkflow.excludeIds_description)
    private List<UUID> excludedIds;

    @Schema(description = SwaggerHelpers.DescriptionWorkflow.isActive_description)
    private List<IsActive> isActive;

    public String getLike() { return like; }

    public void setLike(String like) { this.like = like; }

    public List<UUID> getIds() { return ids; }

    public void setIds(List<UUID> ids) { this.ids = ids; }

    public List<UUID> getExcludedIds() { return excludedIds; }

    public void setExcludedIds(List<UUID> excludedIds) { this.excludedIds = excludedIds; }

    public List<IsActive> getIsActive() { return isActive; }

    public void setIsActive(List<IsActive> isActive) { this.isActive = isActive; }

    public DescriptionWorkflowQuery enrich(QueryFactory queryFactory) {
        DescriptionWorkflowQuery query = queryFactory.query(DescriptionWorkflowQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.isActive != null) query.isActives(isActive);

        this.enrichCommon(query);

        return query;
    }
}
