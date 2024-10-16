package org.opencdmp.commons.types.prefillingsource;

import org.opencdmp.commons.types.externalfetcher.ExternalFetcherApiSourceConfigurationEntity;
import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class PrefillingSourceDefinitionEntity {
    @XmlElement(name = "searchConfiguration")
    private ExternalFetcherApiSourceConfigurationEntity searchConfiguration;
    @XmlElement(name = "getConfiguration")
    private ExternalFetcherApiSourceConfigurationEntity getConfiguration;
    @XmlElementWrapper(name = "fields")
    @XmlElement(name = "field")
    private List<PrefillingSourceDefinitionFieldEntity> fields;
    @XmlElementWrapper(name = "fixedValueFields")
    @XmlElement(name = "fixedValueField")
    private List<PrefillingSourceDefinitionFixedValueFieldEntity> fixedValueFields;

    public ExternalFetcherApiSourceConfigurationEntity getSearchConfiguration() {
        return searchConfiguration;
    }

    public void setSearchConfiguration(ExternalFetcherApiSourceConfigurationEntity searchConfiguration) {
        this.searchConfiguration = searchConfiguration;
    }

    public ExternalFetcherApiSourceConfigurationEntity getGetConfiguration() {
        return getConfiguration;
    }

    public void setGetConfiguration(ExternalFetcherApiSourceConfigurationEntity getConfiguration) {
        this.getConfiguration = getConfiguration;
    }

    public List<PrefillingSourceDefinitionFieldEntity> getFields() {
        return fields;
    }

    public void setFields(List<PrefillingSourceDefinitionFieldEntity> fields) {
        this.fields = fields;
    }

    public List<PrefillingSourceDefinitionFixedValueFieldEntity> getFixedValueFields() {
        return fixedValueFields;
    }

    public void setFixedValueFields(List<PrefillingSourceDefinitionFixedValueFieldEntity> fixedValueFields) {
        this.fixedValueFields = fixedValueFields;
    }
}
