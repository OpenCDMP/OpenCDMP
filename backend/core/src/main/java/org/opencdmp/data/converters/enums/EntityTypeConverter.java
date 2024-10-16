package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.EntityType;
import jakarta.persistence.Converter;

@Converter
public class EntityTypeConverter extends DatabaseEnumConverter<EntityType, Short>{

    @Override
    protected EntityType of(Short i) {
        return EntityType.of(i);
    }

}
