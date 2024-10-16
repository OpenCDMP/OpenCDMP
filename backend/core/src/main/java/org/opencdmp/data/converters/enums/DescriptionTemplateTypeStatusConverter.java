package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.DescriptionTemplateTypeStatus;
import org.opencdmp.commons.enums.IsActive;
import jakarta.persistence.Converter;

@Converter
public class DescriptionTemplateTypeStatusConverter extends DatabaseEnumConverter<DescriptionTemplateTypeStatus, Short> {
    public DescriptionTemplateTypeStatus of(Short i) {
        return DescriptionTemplateTypeStatus.of(i);
    }
}
