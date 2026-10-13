package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum PlanUpdateRequestApplyDecision implements DatabaseEnum<Short> {

    Accept((short) 0),
    Refuse((short) 1),
    PartialAccept((short) 2);

    private final Short value;

    PlanUpdateRequestApplyDecision(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, PlanUpdateRequestApplyDecision> map = EnumUtils.getEnumValueMap(PlanUpdateRequestApplyDecision.class);

    public static PlanUpdateRequestApplyDecision of(Short i) {
        return map.get(i);
    }
}
