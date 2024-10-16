package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.LockTargetType;
import jakarta.persistence.Converter;

@Converter
public class LockTargetTypeConverter extends DatabaseEnumConverter<LockTargetType, Short>{
    protected LockTargetType of(Short i) {
        return LockTargetType.of(i);
    }
}
