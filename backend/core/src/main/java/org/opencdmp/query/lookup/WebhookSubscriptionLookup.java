package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.commons.SQLUtils;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.WebhookSubscriptionQuery;

import java.util.List;
import java.util.UUID;

public class WebhookSubscriptionLookup extends Lookup {

    private String like;
    private List<IsActive> isActive;
    private List<UUID> ids;
    private Boolean enabled;

    private TenantLookup tenantSubQuery;

    /** false: only the default tenant's shared rows (no tenant); true: only tenant-owned rows; null: both. */
    private Boolean tenantIsSet;

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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getTenantIsSet() { return tenantIsSet; }
    public void setTenantIsSet(Boolean tenantIsSet) { this.tenantIsSet = tenantIsSet; }

    public TenantLookup getTenantSubQuery() { return tenantSubQuery; }
    public void setTenantSubQuery(TenantLookup tenantSubQuery) { this.tenantSubQuery = tenantSubQuery; }

    public WebhookSubscriptionQuery enrich(QueryFactory queryFactory) {
        WebhookSubscriptionQuery query = queryFactory.query(WebhookSubscriptionQuery.class);
        if (this.like != null) query.like(SQLUtils.toPostgres(this.like));
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.ids != null) query.ids(this.ids);
        if (this.enabled != null) query.enabled(this.enabled);
        if (this.tenantIsSet != null) query.tenantIsSet(this.tenantIsSet);
        if (this.tenantSubQuery != null) query.tenantSubQuery(this.tenantSubQuery.enrich(queryFactory));

        this.enrichCommon(query);

        return query;
    }
}
