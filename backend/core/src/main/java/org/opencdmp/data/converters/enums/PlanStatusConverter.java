package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.PlanStatus;
import jakarta.persistence.Converter;

@Converter
public class PlanStatusConverter extends DatabaseEnumConverter<PlanStatus, Short> {

    @Override
    protected PlanStatus of(Short i) {
        return PlanStatus.of(i);
    }

}
