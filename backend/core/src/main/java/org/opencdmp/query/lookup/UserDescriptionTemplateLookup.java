package org.opencdmp.query.lookup;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UserDescriptionTemplateRole;
import org.opencdmp.query.UserDescriptionTemplateQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class UserDescriptionTemplateLookup extends Lookup {

    private List<IsActive> isActive;

    private List<UserDescriptionTemplateRole> roles;

    private List<UUID> ids;

    private List<UUID> userIds;
    private List<UUID> descriptionTemplateIds;

    private List<UUID> excludedIds;

    public List<IsActive> getIsActive() {
        return isActive;
    }

    public void setIsActive(List<IsActive> isActive) {
        this.isActive = isActive;
    }

    public List<UserDescriptionTemplateRole> getRoles() {
        return roles;
    }

    public void setRoles(List<UserDescriptionTemplateRole> roles) {
        this.roles = roles;
    }

    public List<UUID> getIds() {
        return ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<UUID> userIds) {
        this.userIds = userIds;
    }

    public List<UUID> getDescriptionTemplateIds() {
        return descriptionTemplateIds;
    }

    public void setDescriptionTemplateIds(List<UUID> descriptionTemplateIds) {
        this.descriptionTemplateIds = descriptionTemplateIds;
    }

    public List<UUID> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<UUID> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public UserDescriptionTemplateQuery enrich(QueryFactory queryFactory) {
        UserDescriptionTemplateQuery query = queryFactory.query(UserDescriptionTemplateQuery.class);
        if (this.isActive != null)
            query.isActive(this.isActive);
        if (this.roles != null)
            query.roles(this.roles);
        if (this.ids != null)
            query.ids(this.ids);
        if (this.excludedIds != null)
            query.excludedIds(this.excludedIds);
        if (this.userIds != null)
            query.userIds(this.userIds);
        if (this.descriptionTemplateIds != null)
            query.userIds(this.descriptionTemplateIds);

        this.enrichCommon(query);

        return query;
    }

}
