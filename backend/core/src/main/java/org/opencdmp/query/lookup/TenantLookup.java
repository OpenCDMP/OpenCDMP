package org.opencdmp.query.lookup;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.TenantQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class TenantLookup extends Lookup {

    private String like;
    private List<IsActive> isActive;
    private List<UUID> ids;
    private List<String> codes;
    private List<UUID> excludedIds;

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

    public void setIds(List<UUID> ids) { this.ids = ids; }

    public List<String> getCodes() { return codes; }

    public void setCodes(List<String> codes) { this.codes = codes; }

    public List<UUID> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<UUID> excludeIds) {
        this.excludedIds = excludeIds;
    }

    public TenantQuery enrich(QueryFactory queryFactory) {
        TenantQuery query = queryFactory.query(TenantQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.ids != null) query.ids(this.ids);
        if (this.codes != null) query.codes(this.codes);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);

        this.enrichCommon(query);

        return query;
    }
}
