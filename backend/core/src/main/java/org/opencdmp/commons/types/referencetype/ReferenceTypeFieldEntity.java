package org.opencdmp.commons.types.referencetype;

import org.opencdmp.commons.enums.ReferenceFieldDataType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "field")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReferenceTypeFieldEntity {

    @XmlAttribute(name = "code")
    private String code;

    @XmlAttribute(name = "label")
    private String label;

    @XmlAttribute(name = "description")
    private String description;
    @XmlAttribute(name = "dataType")
    private ReferenceFieldDataType dataType;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ReferenceFieldDataType getDataType() {
        return dataType;
    }

    public void setDataType(ReferenceFieldDataType dataType) {
        this.dataType = dataType;
    }

}
