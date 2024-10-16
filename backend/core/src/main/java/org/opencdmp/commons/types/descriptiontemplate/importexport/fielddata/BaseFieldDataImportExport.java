package org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import org.opencdmp.commons.enums.FieldType;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseFieldDataImportExport {
    @XmlElement(name = "label")
    private String label;
    @XmlElement(name = "fieldType")
    private FieldType fieldType;

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public FieldType getFieldType() {
        return this.fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }
}
