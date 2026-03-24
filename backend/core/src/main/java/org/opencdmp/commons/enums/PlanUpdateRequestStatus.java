package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum PlanUpdateRequestStatus implements DatabaseEnum<Short> {

    Pending((short) 0),
    Refused((short) 1),
    Accepted((short) 2);

    private final Short value;

    PlanUpdateRequestStatus(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, PlanUpdateRequestStatus> map = EnumUtils.getEnumValueMap(PlanUpdateRequestStatus.class);

    public static PlanUpdateRequestStatus of(Short i) {
        return map.get(i);
    }


}
