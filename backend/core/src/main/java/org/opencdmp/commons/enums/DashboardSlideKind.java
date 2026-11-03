package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum DashboardSlideKind implements DatabaseEnum<Short> {

    Content((short) 0),
    Recommended((short) 1);

    private final Short value;

    DashboardSlideKind(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, DashboardSlideKind> map = EnumUtils.getEnumValueMap(DashboardSlideKind.class);

    public static DashboardSlideKind of(Short i) {
        return map.get(i);
    }

}
