package org.opencdmp.query.lookup;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.SupportiveMaterialFieldType;
import org.opencdmp.query.SupportiveMaterialQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class SupportiveMaterialLookup extends Lookup {

    private String like;

    private List<IsActive> isActive;

    private List<SupportiveMaterialFieldType> types;

    private List<String> languageCodes;

    private List<UUID> ids;

    private List<UUID> excludedIds;

    private List<UUID> tenantIds;

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

    public List<SupportiveMaterialFieldType> getTypes() {
        return types;
    }

    public void setTypes(List<SupportiveMaterialFieldType> types) {
        this.types = types;
    }

    public List<String> getLanguageCodes() {
        return languageCodes;
    }

    public void setLanguageCodes(List<String> languageCodes) {
        this.languageCodes = languageCodes;
    }

    public List<UUID> getTenantIds() {
        return tenantIds;
    }

    public void setTenantIds(List<UUID> tenantIds) {
        this.tenantIds = tenantIds;
    }

    public SupportiveMaterialQuery enrich(QueryFactory queryFactory) {
        SupportiveMaterialQuery query = queryFactory.query(SupportiveMaterialQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.types != null) query.types(this.types);
        if (this.languageCodes != null) query.languageCodes(this.languageCodes);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.tenantIds != null) query.tenantIds(this.tenantIds);

        this.enrichCommon(query);

        return query;
    }

}
