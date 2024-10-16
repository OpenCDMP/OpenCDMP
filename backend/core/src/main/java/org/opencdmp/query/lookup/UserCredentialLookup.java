package org.opencdmp.query.lookup;

import org.opencdmp.query.UserCredentialQuery;
import org.opencdmp.query.UserRoleQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class UserCredentialLookup extends Lookup {
    private List<UUID> ids;

    private List<UUID> excludedIds;
    private List<UUID> userIds;
    private List<String> externalIds;


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

    public List<String> getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(List<String> externalIds) {
        this.externalIds = externalIds;
    }

    public UserCredentialQuery enrich(QueryFactory queryFactory) {
        UserCredentialQuery query = queryFactory.query(UserCredentialQuery.class);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.userIds != null) query.userIds(this.userIds);
        if (this.externalIds != null) query.externalIds(this.externalIds);

        this.enrichCommon(query);

        return query;
    }

}
