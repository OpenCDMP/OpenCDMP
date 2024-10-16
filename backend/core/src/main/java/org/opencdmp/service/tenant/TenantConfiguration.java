package org.opencdmp.service.tenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TenantProperties.class)
public class TenantConfiguration {
	private final TenantProperties properties;

	@Autowired
	public TenantConfiguration(TenantProperties properties) {
		this.properties = properties;
	}

	public TenantProperties getProperties() {
		return properties;
	}
}
