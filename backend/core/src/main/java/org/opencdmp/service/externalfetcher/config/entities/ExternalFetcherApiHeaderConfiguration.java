package org.opencdmp.service.externalfetcher.config.entities;

import org.opencdmp.commons.enums.ExternalFetcherApiHeaderType;

public interface ExternalFetcherApiHeaderConfiguration {

    ExternalFetcherApiHeaderType getKey();

    String getValue();


}
