package org.opencdmp.commons.scope.tenant;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MultitenancyProperties.class)
public class MultitenancyConfiguration {
	private final MultitenancyProperties config;


	public MultitenancyConfiguration(MultitenancyProperties config) {
		this.config = config;
	}

	public MultitenancyProperties getConfig() {
		return this.config;
	}
}
