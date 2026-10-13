package org.opencdmp.service.externalfetcher.config.entities;

import org.opencdmp.commons.enums.ExternalFetcherApiHTTPMethodType;
import org.opencdmp.commons.enums.ExternalFetcherApiResponseFormatType;

import java.util.List;

public interface SourceExternalApiConfiguration<RsConfig extends ResultsConfiguration<? extends ResultFieldsMappingConfiguration>, AuthConfig extends AuthenticationConfiguration, QConfig extends QueryConfig<? extends QueryCaseConfig> , HeaderConfig extends ExternalFetcherApiHeaderConfiguration> extends  SourceBaseConfiguration  {
    String getUrl();

    RsConfig getResults();

    String getPaginationPath();

    String getContentType();

    String getFirstPage();

    ExternalFetcherApiHTTPMethodType getHttpMethod();

    String getRequestBody();

    String getFilterType();

    ExternalFetcherApiResponseFormatType getResponseFormat();

    AuthConfig getAuth();

    List<QConfig> getQueries();

    List<HeaderConfig> getHeaders();

    List<? extends XmlNamespaceConfiguration> getNamespaces();
}


