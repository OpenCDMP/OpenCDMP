package org.opencdmp.service.externalfetcher;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ExternalFetcherProperties.class})
public class ExternalFetcherConfiguration {
	private final ExternalFetcherProperties externalFetcherProperties;

	public ExternalFetcherConfiguration(ExternalFetcherProperties externalFetcherProperties) {
		this.externalFetcherProperties = externalFetcherProperties;
	}

	public ExternalFetcherProperties getPlanServiceProperties() {
		return externalFetcherProperties;
	}
}
