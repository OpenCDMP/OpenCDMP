package org.opencdmp.service.externalfetcher.config.entities;

import org.opencdmp.commons.enums.ExternalFetcherApiHTTPMethodType;

public interface AuthenticationConfiguration {

    Boolean getEnabled();

    String getAuthUrl();

    ExternalFetcherApiHTTPMethodType getAuthMethod();

    String getAuthTokenPath();

    String getAuthRequestBody();

    String getType();
}
