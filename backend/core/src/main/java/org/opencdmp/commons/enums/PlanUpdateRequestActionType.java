package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum PlanUpdateRequestActionType implements DatabaseEnum<Short> {

    None((short) 0),
    CreatePlan((short) 1),
    UpdatePlan((short) 2);

    private final Short value;

    PlanUpdateRequestActionType(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, PlanUpdateRequestActionType> map = EnumUtils.getEnumValueMap(PlanUpdateRequestActionType.class);

    public static PlanUpdateRequestActionType of(Short i) {
        return map.get(i);
    }

}
