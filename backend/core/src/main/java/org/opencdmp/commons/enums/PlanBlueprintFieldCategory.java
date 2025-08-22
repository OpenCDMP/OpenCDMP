package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum PlanBlueprintFieldCategory implements DatabaseEnum<Short> {
    System((short) 0),
    Extra((short) 1),
    ReferenceType((short) 2),
    Upload((short) 3);

    public static class Names {
        public static final String System = "system";
        public static final String Extra = "extra";
        public static final String ReferenceType = "referenceType";
        public static final String Upload = "upload";
    }

    private final Short value;

    PlanBlueprintFieldCategory(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, PlanBlueprintFieldCategory> map = EnumUtils.getEnumValueMap(PlanBlueprintFieldCategory.class);

    public static PlanBlueprintFieldCategory of(Short i) {
        return map.get(i);
    }
}
