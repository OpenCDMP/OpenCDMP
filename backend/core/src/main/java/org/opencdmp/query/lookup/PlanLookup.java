package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.PlanVersionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.elastic.query.PlanElasticQuery;
import org.opencdmp.elastic.query.InnerObjectPlanElasticQuery;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.List;
import java.util.UUID;

public class PlanLookup extends Lookup {

    @Schema(description = SwaggerHelpers.Plan.like_description)
    private String like;

    @Schema(description = SwaggerHelpers.Plan.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.Plan.excludeIds_description)
    private List<UUID> excludedIds;

    @Schema(description = SwaggerHelpers.Plan.groupIds_description)
    private List<UUID> groupIds;

    @Schema(description = SwaggerHelpers.Plan.isActive_description)
    private List<IsActive> isActive;

    @Schema(description = SwaggerHelpers.Plan.versionStatuses_description)
    private List<PlanVersionStatus> versionStatuses;

    @Schema(description = SwaggerHelpers.Plan.statusIds_description)
    private List<UUID> statusIds;

    @Schema(description = SwaggerHelpers.Plan.accessTypes_description)
    private List<PlanAccessType> accessTypes;

    @Schema(description = SwaggerHelpers.Plan.versions_description)
    private List<Integer> versions;

    @Schema(description = SwaggerHelpers.Plan.planDescriptionTemplateSubQuery_description)
    private PlanDescriptionTemplateLookup planDescriptionTemplateSubQuery;

    @Schema(description = SwaggerHelpers.Plan.planUserSubQuery_description)
    private PlanUserLookup planUserSubQuery;

    @Schema(description = SwaggerHelpers.Plan.planBlueprintSubQuery_description)
    private PlanBlueprintLookup planBlueprintSubQuery;

    @Schema(description = SwaggerHelpers.Plan.planReferenceSubQuery_description)
    private PlanReferenceLookup planReferenceSubQuery;

    @Schema(description = SwaggerHelpers.Plan.planStatusSubQuery_description)
    private PlanStatusLookup planStatusSubQuery;

    @Schema(description = SwaggerHelpers.Plan.tenantSubQuery_description)
    private TenantLookup tenantSubQuery;

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

    public TenantLookup getTenantSubQuery() { return this.tenantSubQuery; }

    public void setTenantSubQuery(TenantLookup tenantSubQuery) { this.tenantSubQuery = tenantSubQuery; }

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

    public List<Integer> getVersions() {
        return this.versions;
    }

    public void setVersions(List<Integer> versions) {
        this.versions = versions;
    }

    public List<PlanAccessType> getAccessTypes() {
        return this.accessTypes;
    }

    public void setAccessTypes(List<PlanAccessType> accessTypes) {
        this.accessTypes = accessTypes;
    }

    public List<PlanVersionStatus> getVersionStatuses() {
        return this.versionStatuses;
    }

    public void setVersionStatuses(List<PlanVersionStatus> versionStatuses) {
        this.versionStatuses = versionStatuses;
    }

    public PlanDescriptionTemplateLookup getPlanDescriptionTemplateSubQuery() {
        return this.planDescriptionTemplateSubQuery;
    }

    public void setPlanDescriptionTemplateSubQuery(PlanDescriptionTemplateLookup planDescriptionTemplateSubQuery) {
        this.planDescriptionTemplateSubQuery = planDescriptionTemplateSubQuery;
    }

    public List<UUID> getGroupIds() {
        return this.groupIds;
    }

    public void setGroupIds(List<UUID> groupIds) {
        this.groupIds = groupIds;
    }

    public PlanUserLookup getPlanUserSubQuery() {
        return this.planUserSubQuery;
    }

    public void setPlanUserSubQuery(PlanUserLookup planUserSubQuery) {
        this.planUserSubQuery = planUserSubQuery;
    }

    public PlanBlueprintLookup getPlanBlueprintSubQuery() { return this.planBlueprintSubQuery; }

    public void setPlanBlueprintLookup(PlanBlueprintLookup planBlueprintSubQuery) { this.planBlueprintSubQuery = planBlueprintSubQuery; }


    public PlanReferenceLookup getPlanReferenceSubQuery() { return this.planReferenceSubQuery; }

    public void setPlanReferenceLookup(PlanReferenceLookup planReferenceSubQuery) { this.planReferenceSubQuery = planReferenceSubQuery; }

    public PlanStatusLookup getPlanStatusSubQuery() {
        return planStatusSubQuery;
    }

    public void setPlanStatusSubQuery(PlanStatusLookup planStatusSubQuery) {
        this.planStatusSubQuery = planStatusSubQuery;
    }

    public PlanQuery enrich(QueryFactory queryFactory) {
        PlanQuery query = queryFactory.query(PlanQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.ids != null) query.ids(this.ids);
        if (this.tenantSubQuery != null) query.tenantSubQuery(this.tenantSubQuery.enrich(queryFactory));
        if (this.groupIds != null) query.groupIds(this.groupIds);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.accessTypes != null) query.accessTypes(this.accessTypes);
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.statusIds != null) query.statusIds(this.statusIds);
        if (this.versions != null) query.versions(this.versions);
        if (this.versionStatuses != null) query.versionStatuses(this.versionStatuses);
        if (this.planDescriptionTemplateSubQuery != null) query.planDescriptionTemplateSubQuery(this.planDescriptionTemplateSubQuery.enrich(queryFactory));
        if (this.planUserSubQuery != null) query.planUserSubQuery(this.planUserSubQuery.enrich(queryFactory));
        if (this.planBlueprintSubQuery != null) query.planBlueprintSubQuery(this.planBlueprintSubQuery.enrich(queryFactory));
        if (this.planReferenceSubQuery != null) query.planReferenceSubQuery(this.planReferenceSubQuery.enrich(queryFactory));
        if (this.planStatusSubQuery != null) query.planStatusSubQuery(this.planStatusSubQuery.enrich(queryFactory));

        this.enrichCommon(query);

        return query;
    }

    public PlanElasticQuery enrichElastic(QueryFactory queryFactory) {
        PlanElasticQuery query = queryFactory.query(PlanElasticQuery.class);
        if (this.like != null) query.like(StringUtils.strip(this.like, "%"));
        if (this.ids != null) query.ids(this.ids);
        if (this.groupIds != null) query.groupIds(this.groupIds);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.accessTypes != null) query.accessTypes(this.accessTypes);
        if (this.statusIds != null) query.statuses(this.statusIds);
        if (this.versions != null) query.versions(this.versions);
        if (this.versionStatuses != null) query.versionStatuses(this.versionStatuses);
        if (this.planDescriptionTemplateSubQuery != null) query.planDescriptionTemplateSubQuery(this.planDescriptionTemplateSubQuery.enrichElasticInner(queryFactory));
        if (this.planUserSubQuery != null) query.planSubQuery(this.planUserSubQuery.enrichElasticInner(queryFactory));
        if (this.planBlueprintSubQuery != null && this.planBlueprintSubQuery.getIds() != null && !this.planBlueprintSubQuery.getIds().isEmpty()) query.blueprintIds(this.planBlueprintSubQuery.getIds());
        if (this.planReferenceSubQuery != null) query.referenceSubQuery(this.planReferenceSubQuery.enrichElasticInner(queryFactory));
        if (this.tenantSubQuery != null) throw new UnsupportedOperationException();
        if (this.planStatusSubQuery != null) query.planStatusElasticQuery(this.planStatusSubQuery.enrichElasticInner(queryFactory));

        this.enrichCommon(query);

        return query;
    }

    public InnerObjectPlanElasticQuery enrichElasticInner(QueryFactory queryFactory) {
        InnerObjectPlanElasticQuery query = queryFactory.query(InnerObjectPlanElasticQuery.class);
        if (this.like != null) query.like(StringUtils.strip(this.like, "%"));
        if (this.ids != null) query.ids(this.ids);
        if (this.groupIds != null) query.groupIds(this.groupIds);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.accessTypes != null) query.accessTypes(this.accessTypes);
        if (this.statusIds != null) query.statusIds(this.statusIds);
        if (this.versions != null) query.versions(this.versions);
        if (this.versionStatuses != null) query.versionStatuses(this.versionStatuses);
        if (this.planDescriptionTemplateSubQuery != null) throw new UnsupportedOperationException("");
        if (this.planUserSubQuery != null) query.planSubQuery(this.planUserSubQuery.enrichElasticInner(queryFactory));
        if (this.planBlueprintSubQuery != null && this.planBlueprintSubQuery.getIds() != null && !this.planBlueprintSubQuery.getIds().isEmpty()) throw new UnsupportedOperationException("");
        if (this.planReferenceSubQuery != null) throw new UnsupportedOperationException("");
        if (this.tenantSubQuery != null) throw new UnsupportedOperationException();
        if (this.planStatusSubQuery != null) throw new UnsupportedOperationException();

        return query;
    }

    public boolean useElastic() { return this.like != null && !this.like.isBlank(); }
}
