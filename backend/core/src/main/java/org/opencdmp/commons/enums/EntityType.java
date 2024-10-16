package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum EntityType implements DatabaseEnum<Short> {

    Plan((short) 0);

    private final Short value;

    EntityType(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, EntityType> map = EnumUtils.getEnumValueMap(EntityType.class);

    public static EntityType of(Short i) {
        return map.get(i);
    }

}
