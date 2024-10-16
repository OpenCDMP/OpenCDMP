package org.opencdmp.commons.types.description;


import java.time.Instant;
import java.util.List;

public class FieldEntity {
    private String textValue;
    private List<String> textListValue;
    private Instant dateValue;
    private Boolean booleanValue;
    private ExternalIdentifierEntity externalIdentifier;

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

    public ExternalIdentifierEntity getExternalIdentifier() {
        return this.externalIdentifier;
    }

    public void setExternalIdentifier(ExternalIdentifierEntity externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
    }

    public Boolean getBooleanValue() {
        return this.booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
}
