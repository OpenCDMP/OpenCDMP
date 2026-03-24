package org.opencdmp.commons.enums.rdamadmp;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum FunderIdType implements DatabaseEnum<String> {

    FUNDEREF("fundref"),
    URL("url"),
    OTHER("other");
    private final String value;

    FunderIdType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return this.value;
    }

    private static final Map<String, FunderIdType> map = EnumUtils.getEnumValueMap(FunderIdType.class);

    public static FunderIdType of(String i) {
        return map.get(i);
    }

}
