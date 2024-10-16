package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum RecentActivityItemType implements DatabaseEnum<Short> {

    Plan((short) 0),
    Description((short) 1);

    private final Short value;

    RecentActivityItemType(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, RecentActivityItemType> map = EnumUtils.getEnumValueMap(RecentActivityItemType.class);

    public static RecentActivityItemType of(Short i) {
        return map.get(i);
    }

}
