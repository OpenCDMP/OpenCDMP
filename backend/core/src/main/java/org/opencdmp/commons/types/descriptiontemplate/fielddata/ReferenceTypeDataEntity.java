package org.opencdmp.commons.types.descriptiontemplate.fielddata;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class ReferenceTypeDataEntity extends BaseFieldDataEntity {
    public static final String XmlElementName = "referenceTypeData";

    @XmlAttribute(name = "multipleSelect")
    private Boolean multipleSelect;

    @XmlAttribute(name = "referenceTypeId")
    private UUID referenceTypeId;
    public Boolean getMultipleSelect() {
        return multipleSelect;
    }

    public void setMultipleSelect(Boolean multipleSelect) {
        this.multipleSelect = multipleSelect;
    }

    public UUID getReferenceTypeId() {
        return referenceTypeId;
    }

    public void setReferenceTypeId(UUID referenceTypeId) {
        this.referenceTypeId = referenceTypeId;
    }
}
