package org.opencdmp.commons.configuration.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IntegrationProperties.class)
public class IntegrationConfiguration {
	private final IntegrationProperties properties;

	@Autowired
	public IntegrationConfiguration(IntegrationProperties properties) {
		this.properties = properties;
	}

	public IntegrationProperties getProperties() {
		return this.properties;
	}
}
