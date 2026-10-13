package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.SQLUtils;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.elastic.query.InnerObjectPlanStatusElasticQuery;
import org.opencdmp.query.PlanStatusQuery;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.List;
import java.util.UUID;

public class PlanStatusLookup extends Lookup {

    @Schema(description = SwaggerHelpers.PlanStatus.like_description)
    private String like;

    @Schema(description = SwaggerHelpers.PlanStatus.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.PlanStatus.excludeIds_description)
    private List<UUID> excludedIds;

    @Schema(description = SwaggerHelpers.PlanStatus.isActive_description)
    private List<IsActive> isActive;

    @Schema(description = SwaggerHelpers.PlanStatus.internalStatuses_description)
    private List<org.opencdmp.commons.enums.PlanStatus> internalStatuses;

    private TenantLookup tenantSubQuery;

    /** false: only the default tenant's shared rows (no tenant); true: only tenant-owned rows; null: both. */
    private Boolean tenantIsSet;

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

    public Boolean getTenantIsSet() { return tenantIsSet; }
    public void setTenantIsSet(Boolean tenantIsSet) { this.tenantIsSet = tenantIsSet; }

    public TenantLookup getTenantSubQuery() { return tenantSubQuery; }
    public void setTenantSubQuery(TenantLookup tenantSubQuery) { this.tenantSubQuery = tenantSubQuery; }

    public PlanStatusQuery enrich(QueryFactory queryFactory) {
        PlanStatusQuery query = queryFactory.query(PlanStatusQuery.class);
        if (this.like != null) query.like(SQLUtils.toPostgres(this.like));
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.isActive != null) query.isActives(this.isActive);
        if (this.internalStatuses != null) query.internalStatuses(this.internalStatuses);
        if (this.tenantIsSet != null) query.tenantIsSet(this.tenantIsSet);
        if (this.tenantSubQuery != null) query.tenantSubQuery(this.tenantSubQuery.enrich(queryFactory));

        this.enrichCommon(query);

        return query;
    }

    public InnerObjectPlanStatusElasticQuery enrichElasticInner(QueryFactory queryFactory){
        InnerObjectPlanStatusElasticQuery query = queryFactory.query(InnerObjectPlanStatusElasticQuery.class);
        if (this.ids != null) query.ids(this.ids);
        if (this.internalStatuses != null) query.internalStatuses(this.internalStatuses);
        if (this.excludedIds != null ) throw new UnsupportedOperationException();
        if (this.like != null) throw new UnsupportedOperationException();

        this.enrichCommon(query);

        return query;
    }

}
