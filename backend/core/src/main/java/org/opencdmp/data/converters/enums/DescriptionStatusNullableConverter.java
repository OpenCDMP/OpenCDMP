package org.opencdmp.data.converters.enums;

import jakarta.persistence.Converter;
import org.opencdmp.commons.enums.DescriptionStatus;

@Converter
public class DescriptionStatusNullableConverter extends DatabaseEnumConverter<DescriptionStatus, Short> {

    @Override
    protected DescriptionStatus of(Short i) {
        return DescriptionStatus.of(i);
    }

    @Override
    public Short convertToDatabaseColumn(DescriptionStatus value) {
        if (value == null)  return null;
        return value.getValue();
    }

    @Override
    public DescriptionStatus convertToEntityAttribute(Short dbData) {
        return dbData == null ? null : this.of(dbData);
    }
}