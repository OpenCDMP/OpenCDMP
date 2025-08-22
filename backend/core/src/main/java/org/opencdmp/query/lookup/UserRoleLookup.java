package org.opencdmp.query.lookup;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.UserQuery;
import org.opencdmp.query.UserRoleQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.query.lookup.swagger.SwaggerHelpers;

import java.util.List;
import java.util.UUID;

public class UserRoleLookup extends Lookup {

    @Schema(description = SwaggerHelpers.UserRole.ids_description)
    private List<UUID> ids;

    @Schema(description = SwaggerHelpers.UserRole.excludeIds_description)
    private List<UUID> excludedIds;

    @Schema(description = SwaggerHelpers.UserRole.userIds_description)
    private List<UUID> userIds;

    @Schema(description = SwaggerHelpers.UserRole.roles_description)
    private List<String> roles;


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

    public List<UUID> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<UUID> userIds) {
        this.userIds = userIds;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public UserRoleQuery enrich(QueryFactory queryFactory) {
        UserRoleQuery query = queryFactory.query(UserRoleQuery.class);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.userIds != null) query.userIds(this.userIds);
        if (this.roles != null) query.roles(this.roles);

        this.enrichCommon(query);

        return query;
    }

}
