package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.apache.commons.lang3.StringUtils;
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.elastic.query.DescriptionElasticQuery;
import org.opencdmp.query.DescriptionQuery;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class DescriptionLookup extends Lookup {

    private String like;

    private List<UUID> ids;
    private List<UUID> excludedIds;
    private Instant createdAfter;
    private Instant createdBefore;
    private Instant finalizedAfter;
    private Instant finalizedBefore;

    private PlanLookup planSubQuery;
    private TenantLookup tenantSubQuery;
    private DescriptionTemplateLookup descriptionTemplateSubQuery;
    private DescriptionReferenceLookup descriptionReferenceSubQuery;
    private DescriptionTagLookup descriptionTagSubQuery;

    private List<IsActive> isActive;

    private List<DescriptionStatus> statuses;

    public String getLike() {
        return this.like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public List<UUID> getIds() {
        return this.ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getExcludedIds() {
        return this.excludedIds;
    }

    public void setExcludedIds(List<UUID> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public List<IsActive> getIsActive() {
        return this.isActive;
    }

    public void setIsActive(List<IsActive> isActive) {
        this.isActive = isActive;
    }

    public List<DescriptionStatus> getStatuses() {
        return this.statuses;
    }

    public void setStatuses(List<DescriptionStatus> statuses) {
        this.statuses = statuses;
    }

    public PlanLookup getPlanSubQuery() {
        return this.planSubQuery;
    }

    public void setPlanSubQuery(PlanLookup planSubQuery) { this.planSubQuery = planSubQuery; }

    public TenantLookup getTenantSubQuery() { return this.tenantSubQuery; }

    public void setTenantSubQuery(TenantLookup tenantSubQuery) { this.tenantSubQuery = tenantSubQuery; }

    public DescriptionTemplateLookup getDescriptionTemplateSubQuery() { return this.descriptionTemplateSubQuery; }

    public void setDescriptionTemplateSubQuery(DescriptionTemplateLookup descriptionTemplateSubQuery) { this.descriptionTemplateSubQuery = descriptionTemplateSubQuery; }

    public DescriptionReferenceLookup getDescriptionReferenceSubQuery() { return this.descriptionReferenceSubQuery; }

    public void setDescriptionReferenceSubQuery(DescriptionReferenceLookup descriptionReferenceSubQuery) { this.descriptionReferenceSubQuery = descriptionReferenceSubQuery; }

    public DescriptionTagLookup getDescriptionTagSubQuery() { return this.descriptionTagSubQuery; }

    public void setDescriptionTagSubQuery(DescriptionTagLookup descriptionTagSubQuery) { this.descriptionTagSubQuery = descriptionTagSubQuery; }

    public Instant getCreatedAfter() {
        return this.createdAfter;
    }

    public void setCreatedAfter(Instant createdAfter) {
        this.createdAfter = createdAfter;
    }

    public Instant getCreatedBefore() {
        return this.createdBefore;
    }

    public void setCreatedBefore(Instant createdBefore) {
        this.createdBefore = createdBefore;
    }

    public Instant getFinalizedAfter() {
        return this.finalizedAfter;
    }

    public void setFinalizedAfter(Instant finalizedAfter) {
        this.finalizedAfter = finalizedAfter;
    }

    public Instant getFinalizedBefore() {
        return this.finalizedBefore;
    }

    public void setFinalizedBefore(Instant finalizedBefore) {
        this.finalizedBefore = finalizedBefore;
    }

    public DescriptionQuery enrich(QueryFactory queryFactory) {
        DescriptionQuery query = queryFactory.query(DescriptionQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.planSubQuery != null) query.planSubQuery(this.planSubQuery.enrich(queryFactory));
        if (this.tenantSubQuery != null) query.tenantSubQuery(this.tenantSubQuery.enrich(queryFactory));
        if (this.descriptionTemplateSubQuery != null) query.descriptionTemplateSubQuery(this.descriptionTemplateSubQuery.enrich(queryFactory));
        if (this.descriptionReferenceSubQuery != null) query.descriptionReferenceSubQuery(this.descriptionReferenceSubQuery.enrich(queryFactory));
        if (this.descriptionTagSubQuery != null) query.descriptionTagSubQuery(this.descriptionTagSubQuery.enrich(queryFactory));
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.statuses != null) query.statuses(this.statuses);
        if (this.createdAfter != null) query.createdAfter(this.createdAfter);
        if (this.createdBefore != null) query.createdBefore(this.createdBefore);
        if (this.finalizedAfter != null) query.finalizedAfter(this.finalizedAfter);
        if (this.finalizedBefore != null) query.finalizedBefore(this.finalizedBefore);

        this.enrichCommon(query);

        return query;
    }

    public DescriptionElasticQuery enrichElastic(QueryFactory queryFactory) {
        DescriptionElasticQuery query = queryFactory.query(DescriptionElasticQuery.class);
        if (this.like != null) query.like(StringUtils.strip(this.like, "%"));
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.statuses != null) query.statuses(this.statuses);
        if (this.createdAfter != null) query.createdAfter(this.createdAfter);
        if (this.createdBefore != null) query.createdBefore(this.createdBefore);
        if (this.finalizedAfter != null) query.finalizedAfter(this.finalizedAfter);
        if (this.finalizedBefore != null) query.finalizedBefore(this.finalizedBefore);
        if (this.planSubQuery != null) query.planSubQuery(this.planSubQuery.enrichElasticInner(queryFactory));

        if (this.tenantSubQuery != null && this.tenantSubQuery.getIds() != null && !this.tenantSubQuery.getIds().isEmpty()) query.tenantIds(this.tenantSubQuery.getIds());
        if (this.descriptionTemplateSubQuery != null) query.descriptionTemplateSubQuery(this.descriptionTemplateSubQuery.enrichElasticInner(queryFactory));
        if (this.descriptionReferenceSubQuery != null) query.referenceSubQuery(this.descriptionReferenceSubQuery.enrichElasticInner(queryFactory));
        if (this.descriptionTagSubQuery != null) query.tagSubQuery(this.descriptionTagSubQuery.enrichElasticInner(queryFactory));

        this.enrichCommon(query);

        return query;
    }

    public boolean useElastic() {
        return this.like != null && !this.like.isBlank();
    }

}
