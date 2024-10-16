package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum DescriptionTemplateTypeStatus implements DatabaseEnum<Short> {

    Draft((short) 0), 
    Finalized((short) 1);

    private final Short value;

    DescriptionTemplateTypeStatus(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, DescriptionTemplateTypeStatus> map = EnumUtils.getEnumValueMap(DescriptionTemplateTypeStatus.class);

    public static DescriptionTemplateTypeStatus of(Short i) {
        return map.get(i);
    }

}
