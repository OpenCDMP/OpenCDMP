package org.opencdmp.service.externalfetcher.config.entities;

import org.opencdmp.commons.enums.ExternalFetcherSourceType;

public interface  SourceBaseConfiguration {
    String getKey();
    String getLabel();
    Integer getOrdinal();
    ExternalFetcherSourceType getType();
}
