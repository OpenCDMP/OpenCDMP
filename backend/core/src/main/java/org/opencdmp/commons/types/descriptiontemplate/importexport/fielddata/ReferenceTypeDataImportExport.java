package org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class ReferenceTypeDataImportExport extends BaseFieldDataImportExport {
    public static final String XmlElementName = "referenceTypeData";

    @XmlAttribute(name = "multipleSelect")
    private Boolean multipleSelect;

    @XmlElement(name = "referenceTypeId")
    private UUID referenceTypeId;

    @XmlElement(name = "referenceTypeName")
    private String referenceTypeName;

    @XmlElement(name = "referenceTypeCode")
    private String referenceTypeCode;

    public Boolean getMultipleSelect() {
        return this.multipleSelect;
    }

    public void setMultipleSelect(Boolean multipleSelect) {
        this.multipleSelect = multipleSelect;
    }

    public UUID getReferenceTypeId() {
        return this.referenceTypeId;
    }

    public void setReferenceTypeId(UUID referenceTypeId) {
        this.referenceTypeId = referenceTypeId;
    }

    public String getReferenceTypeCode() {
        return this.referenceTypeCode;
    }

    public void setReferenceTypeCode(String referenceTypeCode) {
        this.referenceTypeCode = referenceTypeCode;
    }

    public String getReferenceTypeName() {
        return referenceTypeName;
    }

    public void setReferenceTypeName(String referenceTypeName) {
        this.referenceTypeName = referenceTypeName;
    }
}
