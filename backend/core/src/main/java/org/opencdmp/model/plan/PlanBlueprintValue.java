package org.opencdmp.model.plan;

import java.time.Instant;
import java.util.UUID;

public class PlanBlueprintValue {
    private UUID fieldId;

    public static final String _fieldId = "fieldId";
    private String fieldValue;
    public static final String _fieldValue = "fieldValue";

    private Instant dateValue;
    public static final String _dateValue = "dateValue";

    private Double numberValue;
    public static final String _numberValue = "numberValue";

    public UUID getFieldId() {
        return fieldId;
    }

    public void setFieldId(UUID fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
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
