package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum WSActionType implements DatabaseEnum<Short> {

    PlanJoin((short) 0),
    PlanLeave((short) 1),
    PlanUserAction((short) 2);

    private final Short value;

    WSActionType(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return this.value;
    }

    private static final Map<Short, WSActionType> map = EnumUtils.getEnumValueMap(WSActionType.class);

    public static WSActionType of(Short i) {
        return map.get(i);
    }


}
