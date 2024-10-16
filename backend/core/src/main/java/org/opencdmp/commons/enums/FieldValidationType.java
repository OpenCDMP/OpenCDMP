package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum FieldValidationType implements DatabaseEnum<Short> {

    None((short) 0), 
    Required((short) 1), 
    Url((short) 2);

    private final Short value;

    FieldValidationType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, FieldValidationType> map = EnumUtils.getEnumValueMap(FieldValidationType.class);

    public static FieldValidationType of(Short i) {
        return map.get(i);
    }

}