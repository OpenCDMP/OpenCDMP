package org.opencdmp.service.maintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KpiProperties.class)
public class KpiConfiguration {
	private final KpiProperties properties;

	@Autowired
	public KpiConfiguration(KpiProperties properties) {
		this.properties = properties;
	}

	public KpiProperties getProperties() {
		return this.properties;
	}
}
