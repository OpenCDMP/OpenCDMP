package org.opencdmp.commons.types.pluginconfiguration;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class PluginConfigurationUserFieldEntity {

    @XmlAttribute(name="code")
    private String code;

    @XmlAttribute(name="fileValue")
    private UUID fileValue;

    @XmlAttribute(name="textValue")
    private String textValue;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UUID getFileValue() {
        return fileValue;
    }

    public void setFileValue(UUID fileValue) {
        this.fileValue = fileValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }
}
