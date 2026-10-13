package org.opencdmp.query.lookup;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.SQLUtils;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.ReferenceSourceType;
import org.opencdmp.query.ReferenceQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ReferenceLookup extends Lookup {

    @Schema(description = SwaggerHelpers.Reference.like_description)
    private String like;

    @Schema(description = SwaggerHelpers.Reference.isActive_description)
    private List<IsActive> isActive;

    @Schema(description = SwaggerHelpers.Reference.sourceTypes_description)
    private Collection<ReferenceSourceType> sourceTypes;

    @Schema(description = SwaggerHelpers.Reference.typeIds_description)
    private Collection<UUID> typeIds;

    @Schema(description = SwaggerHelpers.Reference.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.Reference.excludeIds_description)
    private List<UUID> excludedIds;

    private TenantLookup tenantSubQuery;

    /** false: only the default tenant's shared rows (no tenant); true: only tenant-owned rows; null: both. */
    private Boolean tenantIsSet;

    /** true: only entries a current plan or asset uses. */
    private Boolean isUsed;

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

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<UUID> excludeIds) {
        this.excludedIds = excludeIds;
    }

    public Collection<ReferenceSourceType> getSourceTypes() {
        return sourceTypes;
    }

    public void setSourceTypes(Collection<ReferenceSourceType> sourceTypes) {
        this.sourceTypes = sourceTypes;
    }

    public Collection<UUID> getTypeIds() {
        return typeIds;
    }

    public void setTypeIds(Collection<UUID> typeIds) {
        this.typeIds = typeIds;
    }

    public Boolean getIsUsed() { return isUsed; }
    public void setIsUsed(Boolean isUsed) { this.isUsed = isUsed; }

    public Boolean getTenantIsSet() { return tenantIsSet; }
    public void setTenantIsSet(Boolean tenantIsSet) { this.tenantIsSet = tenantIsSet; }

    public TenantLookup getTenantSubQuery() { return tenantSubQuery; }
    public void setTenantSubQuery(TenantLookup tenantSubQuery) { this.tenantSubQuery = tenantSubQuery; }

    public ReferenceQuery enrich(QueryFactory queryFactory) {
        ReferenceQuery query = queryFactory.query(ReferenceQuery.class);
        if (this.like != null) query.like(SQLUtils.toPostgres(this.like));
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.typeIds != null) query.typeIds(this.typeIds);
        if (this.sourceTypes != null) query.sourceTypes(this.sourceTypes);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.tenantIsSet != null) query.tenantIsSet(this.tenantIsSet);
        if (this.isUsed != null) query.isUsed(this.isUsed);
        if (this.tenantSubQuery != null) query.tenantSubQuery(this.tenantSubQuery.enrich(queryFactory));

        this.enrichCommon(query);

        return query;
    }

}
