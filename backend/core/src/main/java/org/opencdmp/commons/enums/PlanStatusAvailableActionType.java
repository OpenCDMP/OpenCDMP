package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum PlanStatusAvailableActionType implements DatabaseEnum<Short> {

    Deposit((short) 0),
    Export((short) 1),
    Evaluate((short) 2);

    private final Short value;

    PlanStatusAvailableActionType(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return this.value;
    }

    private static final Map<Short, PlanStatusAvailableActionType> map = EnumUtils.getEnumValueMap(PlanStatusAvailableActionType.class);

    public static PlanStatusAvailableActionType of(Short i) {
        return map.get(i);
    }

}
