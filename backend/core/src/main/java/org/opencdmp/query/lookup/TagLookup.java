package org.opencdmp.query.lookup;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.TagQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class TagLookup extends Lookup {

    private String like;

    private List<UUID> ids;

    private List<UUID> excludedIds;

    private List<IsActive> isActive;

    private List<UUID> createdByIds;

    private List<String> tags;

    private List<String> excludedTags;

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
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

    public void setExcludedIds(List<UUID> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public List<IsActive> getIsActive() {
        return isActive;
    }

    public void setIsActive(List<IsActive> isActive) {
        this.isActive = isActive;
    }

    public List<UUID> getCreatedByIds() {
        return createdByIds;
    }

    public void setCreatedByIds(List<UUID> createdByIds) {
        this.createdByIds = createdByIds;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getExcludedTags() {
        return excludedTags;
    }

    public void setExcludedTags(List<String> excludedTags) {
        this.excludedTags = excludedTags;
    }

    public TagQuery enrich(QueryFactory queryFactory) {
        TagQuery query = queryFactory.query(TagQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.tags != null) query.tags(this.tags);
        if (this.excludedTags != null) query.excludedTags(this.excludedTags);
        if (this.isActive != null) query.isActive(this.isActive);
        if (this.createdByIds != null) query.createdByIds(this.createdByIds);

        this.enrichCommon(query);

        return query;
    }

}
