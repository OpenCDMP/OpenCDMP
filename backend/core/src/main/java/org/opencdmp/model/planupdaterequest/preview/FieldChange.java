package org.opencdmp.model.planupdaterequest.preview;

import org.opencdmp.model.persist.descriptionproperties.FieldPersist;

import java.util.UUID;

public class FieldChange {

    private UUID changeId;
    public static final String _changeId = "changeId";

    private String fieldId;
    public static final String _fieldId = "fieldId";

    private String fieldLabel;
    public static final String _fieldLabel = "fieldLabel";

    private FieldPersist currentValue;
    public static final String _currentValue = "currentValue";

    private FieldPersist incomingValue;
    public static final String _incomingValue = "incomingValue";

    public UUID getChangeId() {
        return changeId;
    }

    public void setChangeId(UUID changeId) {
        this.changeId = changeId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    public FieldPersist getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(FieldPersist currentValue) {
        this.currentValue = currentValue;
    }

    public FieldPersist getIncomingValue() {
        return incomingValue;
    }

    public void setIncomingValue(FieldPersist incomingValue) {
        this.incomingValue = incomingValue;
    }
}
