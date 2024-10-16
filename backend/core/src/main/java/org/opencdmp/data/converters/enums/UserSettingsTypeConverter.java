package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.UserSettingsType;
import jakarta.persistence.Converter;

@Converter
public class UserSettingsTypeConverter extends DatabaseEnumConverter<UserSettingsType, Short> {
    public UserSettingsType of(Short i) {
        return UserSettingsType.of(i);
    }
}
