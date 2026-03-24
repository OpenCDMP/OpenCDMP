package org.opencdmp.commons.types.referencetype;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.ReferenceFieldDataType;

import java.util.List;

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

    @XmlElementWrapper(name = "semantics")
    @XmlElement(name = "semantic")
    private List<String> semantics;

    @XmlAttribute(name = "required")
    private boolean required;

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

    public List<String> getSemantics() {
        return semantics;
    }

    public void setSemantics(List<String> semantics) {
        this.semantics = semantics;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
