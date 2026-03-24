package org.opencdmp.commons.enums.rdamadmp;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum ContactIdType implements DatabaseEnum<String> {

    ORCID("orcid"),
    ISNI("isni"),
    OPENID("openid"),
    OTHER("other");
    private final String value;

    ContactIdType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    private static final Map<String, ContactIdType> map = EnumUtils.getEnumValueMap(ContactIdType.class);

    public static ContactIdType of(String i) {
        return map.get(i);
    }

}
