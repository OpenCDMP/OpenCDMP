package org.opencdmp.data.converters.enums;


import jakarta.persistence.Converter;
import org.opencdmp.commons.enums.PlanUpdateRequestActionType;

@Converter
public class PlanUpdateRequestActionTypeConverter extends DatabaseEnumConverter<PlanUpdateRequestActionType, Short> {

    @Override
    protected PlanUpdateRequestActionType of(Short i) {
        return PlanUpdateRequestActionType.of(i);
    }

}
