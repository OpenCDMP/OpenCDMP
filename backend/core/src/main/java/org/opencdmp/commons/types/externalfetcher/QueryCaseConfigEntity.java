package org.opencdmp.commons.types.externalfetcher;

import org.opencdmp.service.externalfetcher.config.entities.QueryCaseConfig;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.UUID;

public class QueryCaseConfigEntity implements QueryCaseConfig {

    private UUID referenceTypeId;
    private String referenceTypeSourceKey;
    private String likePattern;
    private String separator;
    private String value;


    @Override
    public String getReferenceTypeSourceKey() {
        return referenceTypeSourceKey;
    }

    @XmlElement(name = "referenceTypeSourceKey")
    public void setReferenceTypeSourceKey(String referenceTypeSourceKey) {
        this.referenceTypeSourceKey = referenceTypeSourceKey;
    }

    @Override
    public UUID getReferenceTypeId() {
        return referenceTypeId;
    }

    @XmlElement(name = "referenceTypeId")
    public void setReferenceTypeId(UUID referenceTypeId) {
        this.referenceTypeId = referenceTypeId;
    }

    @Override
    public String getLikePattern() {
        return likePattern;
    }

    @XmlElement(name = "likePattern")
    public void setLikePattern(String likePattern) {
        this.likePattern = likePattern;
    }

    @Override
    public String getSeparator() {
        return separator;
    }

    @XmlElement(name = "separator")
    public void setSeparator(String separator) {
        this.separator = separator;
    }

    @Override
    public String getValue() {
        return value;
    }

    @XmlElement(name = "value")
    public void setValue(String value) {
        this.value = value;
    }
}
