package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.IsActive;
import jakarta.persistence.Converter;

@Converter
public class IsActiveConverter extends DatabaseEnumConverter<IsActive, Short> {
    public IsActive of(Short i) {
        return IsActive.of(i);
    }
}
