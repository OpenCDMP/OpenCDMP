package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum PlanBlueprintSystemFieldType implements DatabaseEnum<Short> {

    Title((short)0),
    Description((short)1),
    Language((short)2),
    Contact((short)3),
    AccessRights((short)4),
    User((short)5);
    private final Short value;

    PlanBlueprintSystemFieldType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, PlanBlueprintSystemFieldType> map = EnumUtils.getEnumValueMap(PlanBlueprintSystemFieldType.class);

    public static PlanBlueprintSystemFieldType of(Short i) {
        return map.get(i);
    }
}