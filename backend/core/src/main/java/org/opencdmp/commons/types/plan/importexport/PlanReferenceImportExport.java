package org.opencdmp.commons.types.plan.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import org.opencdmp.commons.enums.ReferenceSourceType;

import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class PlanReferenceImportExport {

    @XmlElement(name = "id")
    private UUID id;
    @XmlElement(name = "label")
    private String label;
    @XmlElement(name = "reference")
    private String reference;
    @XmlElement(name = "fieldId")
    private UUID fieldId;
    @XmlElement(name = "type")
    private PlanReferenceTypeImportExport type;
    @XmlElement(name = "source")
    private String source;
    @XmlElement(name = "sourceType")
    private ReferenceSourceType sourceType;

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

    public PlanReferenceTypeImportExport getType() {
        return this.type;
    }

    public void setType(PlanReferenceTypeImportExport type) {
        this.type = type;
    }

    public UUID getFieldId() {
        return this.fieldId;
    }

    public void setFieldId(UUID fieldId) {
        this.fieldId = fieldId;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ReferenceSourceType getSourceType() {
        return this.sourceType;
    }

    public void setSourceType(ReferenceSourceType sourceType) {
        this.sourceType = sourceType;
    }
}
