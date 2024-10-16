package org.opencdmp.model.externalfetcher;


import java.util.List;

public class ResultsConfiguration {

    public final static String _resultsArrayPath = "resultsArrayPath";
    private String resultsArrayPath;

    public final static String _fieldsMapping = "fieldsMapping";
    private List<ResultFieldsMappingConfiguration> fieldsMapping;

    public String getResultsArrayPath() {
        return resultsArrayPath;
    }

    public void setResultsArrayPath(String resultsArrayPath) {
        this.resultsArrayPath = resultsArrayPath;
    }

    public List<ResultFieldsMappingConfiguration> getFieldsMapping() {
        return fieldsMapping;
    }

    public void setFieldsMapping(List<ResultFieldsMappingConfiguration> fieldsMapping) {
        this.fieldsMapping = fieldsMapping;
    }
}
