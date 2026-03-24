package org.opencdmp.commons.enums.rdamadmp;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum GrantIdType implements DatabaseEnum<String> {

    URL("url"),
    OTHER("other");
    private final String value;

    GrantIdType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    private static final Map<String, GrantIdType> map = EnumUtils.getEnumValueMap(GrantIdType.class);

    public static GrantIdType of(String i) {
        return map.get(i);
    }

}
