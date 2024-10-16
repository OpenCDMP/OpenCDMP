package org.opencdmp.data.converters.enums;


import org.opencdmp.commons.enums.ActionConfirmationType;
import jakarta.persistence.Converter;

@Converter
public class ActionConfirmationTypeConverter extends DatabaseEnumConverter<ActionConfirmationType, Short> {

    @Override
    protected ActionConfirmationType of(Short i) {
        return ActionConfirmationType.of(i);
    }

}
