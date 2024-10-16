package org.opencdmp.service.externalfetcher.config.entities;

import java.util.List;

public interface QueryConfig<CaseConfig extends QueryCaseConfig> {
    String getName();

    List<CaseConfig> getCases();

    String getDefaultValue();
}

