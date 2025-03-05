package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum DescriptionStatusAvailableActionType implements DatabaseEnum<Short> {

    Export((short) 0);

    private final Short value;

    DescriptionStatusAvailableActionType(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return this.value;
    }

    private static final Map<Short, DescriptionStatusAvailableActionType> map = EnumUtils.getEnumValueMap(DescriptionStatusAvailableActionType.class);

    public static DescriptionStatusAvailableActionType of(Short i) {
        return map.get(i);
    }

}
