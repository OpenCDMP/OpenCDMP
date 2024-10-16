package org.opencdmp.query.lookup;

import org.opencdmp.commons.enums.UserSettingsType;
import org.opencdmp.query.UserSettingsQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class UserSettingsLookup extends Lookup {

    private String like;
    private List<UUID> ids;
    private List<UserSettingsType> userSettingsTypes;

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

    public List<UserSettingsType> getUserSettingsTypes() {
        return userSettingsTypes;
    }

    public void setUserSettingsTypes(List<UserSettingsType> userSettingsTypes) {
        this.userSettingsTypes = userSettingsTypes;
    }

    public UserSettingsQuery enrich(QueryFactory queryFactory) {
        UserSettingsQuery query = queryFactory.query(UserSettingsQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.ids != null) query.ids(this.ids);
        if (this.userSettingsTypes != null) query.userSettingsTypes(this.userSettingsTypes);

        this.enrichCommon(query);

        return query;
    }

}
