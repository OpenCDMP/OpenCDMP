package org.opencdmp.data.converters.enums;

import org.opencdmp.commons.enums.PlanVersionStatus;
import jakarta.persistence.Converter;

@Converter
public class PlanVersionStatusConverter extends DatabaseEnumConverter<PlanVersionStatus, Short> {

    @Override
    protected PlanVersionStatus of(Short i) {
        return PlanVersionStatus.of(i);
    }

}
