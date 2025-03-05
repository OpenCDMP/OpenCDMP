package org.opencdmp.commons.types.externalfetcher;

import jakarta.xml.bind.annotation.XmlElement;
import org.opencdmp.commons.enums.ExternalFetcherApiHeaderType;
import org.opencdmp.service.externalfetcher.config.entities.ExternalFetcherApiHeaderConfiguration;

public class ExternalFetcherApiHeaderConfigurationEntity implements ExternalFetcherApiHeaderConfiguration {


    private ExternalFetcherApiHeaderType key;
    private String value;

    @Override
    public ExternalFetcherApiHeaderType getKey() {
        return key;
    }
    @XmlElement(name = "key")
    public void setKey(ExternalFetcherApiHeaderType key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }
    @XmlElement(name = "value")
    public void setValue(String value) {
        this.value = value;
    }
}
