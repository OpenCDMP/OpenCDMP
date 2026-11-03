package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum DashboardSlideButtonType implements DatabaseEnum<Short> {

    Url((short) 0),
    Email((short) 1);

    private final Short value;

    DashboardSlideButtonType(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, DashboardSlideButtonType> map = EnumUtils.getEnumValueMap(DashboardSlideButtonType.class);

    public static DashboardSlideButtonType of(Short i) {
        return map.get(i);
    }

}
