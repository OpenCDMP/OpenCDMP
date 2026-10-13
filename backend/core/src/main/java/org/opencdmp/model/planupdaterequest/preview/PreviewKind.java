package org.opencdmp.model.planupdaterequest.preview;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.commons.enums.EnumUtils;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum PreviewKind implements DatabaseEnum<Short> {

    CREATE((short) 0),
    UPDATE((short) 1);

    private final Short value;

    PreviewKind(Short value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, PreviewKind> map = EnumUtils.getEnumValueMap(PreviewKind.class);

    public static PreviewKind of(Short i) {
        return map.get(i);
    }
}
