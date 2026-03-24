package org.opencdmp.data.converters.enums;

import jakarta.persistence.Converter;
import org.opencdmp.commons.enums.PlanBlueprintTypeStatus;

@Converter
public class PlanBlueprintTypeStatusConverter extends DatabaseEnumConverter<PlanBlueprintTypeStatus, Short> {
    public PlanBlueprintTypeStatus of(Short i) {
        return PlanBlueprintTypeStatus.of(i);
    }
}
