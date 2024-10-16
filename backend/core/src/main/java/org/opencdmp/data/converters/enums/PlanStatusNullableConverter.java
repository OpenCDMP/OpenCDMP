package org.opencdmp.data.converters.enums;

import jakarta.persistence.Converter;
import org.opencdmp.commons.enums.PlanStatus;

@Converter
public class PlanStatusNullableConverter extends DatabaseEnumConverter<PlanStatus, Short> {

    @Override
    protected PlanStatus of(Short i) {
        return PlanStatus.of(i);
    }

    @Override
    public Short convertToDatabaseColumn(PlanStatus value) {
        if (value == null)  return null;
        return value.getValue();
    }

    @Override
    public PlanStatus convertToEntityAttribute(Short dbData) {
        return dbData == null ? null : this.of(dbData);
    }
}