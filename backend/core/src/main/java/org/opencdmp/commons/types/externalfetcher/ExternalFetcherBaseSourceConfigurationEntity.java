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

    private UUID storageFileId;

    private String navigationUrl;

    private String tooltipMessage;

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

    public UUID getStorageFileId() {
        return storageFileId;
    }

    @XmlElement(name = "storageFileId")
    public void setStorageFileId(UUID storageFileId) {
        this.storageFileId = storageFileId;
    }

    public String getNavigationUrl() {
        return navigationUrl;
    }

    @XmlElement(name = "navigationUrl")
    public void setNavigationUrl(String navigationUrl) {
        this.navigationUrl = navigationUrl;
    }

    public String getTooltipMessage() {
        return tooltipMessage;
    }

    @XmlElement(name = "tooltipMessage")
    public void setTooltipMessage(String tooltipMessage) {
        this.tooltipMessage = tooltipMessage;
    }
}
