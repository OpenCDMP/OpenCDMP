package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum DescriptionStatus implements DatabaseEnum<Short> {

    Draft((short) 0),
    Finalized((short) 1),
    Canceled((short) 2);

    private final Short value;

    DescriptionStatus(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, DescriptionStatus> map = EnumUtils.getEnumValueMap(DescriptionStatus.class);

    public static DescriptionStatus of(Short i) {
        return map.get(i);
    }

}
