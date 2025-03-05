package org.opencdmp.model.externalfetcher;

import org.opencdmp.commons.enums.ExternalFetcherApiHeaderType;

public class ExternalFetcherApiHeaderConfiguration {
    public final static String _key = "key";
    private ExternalFetcherApiHeaderType key;

    public final static String _value = "value";
    private String value;

    public ExternalFetcherApiHeaderType getKey() {
        return key;
    }

    public void setKey(ExternalFetcherApiHeaderType key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
