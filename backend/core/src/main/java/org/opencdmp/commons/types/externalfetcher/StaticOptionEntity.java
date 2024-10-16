package org.opencdmp.commons.types.externalfetcher;

import org.opencdmp.service.externalfetcher.config.entities.StaticOption;
import jakarta.xml.bind.annotation.XmlElement;

public class StaticOptionEntity implements StaticOption {

    private String code;

    private String value;

    public String getCode() {
        return code;
    }

    @XmlElement(name = "code")
    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    @XmlElement(name = "value")
    public void setValue(String value) {
        this.value = value;
    }
}
