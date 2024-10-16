package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.PlanBlueprintVersionStatus;
import org.opencdmp.data.converters.enums.DatabaseEnumConverter;
import jakarta.persistence.Converter;

@Converter
public class PlanBlueprintVersionStatusConverter extends DatabaseEnumConverter<PlanBlueprintVersionStatus, Short> {

    @Override
    protected PlanBlueprintVersionStatus of(Short i) {
        return PlanBlueprintVersionStatus.of(i);
    }

}
