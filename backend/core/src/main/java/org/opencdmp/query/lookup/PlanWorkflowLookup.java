package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.PlanWorkflowQuery;

import java.util.List;
import java.util.UUID;

public class PlanWorkflowLookup extends Lookup {

    private String like;

    private List<UUID> ids;

    private List<UUID> excludedIds;

    private List<IsActive> isActive;

    public String getLike() { return like; }

    public void setLike(String like) { this.like = like; }

    public List<UUID> getIds() { return ids; }

    public void setIds(List<UUID> ids) { this.ids = ids; }

    public List<UUID> getExcludedIds() { return excludedIds; }

    public void setExcludedIds(List<UUID> excludedIds) { this.excludedIds = excludedIds; }

    public List<IsActive> getIsActive() { return isActive; }

    public void setIsActive(List<IsActive> isActive) { this.isActive = isActive; }

    public PlanWorkflowQuery enrich(QueryFactory queryFactory) {
        PlanWorkflowQuery query = queryFactory.query(PlanWorkflowQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.isActive != null) query.isActives(this.isActive);

        this.enrichCommon(query);

        return query;
    }
}
