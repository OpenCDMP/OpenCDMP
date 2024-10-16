package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import jakarta.persistence.Converter;

@Converter
public class DescriptionTemplateVersionStatusConverter extends DatabaseEnumConverter<DescriptionTemplateVersionStatus, Short>{
    protected DescriptionTemplateVersionStatus of(Short i) {
        return DescriptionTemplateVersionStatus.of(i);
    }
}
