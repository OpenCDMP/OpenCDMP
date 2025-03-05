package org.opencdmp.query.lookup;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.LanguageQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class LanguageLookup extends Lookup {

    private String like;

    private List<IsActive> isActive;

    private List<String> codes;

    private List<UUID> ids;

    private List<UUID> excludedIds;

    private TenantLookup tenantSubQuery;

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

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

    public TenantLookup getTenantSubQuery() {
        return tenantSubQuery;
    }

    public void setTenantSubQuery(TenantLookup tenantSubQuery) {
        this.tenantSubQuery = tenantSubQuery;
    }

    public LanguageQuery enrich(QueryFactory queryFactory) {
        LanguageQuery query = queryFactory.query(LanguageQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.codes != null) query.codes(this.codes);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.tenantSubQuery != null) query.tenantSubQuery(this.tenantSubQuery.enrich(queryFactory));

        this.enrichCommon(query);

        return query;
    }

}
