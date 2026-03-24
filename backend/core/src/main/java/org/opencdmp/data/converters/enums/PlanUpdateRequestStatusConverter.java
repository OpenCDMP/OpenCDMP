package org.opencdmp.data.converters.enums;


import jakarta.persistence.Converter;
import org.opencdmp.commons.enums.PlanUpdateRequestStatus;

@Converter
public class PlanUpdateRequestStatusConverter extends DatabaseEnumConverter<PlanUpdateRequestStatus, Short> {

    @Override
    protected PlanUpdateRequestStatus of(Short i) {
        return PlanUpdateRequestStatus.of(i);
    }

}
