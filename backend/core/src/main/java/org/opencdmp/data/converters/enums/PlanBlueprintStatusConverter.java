package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.PlanBlueprintStatus;
import jakarta.persistence.Converter;

@Converter
public class PlanBlueprintStatusConverter extends DatabaseEnumConverter<PlanBlueprintStatus, Short> {
    public PlanBlueprintStatus of(Short i) {
        return PlanBlueprintStatus.of(i);
    }
}
