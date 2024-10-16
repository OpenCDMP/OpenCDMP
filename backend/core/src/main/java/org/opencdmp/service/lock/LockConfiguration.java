package org.opencdmp.service.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LockProperties.class)
public class LockConfiguration {
	private final LockProperties properties;

	@Autowired
	public LockConfiguration(LockProperties properties) {
		this.properties = properties;
	}

	public LockProperties getProperties() {
		return properties;
	}
}
