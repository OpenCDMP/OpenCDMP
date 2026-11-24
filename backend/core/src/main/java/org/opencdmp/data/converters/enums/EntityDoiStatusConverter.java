package org.opencdmp.data.converters.enums;

import jakarta.persistence.Converter;
import org.opencdmp.commons.enums.EntityDoiStatus;

@Converter
public class EntityDoiStatusConverter extends DatabaseEnumConverter<EntityDoiStatus, Short> {
    protected EntityDoiStatus of(Short i) {
        return EntityDoiStatus.of(i);
    }
}
