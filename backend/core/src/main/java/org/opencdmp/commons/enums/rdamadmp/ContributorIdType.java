package org.opencdmp.commons.enums.rdamadmp;

import com.fasterxml.jackson.annotation.JsonValue;
import gr.cite.tools.exception.MyApplicationException;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.HashMap;
import java.util.Map;

public enum ContributorIdType implements DatabaseEnum<String> {

    ORCID("orcid"),
    ISNI("isni"),
    OPENID("openid"),
    OTHER("other");
    private final String value;
    private final static Map<String, ContributorIdType> CONSTANTS = new HashMap<String, ContributorIdType>();

    ContributorIdType(String value) {
        this.value = value;
    }


    static {
        for (ContributorIdType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    private static final Map<String, ContributorIdType> map = EnumUtils.getEnumValueMap(ContributorIdType.class);

    public static ContributorIdType of(String i) {
        return map.get(i);
    }

    public static ContributorIdType fromValue(String value) {
        ContributorIdType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new MyApplicationException(value);
        } else {
            return constant;
        }
    }
}
