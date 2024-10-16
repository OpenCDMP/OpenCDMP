package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.DescriptionStatus;
import jakarta.persistence.Converter;

@Converter
public class DescriptionStatusConverter extends DatabaseEnumConverter<DescriptionStatus, Short>{
    protected DescriptionStatus of(Short i) {
        return DescriptionStatus.of(i);
    }
}
