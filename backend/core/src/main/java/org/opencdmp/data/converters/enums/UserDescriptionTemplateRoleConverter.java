package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.UserDescriptionTemplateRole;
import jakarta.persistence.Converter;

@Converter
public class UserDescriptionTemplateRoleConverter extends DatabaseEnumConverter<UserDescriptionTemplateRole, Short>{
    protected UserDescriptionTemplateRole of(Short i) {
        return UserDescriptionTemplateRole.of(i);
    }
}
