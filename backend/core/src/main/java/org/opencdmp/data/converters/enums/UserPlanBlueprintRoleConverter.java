package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.UserPlanBlueprintRole;
import jakarta.persistence.Converter;

@Converter
public class UserPlanBlueprintRoleConverter extends DatabaseEnumConverter<UserPlanBlueprintRole, Short>{
    protected UserPlanBlueprintRole of(Short i) {
        return UserPlanBlueprintRole.of(i);
    }
}
