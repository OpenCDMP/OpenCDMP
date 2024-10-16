package org.opencdmp.query.lookup;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.TenantConfigurationType;
import org.opencdmp.query.TenantConfigurationQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class TenantConfigurationLookup extends Lookup {

    private List<IsActive> isActive;
    private List<TenantConfigurationType> types;
    private List<UUID> ids;
    private List<UUID> tenantIds;
    private List<UUID> excludedIds;
    private Boolean tenantIsSet;


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

    public List<TenantConfigurationType> getTypes() {
        return types;
    }

    public void setTypes(List<TenantConfigurationType> types) {
        this.types = types;
    }

    public List<UUID> getTenantIds() {
        return tenantIds;
    }

    public void setTenantIds(List<UUID> tenantIds) {
        this.tenantIds = tenantIds;
    }

    public Boolean getTenantIsSet() {
        return tenantIsSet;
    }

    public void setTenantIsSet(Boolean tenantIsSet) {
        this.tenantIsSet = tenantIsSet;
    }

    public TenantConfigurationQuery enrich(QueryFactory queryFactory) {
        TenantConfigurationQuery query = queryFactory.query(TenantConfigurationQuery.class);
        if (this.types != null) query.types(this.types);
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.tenantIds != null) query.tenantIds(this.tenantIds);
        if (this.tenantIsSet != null) query.tenantIsSet(this.tenantIsSet);

        this.enrichCommon(query);

        return query;
    }
}
