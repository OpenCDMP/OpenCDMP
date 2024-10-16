package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.PrefillingSourceQuery;

import java.util.List;
import java.util.UUID;

public class PrefillingSourceLookup extends Lookup {

    private String like;

    private List<IsActive> isActive;

    private List<UUID> ids;

    private List<UUID> excludedIds;

    private List<String> codes;

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

    public List<UUID> getIds() {
        return this.ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getExcludedIds() {
        return this.excludedIds;
    }

    public void setExcludedIds(List<UUID> excludeIds) {
        this.excludedIds = excludeIds;
    }


    public PrefillingSourceQuery enrich(QueryFactory queryFactory) {
        PrefillingSourceQuery query = queryFactory.query(PrefillingSourceQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.codes != null) query.codes(this.codes);
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);

        this.enrichCommon(query);

        return query;
    }

}
