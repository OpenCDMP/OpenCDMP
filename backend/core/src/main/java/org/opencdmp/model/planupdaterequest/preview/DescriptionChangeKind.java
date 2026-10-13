package org.opencdmp.model.planupdaterequest.preview;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum DescriptionChangeKind implements DatabaseEnum<Short> {

    UPDATE((short) 0),
    ADD((short) 1),
    DELETE((short) 2);

    private final Short value;

    DescriptionChangeKind(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, DescriptionChangeKind> map = EnumUtils.getEnumValueMap(DescriptionChangeKind.class);

    public static DescriptionChangeKind of(Short i) {
        return map.get(i);
    }
}
