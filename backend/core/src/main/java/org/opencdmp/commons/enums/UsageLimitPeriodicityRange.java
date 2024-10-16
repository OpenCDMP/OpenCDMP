package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum UsageLimitPeriodicityRange implements DatabaseEnum<Short> {

    Monthly((short) 0),
    Yearly((short) 1);

    private final Short value;

    UsageLimitPeriodicityRange(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, UsageLimitPeriodicityRange> map = EnumUtils.getEnumValueMap(UsageLimitPeriodicityRange.class);

    public static UsageLimitPeriodicityRange of(Short i) {
        return map.get(i);
    }

}
