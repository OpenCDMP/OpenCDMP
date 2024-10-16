package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum UserDescriptionTemplateRole implements DatabaseEnum<Short> {

    Owner((short) 0),
    Member((short) 1);

    private final Short value;

    UserDescriptionTemplateRole(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, UserDescriptionTemplateRole> map = EnumUtils.getEnumValueMap(UserDescriptionTemplateRole.class);

    public static UserDescriptionTemplateRole of(Short i) {
        return map.get(i);
    }

}
