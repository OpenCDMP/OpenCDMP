package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.TenantConfigurationType;
import jakarta.persistence.Converter;

@Converter
public class TenantConfigurationTypeConverter extends DatabaseEnumConverter<TenantConfigurationType, Short> {
    public TenantConfigurationType of(Short i) {
        return TenantConfigurationType.of(i);
    }
}
