package org.opencdmp.data.converters.enums;

import jakarta.persistence.Converter;
import org.opencdmp.commons.enums.rdamadmp.DmpIdType;

@Converter
public class DmpIdTypeConverter extends DatabaseEnumConverter<DmpIdType, String> {

    @Override
    protected DmpIdType of(String value) {
        return DmpIdType.of(value);
    }

    @Override
    public String convertToDatabaseColumn(DmpIdType value) {
        if (value == null) return null;
        return value.getValue();
    }

    @Override
    public DmpIdType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : this.of(dbData);
    }
}
