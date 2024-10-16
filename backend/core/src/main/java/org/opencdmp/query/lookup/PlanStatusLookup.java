package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.query.PlanStatusQuery;

import java.util.List;
import java.util.UUID;

public class PlanStatusLookup extends Lookup {

    private String like;

    private List<UUID> ids;

    private List<UUID> excludedIds;

    private List<IsActive> isActive;

    private List<org.opencdmp.commons.enums.PlanStatus> internalStatuses;

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

    public List<UUID> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<UUID> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public List<IsActive> getIsActive() {
        return isActive;
    }

    public void setIsActive(List<IsActive> isActive) {
        this.isActive = isActive;
    }

    public List<PlanStatus> getInternalStatuses() { return this.internalStatuses; }

    public void setInternalStatuses(List<PlanStatus> internalStatuses) { this.internalStatuses = internalStatuses; }

    public PlanStatusQuery enrich(QueryFactory queryFactory) {
        PlanStatusQuery query = queryFactory.query(PlanStatusQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.isActive != null) query.isActives(this.isActive);
        if (this.internalStatuses != null) query.internalStatuses(this.internalStatuses);

        this.enrichCommon(query);

        return query;
    }

}
