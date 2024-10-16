package org.opencdmp.commons.types.externalfetcher;

import org.opencdmp.commons.enums.ExternalFetcherSourceType;
import org.opencdmp.service.externalfetcher.config.entities.SourceBaseConfiguration;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.List;
import java.util.UUID;

public abstract class ExternalFetcherBaseSourceConfigurationEntity implements SourceBaseConfiguration {

    private String key;

    private String label;

    private Integer ordinal;
    private ExternalFetcherSourceType type;

    private List<UUID> referenceTypeDependencyIds;
    public ExternalFetcherSourceType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    @XmlElement(name = "key")
    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    @XmlElement(name = "label")
    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    @XmlElement(name = "ordinal")
    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    @XmlElement(name = "type")
    public void setType(ExternalFetcherSourceType type) {
        this.type = type;
    }

    public List<UUID> getReferenceTypeDependencyIds() {
        return referenceTypeDependencyIds;
    }

    @XmlElementWrapper
    @XmlElement(name = "referenceTypeDependencyIds")
    public void setReferenceTypeDependencyIds(List<UUID> referenceTypeDependencyIds) {
        this.referenceTypeDependencyIds = referenceTypeDependencyIds;
    }
}
