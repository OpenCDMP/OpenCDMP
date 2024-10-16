package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum StorageType implements DatabaseEnum<Short> {

    Temp((short) 0),
    Main((short) 1),
    Transformer((short)2),
    Deposit((short)3);

    private final Short value;

    StorageType(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, StorageType> map = EnumUtils.getEnumValueMap(StorageType.class);

    public static StorageType of(Short i) {
        return map.get(i);
    }

}
