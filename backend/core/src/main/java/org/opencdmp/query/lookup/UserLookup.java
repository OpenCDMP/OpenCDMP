package org.opencdmp.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.UserQuery;

import java.util.List;
import java.util.UUID;

public class UserLookup extends Lookup {

    private String like;

    private List<UUID> ids;

    private List<String> emails;

    private List<UUID> excludedIds;

    private List<IsActive> isActive;

    private UserRoleLookup userRoleSubQuery;

    private TenantUserLookup tenantUserSubQuery;

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

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public UserRoleLookup getUserRoleSubQuery() {
        return userRoleSubQuery;
    }

    public void setUserRoleSubQuery(UserRoleLookup userRoleSubQuery) {
        this.userRoleSubQuery = userRoleSubQuery;
    }

    public TenantUserLookup getTenantUserSubQuery() {
        return tenantUserSubQuery;
    }

    public void setTenantUserSubQuery(TenantUserLookup tenantUserSubQuery) {
        this.tenantUserSubQuery = tenantUserSubQuery;
    }

    public UserQuery enrich(QueryFactory queryFactory) {
        UserQuery query = queryFactory.query(UserQuery.class);
        if (this.like != null)
            query.like(this.like);
        if (this.ids != null)
            query.ids(this.ids);
        if (this.emails != null)
            query.emails(this.emails);
        if (this.userRoleSubQuery != null)
            query.userRoleSubQuery(this.userRoleSubQuery.enrich(queryFactory));
        if (this.tenantUserSubQuery != null)
            query.tenantUserSubQuery(this.tenantUserSubQuery.enrich(queryFactory));
        if (this.excludedIds != null)
            query.excludedIds(this.excludedIds);
        if (this.isActive != null)
            query.isActive(this.isActive);

        this.enrichCommon(query);

        return query;
    }

}
