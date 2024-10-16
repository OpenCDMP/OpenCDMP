package org.opencdmp.query.lookup;

import org.opencdmp.commons.enums.ContactInfoType;
import org.opencdmp.query.UserContactInfoQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class UserContantInfoLookup extends Lookup {
    private List<UUID> ids;
    private List<UUID> excludedIds;
    private List<UUID> userIds;
    private List<String> values;
    private List<ContactInfoType> types;


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

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public List<ContactInfoType> getTypes() {
        return types;
    }

    public void setTypes(List<ContactInfoType> types) {
        this.types = types;
    }

    public UserContactInfoQuery enrich(QueryFactory queryFactory) {
        UserContactInfoQuery query = queryFactory.query(UserContactInfoQuery.class);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.userIds != null) query.userIds(this.userIds);
        if (this.values != null) query.values(this.values);
        if (this.types != null) query.types(this.types);

        this.enrichCommon(query);

        return query;
    }

}
