package org.opencdmp.commons.enums.annotation;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum AnnotationProtectionType implements DatabaseEnum<Short> {

    Private((short) 0),
    EntityAccessors((short) 1);

    private final Short value;

    AnnotationProtectionType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, AnnotationProtectionType> map = EnumUtils.getEnumValueMap(AnnotationProtectionType.class);

    public static AnnotationProtectionType of(Short i) {
        return map.get(i);
    }

}
