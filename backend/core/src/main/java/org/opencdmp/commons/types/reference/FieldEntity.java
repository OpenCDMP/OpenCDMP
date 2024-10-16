package org.opencdmp.commons.types.reference;

import org.opencdmp.commons.enums.ReferenceFieldDataType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "field")
@XmlAccessorType(XmlAccessType.FIELD)
public class FieldEntity {

    @XmlAttribute(name = "code")
    private String code;
    @XmlAttribute(name = "dataType")
    private ReferenceFieldDataType dataType;
    @XmlAttribute(name = "value")
    private String value;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public ReferenceFieldDataType getDataType() {
        return dataType;
    }

    public void setDataType(ReferenceFieldDataType dataType) {
        this.dataType = dataType;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
