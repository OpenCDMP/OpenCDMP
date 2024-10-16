package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.PlanUserRole;
import jakarta.persistence.Converter;

@Converter
public class PlanUserRoleConverter extends DatabaseEnumConverter<PlanUserRole, Short> {

    @Override
    protected PlanUserRole of(Short i) {
        return PlanUserRole.of(i);
    }

}
