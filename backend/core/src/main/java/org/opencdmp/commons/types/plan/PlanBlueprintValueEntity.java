package org.opencdmp.commons.types.plan;

import java.time.Instant;
import java.util.UUID;

public class PlanBlueprintValueEntity {

    private UUID fieldId;

    private String value;

    private Instant dateValue;

    private Double numberValue;

    public UUID getFieldId() {
        return fieldId;
    }

    public void setFieldId(UUID fieldId) {
        this.fieldId = fieldId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getDateValue() {
        return dateValue;
    }

    public void setDateValue(Instant dateValue) {
        this.dateValue = dateValue;
    }

    public Double getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Double numberValue) {
        this.numberValue = numberValue;
    }
}
