package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum PlanBlueprintVersionStatus implements DatabaseEnum<Short> {

    Current((short) 0),
    Previous((short) 1),
    NotFinalized((short) 2);

    private final Short value;

    PlanBlueprintVersionStatus(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, PlanBlueprintVersionStatus> map = EnumUtils.getEnumValueMap(PlanBlueprintVersionStatus.class);

    public static PlanBlueprintVersionStatus of(Short i) {
        return map.get(i);
    }

}
