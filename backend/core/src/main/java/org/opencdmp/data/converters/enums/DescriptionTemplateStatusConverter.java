package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.DescriptionTemplateStatus;
import org.opencdmp.commons.enums.DescriptionTemplateTypeStatus;
import jakarta.persistence.Converter;

@Converter
public class DescriptionTemplateStatusConverter extends DatabaseEnumConverter<DescriptionTemplateStatus, Short> {
    public DescriptionTemplateStatus of(Short i) {
        return DescriptionTemplateStatus.of(i);
    }
}
