package org.opencdmp.query.lookup;

import org.opencdmp.commons.enums.DescriptionTemplateTypeStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.DescriptionTemplateTypeQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class DescriptionTemplateTypeLookup extends Lookup {

    private String like;

    private List<IsActive> isActive;

    private List<DescriptionTemplateTypeStatus> statuses;

    private List<UUID> ids;

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

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<UUID> excludeIds) {
        this.excludedIds = excludeIds;
    }

    public List<DescriptionTemplateTypeStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<DescriptionTemplateTypeStatus> statuses) {
        this.statuses = statuses;
    }

    public DescriptionTemplateTypeQuery enrich(QueryFactory queryFactory) {
        DescriptionTemplateTypeQuery query = queryFactory.query(DescriptionTemplateTypeQuery.class);
        if (this.like != null)
            query.like(this.like);
        if (this.isActive != null)
            query.isActive(this.isActive);
        if (this.statuses != null)
            query.statuses(this.statuses);
        if (this.ids != null)
            query.ids(this.ids);
        if (this.excludedIds != null)
            query.excludedIds(this.excludedIds);

        this.enrichCommon(query);

        return query;
    }

}
