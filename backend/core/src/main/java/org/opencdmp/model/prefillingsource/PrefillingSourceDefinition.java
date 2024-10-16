package org.opencdmp.model.prefillingsource;

import org.opencdmp.model.externalfetcher.ExternalFetcherApiSourceConfiguration;

import java.util.List;

public class PrefillingSourceDefinition {

    private ExternalFetcherApiSourceConfiguration searchConfiguration;
    public final static String _searchConfiguration = "searchConfiguration";

    private ExternalFetcherApiSourceConfiguration getConfiguration;
    public final static String _getConfiguration = "getConfiguration";

    private List<PrefillingSourceDefinitionField> fields;
    public final static String _fields = "fields";

    private List<PrefillingSourceDefinitionFixedValueField> fixedValueFields;
    public final static String _fixedValueFields = "fixedValueFields";

    public ExternalFetcherApiSourceConfiguration getSearchConfiguration() {
        return searchConfiguration;
    }

    public void setSearchConfiguration(ExternalFetcherApiSourceConfiguration searchConfiguration) {
        this.searchConfiguration = searchConfiguration;
    }

    public ExternalFetcherApiSourceConfiguration getGetConfiguration() {
        return getConfiguration;
    }

    public void setGetConfiguration(ExternalFetcherApiSourceConfiguration getConfiguration) {
        this.getConfiguration = getConfiguration;
    }

    public List<PrefillingSourceDefinitionField> getFields() {
        return fields;
    }

    public void setFields(List<PrefillingSourceDefinitionField> fields) {
        this.fields = fields;
    }

    public List<PrefillingSourceDefinitionFixedValueField> getFixedValueFields() {
        return fixedValueFields;
    }

    public void setFixedValueFields(List<PrefillingSourceDefinitionFixedValueField> fixedValueFields) {
        this.fixedValueFields = fixedValueFields;
    }
}
