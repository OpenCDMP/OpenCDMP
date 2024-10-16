package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum PlanValidationOutput implements DatabaseEnum<Short> {

    Valid((short) 1),
    Invalid((short) 2);

    private final Short value;

    PlanValidationOutput(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, PlanValidationOutput> map = EnumUtils.getEnumValueMap(PlanValidationOutput.class);

    public static PlanValidationOutput of(Short i) {
        return map.get(i);
    }

}
