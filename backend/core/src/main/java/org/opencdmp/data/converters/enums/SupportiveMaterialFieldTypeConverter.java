package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.SupportiveMaterialFieldType;
import jakarta.persistence.Converter;

@Converter
public class SupportiveMaterialFieldTypeConverter extends DatabaseEnumConverter<SupportiveMaterialFieldType, Short> {
    public SupportiveMaterialFieldType of(Short i) {
        return SupportiveMaterialFieldType.of(i);
    }
}
