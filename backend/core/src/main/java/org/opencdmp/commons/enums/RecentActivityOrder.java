package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum RecentActivityOrder implements DatabaseEnum<Short> {
    UpdatedAt((short) 0),
    Label((short) 1),
    Status((short) 2);

    private final Short value;

    RecentActivityOrder(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, RecentActivityOrder> map = EnumUtils.getEnumValueMap(RecentActivityOrder.class);

    public static RecentActivityOrder of(Short i) {
        return map.get(i);
    }

}
