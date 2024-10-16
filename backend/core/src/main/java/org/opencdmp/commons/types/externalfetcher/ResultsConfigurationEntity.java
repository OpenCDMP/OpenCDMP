package org.opencdmp.commons.types.externalfetcher;

import org.opencdmp.service.externalfetcher.config.entities.ResultsConfiguration;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.List;

public class ResultsConfigurationEntity implements ResultsConfiguration<ResultFieldsMappingConfigurationEntity> {
    private String resultsArrayPath;
    private List<ResultFieldsMappingConfigurationEntity> fieldsMapping;

    public String getResultsArrayPath() {
        return resultsArrayPath;
    }

    @XmlElement(name = "resultsArrayPath")
    public void setResultsArrayPath(String resultsArrayPath) {
        this.resultsArrayPath = resultsArrayPath;
    }

    public List<ResultFieldsMappingConfigurationEntity> getFieldsMapping() {
        return fieldsMapping;
    }

    @XmlElementWrapper
    @XmlElement(name = "field")
    public void setFieldsMapping(List<ResultFieldsMappingConfigurationEntity> fieldsMapping) {
        this.fieldsMapping = fieldsMapping;
    }

}
