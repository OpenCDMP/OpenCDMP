package org.opencdmp.commons.types.description.importexport;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.ReferenceFieldDataType;

import java.util.List;
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
    @XmlElementWrapper(name = "fields")
    @XmlElement(name = "field")
    private List<ReferenceFieldImportExport> fields;

    @XmlType(name = "descriptionReferenceFieldImportExport")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ReferenceFieldImportExport {

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

    public List<ReferenceFieldImportExport> getFields() {
        return fields;
    }

    public void setFields(List<ReferenceFieldImportExport> fields) {
        this.fields = fields;
    }
}
