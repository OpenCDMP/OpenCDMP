package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.SQLUtils;
import org.opencdmp.commons.enums.DescriptionTemplateStatus;
import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.elastic.query.InnerObjectDescriptionTemplateElasticQuery;
import org.opencdmp.elastic.query.NestedDescriptionTemplateElasticQuery;
import org.opencdmp.query.DescriptionTemplateQuery;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.List;
import java.util.UUID;

public class DescriptionTemplateLookup extends Lookup {

    @Schema(description = SwaggerHelpers.DescriptionTemplate.like_description)
    private String like;

    @Schema(description = SwaggerHelpers.DescriptionTemplate.isActive_description)
    private List<IsActive> isActive;

    @Schema(description = SwaggerHelpers.DescriptionTemplate.groupIds_description)
    private List<UUID> groupIds;

    @Schema(description = SwaggerHelpers.DescriptionTemplate.versions_description)
    private List<Short> versions;

    @Schema(description = SwaggerHelpers.DescriptionTemplate.statuses_description)
    private List<DescriptionTemplateStatus> statuses;

    @Schema(description = SwaggerHelpers.DescriptionTemplate.versionStatuses_description)
    private List<DescriptionTemplateVersionStatus> versionStatuses;

    @Schema(description = SwaggerHelpers.DescriptionTemplate.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.DescriptionTemplate.typeIds_description)
    private List<UUID> typeIds;

    @Schema(description = SwaggerHelpers.DescriptionTemplate.excludeIds_description)
    private List<UUID> excludedIds;

    @Schema(description = SwaggerHelpers.DescriptionTemplate.excludeGroupIds_description)
    private List<UUID> excludedGroupIds;

    @Schema(description = SwaggerHelpers.DescriptionTemplate.languages_description)
    private List<String> languages;

    @Schema(description = SwaggerHelpers.DescriptionTemplate.onlyCanEdit_description)
    private Boolean onlyCanEdit;

    private TenantLookup tenantSubQuery;

    /** false: only the default tenant's shared rows (no tenant); true: only tenant-owned rows; null: both. */
    private Boolean tenantIsSet;

    public String getLike() {
        return this.like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public List<IsActive> getIsActive() {
        return this.isActive;
    }

    public void setIsActive(List<IsActive> isActive) {
        this.isActive = isActive;
    }

    public List<UUID> getGroupIds() {
        return this.groupIds;
    }

    public void setGroupIds(List<UUID> groupIds) {
        this.groupIds = groupIds;
    }

    public List<Short> getVersions() {
        return this.versions;
    }

    public void setVersions(List<Short> versions) {
        this.versions = versions;
    }

    public List<DescriptionTemplateStatus> getStatuses() {
        return this.statuses;
    }

    public void setStatuses(List<DescriptionTemplateStatus> statuses) {
        this.statuses = statuses;
    }

    public List<DescriptionTemplateVersionStatus> getVersionStatuses() {
        return this.versionStatuses;
    }

    public void setVersionStatuses(List<DescriptionTemplateVersionStatus> versionStatuses) {
        this.versionStatuses = versionStatuses;
    }

    public List<UUID> getIds() {
        return this.ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getTypeIds() {
        return this.typeIds;
    }

    public void setTypeIds(List<UUID> typeIds) {
        this.typeIds = typeIds;
    }

    public List<UUID> getExcludedIds() {
        return this.excludedIds;
    }

    public void setExcludedIds(List<UUID> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public List<UUID> getExcludedGroupIds() {
        return this.excludedGroupIds;
    }

    public void setExcludedGroupIds(List<UUID> excludedGroupIds) {
        this.excludedGroupIds = excludedGroupIds;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public Boolean getOnlyCanEdit() {
        return this.onlyCanEdit;
    }

    public void setOnlyCanEdit(Boolean onlyCanEdit) {
        this.onlyCanEdit = onlyCanEdit;
    }

    public Boolean getTenantIsSet() { return tenantIsSet; }
    public void setTenantIsSet(Boolean tenantIsSet) { this.tenantIsSet = tenantIsSet; }

    public TenantLookup getTenantSubQuery() { return tenantSubQuery; }
    public void setTenantSubQuery(TenantLookup tenantSubQuery) { this.tenantSubQuery = tenantSubQuery; }

    public DescriptionTemplateQuery enrich(QueryFactory queryFactory) {
        DescriptionTemplateQuery query = queryFactory.query(DescriptionTemplateQuery.class);
        if (this.like != null)
            query.like(SQLUtils.toPostgres(this.like));
        if (this.isActive != null)
            query.isActive(this.isActive);
        if (this.groupIds != null)
            query.groupIds(this.groupIds);
        if (this.statuses != null)
            query.statuses(this.statuses);
        if (this.ids != null)
            query.ids(this.ids);
        if (this.excludedIds != null)
            query.excludedIds(this.excludedIds);
        if (this.excludedGroupIds != null)
            query.excludedGroupIds(this.excludedGroupIds);
        if (this.typeIds != null)
            query.typeIds(this.typeIds);
        if (this.versions != null)
            query.versions(this.versions);
        if (this.versionStatuses != null)
            query.versionStatuses(this.versionStatuses);
        if (this.languages != null)
            query.languages(this.languages);
        if (this.onlyCanEdit != null)
            query.onlyCanEdit(this.onlyCanEdit);
        if (this.tenantIsSet != null) query.tenantIsSet(this.tenantIsSet);
        if (this.tenantSubQuery != null) query.tenantSubQuery(this.tenantSubQuery.enrich(queryFactory));
        this.enrichCommon(query);

        return query;
    }

    public InnerObjectDescriptionTemplateElasticQuery enrichElasticInner(QueryFactory queryFactory) {
        InnerObjectDescriptionTemplateElasticQuery query = queryFactory.query(InnerObjectDescriptionTemplateElasticQuery.class);
        if (this.ids != null) query.ids(this.ids);
        if (this.groupIds != null) query.groupIds(this.groupIds);
        if (this.versionStatuses != null) query.versionStatuses(this.versionStatuses);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.excludedGroupIds != null) query.excludedGroupIds(this.excludedGroupIds);

        if (this.like != null) throw new UnsupportedOperationException("");
        if (this.isActive != null && this.isActive.stream().anyMatch(x->x.equals(IsActive.Inactive))) throw new UnsupportedOperationException("");
        if (this.statuses != null) throw new UnsupportedOperationException("");
        if (this.typeIds != null) query.typeIds(this.typeIds);
        if (this.versions != null) throw new UnsupportedOperationException("");
        if (this.onlyCanEdit != null) throw new UnsupportedOperationException("");

        this.enrichCommon(query);

        return query;
    }
}
