package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum DescriptionValidationOutput implements DatabaseEnum<Short> {

    Valid((short) 1),
    Invalid((short) 2);

    private final Short value;

    DescriptionValidationOutput(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, DescriptionValidationOutput> map = EnumUtils.getEnumValueMap(DescriptionValidationOutput.class);

    public static DescriptionValidationOutput of(Short i) {
        return map.get(i);
    }

}
