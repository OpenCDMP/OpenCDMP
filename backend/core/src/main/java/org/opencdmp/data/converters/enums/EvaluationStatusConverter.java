package org.opencdmp.data.converters.enums;

import jakarta.persistence.Converter;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.enums.EvaluationStatus;

@Converter
public class EvaluationStatusConverter extends DatabaseEnumConverter<EvaluationStatus, Short>{

    @Override
    protected EvaluationStatus of(Short i) {
        return EvaluationStatus.of(i);
    }

}
