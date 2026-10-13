package org.opencdmp.model.externalfetcher;

import org.opencdmp.commons.enums.ExternalFetcherSourceType;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.referencetype.ReferenceType;

import java.util.List;


public abstract class ExternalFetcherBaseSourceConfiguration {

    public final static String _key = "key";
    private String key;

    public final static String _label = "label";
    private String label;

    public final static String _ordinal = "ordinal";
    private Integer ordinal;

    public final static String _type = "type";
    private ExternalFetcherSourceType type;

    public final static String _referenceTypeDependencies = "referenceTypeDependencies";
    private List<ReferenceType> referenceTypeDependencies;

    public final static String _storageFile = "storageFile";
    private StorageFile storageFile;

    public final static String _navigationUrl = "navigationUrl";
    private String navigationUrl;

    public final static String _tooltipMessage = "tooltipMessage";
    private String tooltipMessage;

    public ExternalFetcherSourceType getType() {
        return type;
    }

    public void setType(ExternalFetcherSourceType type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public List<ReferenceType> getReferenceTypeDependencies() {
        return referenceTypeDependencies;
    }

    public void setReferenceTypeDependencies(List<ReferenceType> referenceTypeDependencies) {
        this.referenceTypeDependencies = referenceTypeDependencies;
    }

    public StorageFile getStorageFile() {
        return storageFile;
    }

    public void setStorageFile(StorageFile storageFile) {
        this.storageFile = storageFile;
    }

    public String getNavigationUrl() {
        return navigationUrl;
    }

    public void setNavigationUrl(String navigationUrl) {
        this.navigationUrl = navigationUrl;
    }

    public String getTooltipMessage() {
        return tooltipMessage;
    }

    public void setTooltipMessage(String tooltipMessage) {
        this.tooltipMessage = tooltipMessage;
    }
}
