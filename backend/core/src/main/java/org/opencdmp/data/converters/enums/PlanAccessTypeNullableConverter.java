package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.PlanAccessType;
import jakarta.persistence.Converter;

@Converter
public class PlanAccessTypeNullableConverter extends DatabaseEnumConverter<PlanAccessType, Short> {

    @Override
    protected PlanAccessType of(Short i) {
        return PlanAccessType.of(i);
    }


    @Override
    public Short convertToDatabaseColumn(PlanAccessType value) {
        if (value == null)  return null;
        return value.getValue();
    }

    @Override
    public PlanAccessType convertToEntityAttribute(Short dbData) {
        return dbData == null ? null : this.of(dbData);
    }
}
