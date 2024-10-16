package org.opencdmp.model;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.query.UserQuery;
import org.opencdmp.query.lookup.UserRoleLookup;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class PrefillingLookup extends Lookup {
    private String like;

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

}
