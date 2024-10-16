package org.opencdmp.commons.types.descriptiontemplate.fielddata;

import org.opencdmp.commons.enums.FieldType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;


@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseFieldDataEntity  {

    @XmlAttribute(name = "label")
    private String label;
    @XmlAttribute(name = "fieldType")
    private FieldType fieldType;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }
}
