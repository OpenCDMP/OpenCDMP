package org.opencdmp.data.converters.enums;


import org.opencdmp.commons.enums.ActionConfirmationStatus;
import jakarta.persistence.Converter;

@Converter
public class ActionConfirmationStatusConverter extends DatabaseEnumConverter<ActionConfirmationStatus, Short> {

    @Override
    protected ActionConfirmationStatus of(Short i) {
        return ActionConfirmationStatus.of(i);
    }

}
