package org.opencdmp.commons.types.description.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionReferenceImportExport {

    @XmlElement(name = "id")
    private UUID id;
    @XmlElement(name = "label")
    private String label;
    @XmlElement(name = "reference")
    private String reference;
    @XmlElement(name = "source")
    private String source;
    @XmlElement(name = "fieldId")
    private String fieldId;
    @XmlElement(name = "ordinal")
    private int ordinal;
    @XmlElement(name = "type")
    private DescriptionReferenceTypeImportExport type;

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getReference() {
        return this.reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public DescriptionReferenceTypeImportExport getType() {
        return this.type;
    }

    public void setType(DescriptionReferenceTypeImportExport type) {
        this.type = type;
    }

    public String getFieldId() {
        return this.fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
