package org.opencdmp.commons.types.description.importexport;



import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opencdmp.commons.xmladapter.InstantXmlAdapter;

import java.time.Instant;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionFieldImportExport {
    @XmlElement(name = "fieldId")
    private String fieldId;
    @XmlElement(name = "textValue")
    private String textValue;
    @XmlElementWrapper(name = "textListValues")
    @XmlElement(name = "textListValue")
    private List<String> textListValue;
    @XmlElement(name = "dateValue")
    @XmlJavaTypeAdapter(InstantXmlAdapter.class)
    private Instant dateValue;

    @XmlElement(name = "booleanValue")
    private Boolean booleanValue;
    @XmlElement(name = "externalIdentifier")
    private DescriptionExternalIdentifierImportExport externalIdentifier;

    public String getFieldId() {
        return this.fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getTextValue() {
        return this.textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public List<String> getTextListValue() {
        return this.textListValue;
    }

    public void setTextListValue(List<String> textListValue) {
        this.textListValue = textListValue;
    }

    public Instant getDateValue() {
        return this.dateValue;
    }

    public void setDateValue(Instant dateValue) {
        this.dateValue = dateValue;
    }

    public Boolean getBooleanValue() {
        return this.booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public DescriptionExternalIdentifierImportExport getExternalIdentifier() {
        return this.externalIdentifier;
    }

    public void setExternalIdentifier(DescriptionExternalIdentifierImportExport externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
    }
}
