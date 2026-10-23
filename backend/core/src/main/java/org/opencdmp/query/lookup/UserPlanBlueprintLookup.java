package org.opencdmp.query.lookup;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UserPlanBlueprintRole;
import org.opencdmp.query.UserPlanBlueprintQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class UserPlanBlueprintLookup extends Lookup {

    private List<IsActive> isActive;

    private List<UserPlanBlueprintRole> roles;

    private List<UUID> ids;

    private List<UUID> userIds;
    private List<UUID> planBlueprintIds;

    private List<UUID> excludedIds;

    public List<IsActive> getIsActive() {
        return isActive;
    }

    public void setIsActive(List<IsActive> isActive) {
        this.isActive = isActive;
    }

    public List<UserPlanBlueprintRole> getRoles() {
        return roles;
    }

    public void setRoles(List<UserPlanBlueprintRole> roles) {
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

    public List<UUID> getPlanBlueprintIds() {
        return planBlueprintIds;
    }

    public void setPlanBlueprintIds(List<UUID> planBlueprintIds) {
        this.planBlueprintIds = planBlueprintIds;
    }

    public List<UUID> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<UUID> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public UserPlanBlueprintQuery enrich(QueryFactory queryFactory) {
        UserPlanBlueprintQuery query = queryFactory.query(UserPlanBlueprintQuery.class);
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
        if (this.planBlueprintIds != null)
            query.planBlueprintIds(this.planBlueprintIds);

        this.enrichCommon(query);

        return query;
    }

}
