package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum PlanAccessType implements DatabaseEnum<Short> {

    Public((short) 0), Restricted((short) 1);

    private final Short value;

    PlanAccessType(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, PlanAccessType> map = EnumUtils.getEnumValueMap(PlanAccessType.class);

    public static PlanAccessType of(Short i) {
        return map.get(i);
    }

}
