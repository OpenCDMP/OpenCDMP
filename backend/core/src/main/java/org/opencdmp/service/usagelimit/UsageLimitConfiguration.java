package org.opencdmp.service.usagelimit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(UsageLimitProperties.class)
public class UsageLimitConfiguration {
	private final UsageLimitProperties properties;

	@Autowired
	public UsageLimitConfiguration(UsageLimitProperties properties) {
		this.properties = properties;
	}

	public UsageLimitProperties getProperties() {
		return this.properties;
	}
}
