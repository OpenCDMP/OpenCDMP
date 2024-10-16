package org.opencdmp.model.externalfetcher;

import org.opencdmp.commons.enums.ExternalFetcherSourceType;
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
}
