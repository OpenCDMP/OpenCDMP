package org.opencdmp.model.publicapi.datasetwizard;

import org.opencdmp.commons.enums.FieldType;

public class FieldDescriptionEntity {
    private FieldType fieldType;
    private String cssClass;

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }


}
