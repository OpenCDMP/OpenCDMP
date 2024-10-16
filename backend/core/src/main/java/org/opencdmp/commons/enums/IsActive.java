package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum IsActive implements DatabaseEnum<Short> {

    Inactive((short) 0),
    Active((short) 1);

    private final Short value;

    IsActive(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, IsActive> map = EnumUtils.getEnumValueMap(IsActive.class);

    public static IsActive of(Short i) {
        return map.get(i);
    }

}
