package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.StorageType;
import jakarta.persistence.Converter;

@Converter
public class StorageTypeConverter extends DatabaseEnumConverter<StorageType, Short> {
    public StorageType of(Short i) {
        return StorageType.of(i);
    }
}
