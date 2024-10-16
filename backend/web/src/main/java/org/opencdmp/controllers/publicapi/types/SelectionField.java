package org.opencdmp.controllers.publicapi.types;

/**
 * Created by ikalyvas on 2/7/2018.
 */
public class SelectionField {
    private FieldSelectionType type = FieldSelectionType.FIELD;
    private String field;

    public SelectionField(String field) {
        this.field = field;
    }

    public SelectionField(FieldSelectionType type, String field) {
        this.type = type;
        this.field = field;
    }

    public FieldSelectionType getType() {
        return type;
    }

    public void setType(FieldSelectionType type) {
        this.type = type;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
