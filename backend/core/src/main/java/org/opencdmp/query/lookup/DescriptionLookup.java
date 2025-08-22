package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.elastic.query.DescriptionElasticQuery;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class DescriptionLookup extends Lookup {

    @Schema(description = SwaggerHelpers.Description.like_description)
    private String like;

    @Schema(description = SwaggerHelpers.Description.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.Description.excludeIds_description)
    private List<UUID> excludedIds;

    @Schema(description = SwaggerHelpers.Description.createdAfter_description)
    private Instant createdAfter;

    @Schema(description = SwaggerHelpers.Description.createdBefore_description)
    private Instant createdBefore;

    @Schema(description = SwaggerHelpers.Description.finalizedAfter_description)
    private Instant finalizedAfter;

    @Schema(description = SwaggerHelpers.Description.finalizedBefore_description)
    private Instant finalizedBefore;

    @Schema(description = SwaggerHelpers.Description.isActive_description)
    private List<IsActive> isActive;

    @Schema(description = SwaggerHelpers.Description.statusIds_description)
    private List<UUID> statusIds;

    @Schema(description = SwaggerHelpers.Description.planSubQuery_description)
    private PlanLookup planSubQuery;

    @Schema(description = SwaggerHelpers.Description.tenantSubQuery_description)
    private TenantLookup tenantSubQuery;

    @Schema(description = SwaggerHelpers.Description.descriptionTemplateSubQuery_description)
    private DescriptionTemplateLookup descriptionTemplateSubQuery;

    @Schema(description = SwaggerHelpers.Description.descriptionReferenceSubQuery_description)
    private DescriptionReferenceLookup descriptionReferenceSubQuery;

    @Schema(description = SwaggerHelpers.Description.descriptionTagSubQuery_description)
    private DescriptionTagLookup descriptionTagSubQuery;

    @Schema(description = SwaggerHelpers.Description.descriptionStatusSubQuery_description)
    private DescriptionStatusLookup descriptionStatusSubQuery;

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

    public List<UUID> getStatusIds() {
        return statusIds;
    }

    public void setStatusIds(List<UUID> statusIds) {
        this.statusIds = statusIds;
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

    public DescriptionStatusLookup getDescriptionStatusSubQuery() {
        return descriptionStatusSubQuery;
    }

    public void setDescriptionStatusSubQuery(DescriptionStatusLookup descriptionStatusSubQuery) {
        this.descriptionStatusSubQuery = descriptionStatusSubQuery;
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
        if (this.statusIds != null) query.statusIds(this.statusIds);
        if (this.createdAfter != null) query.createdAfter(this.createdAfter);
        if (this.createdBefore != null) query.createdBefore(this.createdBefore);
        if (this.finalizedAfter != null) query.finalizedAfter(this.finalizedAfter);
        if (this.finalizedBefore != null) query.finalizedBefore(this.finalizedBefore);
        if (this.descriptionStatusSubQuery != null) query.descriptionStatusSubQuery(this.descriptionStatusSubQuery.enrich(queryFactory));

        this.enrichCommon(query);

        return query;
    }

    public DescriptionElasticQuery enrichElastic(QueryFactory queryFactory) {
        DescriptionElasticQuery query = queryFactory.query(DescriptionElasticQuery.class);
        if (this.like != null) query.like(StringUtils.strip(this.like, "%"));
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.statusIds != null) query.statusIds(this.statusIds);
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
