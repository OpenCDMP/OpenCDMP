package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum UserPlanBlueprintRole implements DatabaseEnum<Short> {

    Owner((short) 0),
    Member((short) 1);

    private final Short value;

    UserPlanBlueprintRole(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, UserPlanBlueprintRole> map = EnumUtils.getEnumValueMap(UserPlanBlueprintRole.class);

    public static UserPlanBlueprintRole of(Short i) {
        return map.get(i);
    }

}
