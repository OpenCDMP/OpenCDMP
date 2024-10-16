package org.opencdmp.commons.types.descriptiontemplate.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opencdmp.commons.xmladapter.InstantXmlAdapter;

import java.time.Instant;

@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionTemplateRuleImportExport {

    @XmlElement(name="target")
    private String target;
    @XmlElement(name="value")
    private String textValue;

    @XmlElement(name = "dateValue")
    @XmlJavaTypeAdapter(InstantXmlAdapter.class)
    private Instant dateValue;

    @XmlElement(name = "booleanValue")
    private Boolean booleanValue;
    
    public String getTarget() {
        return this.target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTextValue() {
        return this.textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
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
}