package org.opencdmp.commons.types.planblueprint;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class ReferenceTypeFieldEntity extends FieldEntity {

    @XmlAttribute(name="referenceTypeId")
    private UUID referenceTypeId;

    @XmlAttribute(name = "multipleSelect")
    private Boolean multipleSelect;
    public UUID getReferenceTypeId() {
        return this.referenceTypeId;
    }

    public void setReferenceTypeId(UUID referenceTypeId) {
        this.referenceTypeId = referenceTypeId;
    }

    public Boolean getMultipleSelect() {
        return this.multipleSelect;
    }

    public void setMultipleSelect(Boolean multipleSelect) {
        this.multipleSelect = multipleSelect;
    }
}
