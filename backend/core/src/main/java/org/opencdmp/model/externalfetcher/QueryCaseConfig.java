package org.opencdmp.model.externalfetcher;


import org.opencdmp.model.referencetype.ReferenceType;

public class QueryCaseConfig {

    public final static String _likePattern = "likePattern";
    private String likePattern;

    public final static String _separator = "separator";
    private String separator;

    public final static String _value = "value";
    private String value;

    public final static String _referenceType = "referenceType";
    private ReferenceType referenceType;

    public final static String _referenceTypeSourceKey = "referenceTypeSourceKey";
    private String referenceTypeSourceKey;

    public String getLikePattern() {
        return likePattern;
    }

    public void setLikePattern(String likePattern) {
        this.likePattern = likePattern;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(ReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceTypeSourceKey() {
        return referenceTypeSourceKey;
    }

    public void setReferenceTypeSourceKey(String referenceTypeSourceKey) {
        this.referenceTypeSourceKey = referenceTypeSourceKey;
    }
}

