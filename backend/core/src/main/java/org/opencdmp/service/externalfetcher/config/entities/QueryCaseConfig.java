package org.opencdmp.service.externalfetcher.config.entities;

import java.util.UUID;

public interface QueryCaseConfig {
    UUID getReferenceTypeId();
    String getReferenceTypeSourceKey();

    String getLikePattern();

    String getSeparator();

    String getValue();

}
