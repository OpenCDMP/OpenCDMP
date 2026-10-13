package org.opencdmp.commons.types.externalfetcher;

import jakarta.xml.bind.annotation.XmlElement;
import org.opencdmp.service.externalfetcher.config.entities.XmlNamespaceConfiguration;

public class XmlNamespaceConfigurationEntity implements XmlNamespaceConfiguration {

    private String prefix;
    private String uri;

    @Override
    public String getPrefix() {
        return prefix;
    }
    @XmlElement(name = "prefix")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getUri() {
        return uri;
    }
    @XmlElement(name = "uri")
    public void setUri(String uri) {
        this.uri = uri;
    }
}
