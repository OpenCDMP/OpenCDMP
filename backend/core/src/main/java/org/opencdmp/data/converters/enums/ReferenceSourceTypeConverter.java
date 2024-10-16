package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.ReferenceSourceType;
import jakarta.persistence.Converter;

@Converter
public class ReferenceSourceTypeConverter extends DatabaseEnumConverter<ReferenceSourceType, Short>{
    public ReferenceSourceType of(Short i) {
        return ReferenceSourceType.of(i);
    }
}
