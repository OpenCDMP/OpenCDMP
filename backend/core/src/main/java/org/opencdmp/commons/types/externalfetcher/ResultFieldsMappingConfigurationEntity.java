package org.opencdmp.commons.types.externalfetcher;

import jakarta.xml.bind.annotation.XmlElement;
import org.opencdmp.service.externalfetcher.config.entities.ResultFieldsMappingConfiguration;

public class ResultFieldsMappingConfigurationEntity implements ResultFieldsMappingConfiguration {
    private String code;
    private String responsePath;

    public String getCode() {
        return this.code;
    }

    @XmlElement(name = "code")
    public void setCode(String code) {
        this.code = code;
    }

    public String getResponsePath() {
        return this.responsePath;
    }

    @XmlElement(name = "responsePath")
    public void setResponsePath(String responsePath) {
        this.responsePath = responsePath;
    }

}
