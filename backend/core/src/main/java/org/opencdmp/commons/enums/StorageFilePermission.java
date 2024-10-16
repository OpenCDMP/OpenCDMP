package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import org.opencdmp.data.converters.enums.DatabaseEnum;

import java.util.Map;

public enum StorageFilePermission implements DatabaseEnum<Short> {

    Read((short) 0),
    Write((short) 1);

    private final Short value;

    StorageFilePermission(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return value;
    }

    private static final Map<Short, StorageFilePermission> map = EnumUtils.getEnumValueMap(StorageFilePermission.class);

    public static StorageFilePermission of(Short i) {
        return map.get(i);
    }

}
