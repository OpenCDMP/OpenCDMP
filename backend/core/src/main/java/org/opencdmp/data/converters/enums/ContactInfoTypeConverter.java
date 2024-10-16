package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.ContactInfoType;
import org.opencdmp.commons.enums.DescriptionStatus;
import jakarta.persistence.Converter;

@Converter
public class ContactInfoTypeConverter extends DatabaseEnumConverter<ContactInfoType, Short>{
    protected ContactInfoType of(Short i) {
        return ContactInfoType.of(i);
    }
}
