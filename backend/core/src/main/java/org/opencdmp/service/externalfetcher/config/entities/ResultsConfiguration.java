package org.opencdmp.service.externalfetcher.config.entities;

import java.util.List;

public interface ResultsConfiguration <T extends ResultFieldsMappingConfiguration> {
    String getResultsArrayPath();

    List<T> getFieldsMapping();
}
