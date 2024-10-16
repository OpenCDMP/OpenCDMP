package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.query.UsageLimitQuery;

import java.util.List;
import java.util.UUID;

public class UsageLimitLookup extends Lookup {

    private String like;
    private List<IsActive> isActive;
    private List<UUID> ids;
    private List<UsageLimitTargetMetric> usageLimitTargetMetrics;
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

    public void setIds(List<UUID> ids) { this.ids = ids; }

    public List<UsageLimitTargetMetric> getUsageLimitTargetMetrics() {
        return usageLimitTargetMetrics;
    }

    public void setUsageLimitTargetMetrics(List<UsageLimitTargetMetric> usageLimitTargetMetrics) {
        this.usageLimitTargetMetrics = usageLimitTargetMetrics;
    }

    public List<UUID> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<UUID> excludeIds) {
        this.excludedIds = excludeIds;
    }

    public UsageLimitQuery enrich(QueryFactory queryFactory) {
        UsageLimitQuery query = queryFactory.query(UsageLimitQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.ids != null) query.ids(this.ids);
        if (this.usageLimitTargetMetrics != null) query.usageLimitTargetMetrics(this.usageLimitTargetMetrics);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);

        this.enrichCommon(query);

        return query;
    }
}
